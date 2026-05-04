package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.exception.EmailAlreadyExistsException;
import ar.edu.itba.paw.services.exception.UsernameAlreadyExistsException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthController controller;

    private MockMvc authMvc() throws Exception {
        final LocalValidatorFactoryBean validatorBean = new LocalValidatorFactoryBean();
        validatorBean.afterPropertiesSet();

        return MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validatorBean)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private static void bindPrincipal(final AuthenticatedUser user) {
        final UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Standalone MockMvc resolves {@code Authentication} via {@code HttpServletRequest#getUserPrincipal()}.
     * Tests that only set {@link SecurityContextHolder} must also attach the same token to the request.
     */
    private static RequestPostProcessor securityContextAuthenticationAsRequestPrincipal() {
        return request -> {
            request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication());
            return request;
        };
    }

    private static UsernamePasswordAuthenticationToken authenticatedAuthToken(final AuthenticatedUser user) {
        return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
    }

    @Test
    void loginPage_anonymous_showsLoginView() throws Exception {
        // Arrange
        final MockMvc mockMvc = authMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/login"));
        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("login.jsp"));
    }

    @Test
    void loginPage_alreadyAuthenticated_redirectsHome() throws Exception {
        // Arrange
        bindPrincipal(testUser());
        try {
            final MockMvc mockMvc = authMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(get("/login").with(securityContextAuthenticationAsRequestPrincipal()));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void loginPage_withErrorParam_addsErrorCode() throws Exception {
        // Arrange
        final MockMvc mockMvc = authMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/login").param("error", ""));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(model().attributeExists("loginErrorCode"));
    }

    @Test
    void loginPage_withLogoutParam_addsMessageCode() throws Exception {
        // Arrange
        final MockMvc mockMvc = authMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/login").param("logout", ""));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(model().attributeExists("loginMessageCode"));
    }

    @Test
    void loginPage_withRegisteredParam_addsMessageCode() throws Exception {
        // Arrange
        final MockMvc mockMvc = authMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/login").param("registered", ""));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(model().attributeExists("loginMessageCode"));
    }

    @Test
    void registerPage_anonymous_showsRegisterView() throws Exception {
        // Arrange
        final MockMvc mockMvc = authMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/register"));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(view().name("register.jsp"));
    }

    @Test
    void registerPage_alreadyAuthenticated_redirectsHome() throws Exception {
        // Arrange
        bindPrincipal(testUser());
        try {
            final MockMvc mockMvc = authMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(get("/register").with(securityContextAuthenticationAsRequestPrincipal()));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void createAccount_validForm_autoLoginSucceeds_redirectsHome() throws Exception {
        // Arrange
        when(authenticationManager.authenticate(any())).thenReturn(authenticatedAuthToken(testUser()));
        final MockMvc mockMvc = authMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(post("/register")
                .param("username", "validuser")
                .param("email", "valid@test.com")
                .param("password", "securepass")
                .param("confirmPassword", "securepass"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
    }

    @Test
    void createAccount_validForm_autoLoginFails_redirectsToRegistered() throws Exception {
        // Arrange
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        final MockMvc mockMvc = authMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(post("/register")
                .param("username", "validuser")
                .param("email", "valid@test.com")
                .param("password", "securepass")
                .param("confirmPassword", "securepass"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?registered"));
    }

    @Test
    void createAccount_emailAlreadyExists_showsFormWithError() throws Exception {
        // Arrange
        doThrow(new EmailAlreadyExistsException("duplicate@test.com")).when(userService).createUser(any(), any(), any());
        final MockMvc mockMvc = authMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(post("/register")
                .param("username", "validuser")
                .param("email", "valid@test.com")
                .param("password", "securepass")
                .param("confirmPassword", "securepass"));
        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("register.jsp"))
                .andExpect(model().attribute("registrationErrorCode", "auth.register.error.email.exists"));
    }

    @Test
    void createAccount_usernameAlreadyExists_showsFormWithError() throws Exception {
        // Arrange
        doThrow(new UsernameAlreadyExistsException("validuser")).when(userService).createUser(any(), any(), any());
        final MockMvc mockMvc = authMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(post("/register")
                .param("username", "validuser")
                .param("email", "valid@test.com")
                .param("password", "securepass")
                .param("confirmPassword", "securepass"));
        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("register.jsp"))
                .andExpect(model().attribute("registrationErrorCode", "auth.register.error.username.exists"));
    }

    @Test
    void createAccount_validationErrors_missingUsername_showsForm() throws Exception {
        // Arrange
        final MockMvc mockMvc = authMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(post("/register")
                .param("email", "valid@test.com")
                .param("password", "securepass")
                .param("confirmPassword", "securepass"));
        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("register.jsp"))
                .andExpect(model().attributeExists("registrationErrorCode"));
    }

    @Test
    void createAccount_alreadyAuthenticated_redirectsHome() throws Exception {
        // Arrange
        bindPrincipal(testUser());

        try {
            final MockMvc mockMvc = authMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            post("/register")
                                    .with(securityContextAuthenticationAsRequestPrincipal())
                                    .param("username", "validuser")
                                    .param("email", "valid@test.com")
                                    .param("password", "securepass")
                                    .param("confirmPassword", "securepass"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
        } finally {
            clearSecurityContext();
        }
    }

    private static AuthenticatedUser testUser() {
        return new AuthenticatedUser(1L, "driver", "driver@test.com", "pass", Collections.emptyList());
    }
}
