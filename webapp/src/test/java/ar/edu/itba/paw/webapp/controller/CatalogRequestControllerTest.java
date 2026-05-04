package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.AdminRequestService;
import ar.edu.itba.paw.services.BodyTypeRequestService;
import ar.edu.itba.paw.services.BrandRequestService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.controller.support.ControllerTestMvcSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CatalogRequestControllerTest {

    @Mock
    private BrandRequestService brandRequestService;

    @Mock
    private BodyTypeRequestService bodyTypeRequestService;

    @Mock
    private AdminRequestService adminRequestService;

    @InjectMocks
    private CatalogRequestController controller;

    private MockMvc catalogMockMvc() throws Exception {
        final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private static AuthenticatedUser testUser(final long id) {
        return new AuthenticatedUser(id, "Contributor", id + "@test.com", "pw", Collections.emptyList());
    }

    @Test
    void requestBrand_anonymous_redirectsLogin() throws Exception {
        // Arrange
        final MockMvc mockMvc = catalogMockMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(
                        post("/brand-requests").param("name", "NovaMarca").param("comments", "Please add."));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test
    void requestBrand_authenticated_redirectsToCarsWithSubmitted() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(15L));

        try {
            final MockMvc mockMvc = catalogMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            post("/brand-requests")
                                    .with(ControllerTestMvcSupport.authenticationPrincipalRequestPostProcessor())
                                    .header("Referer", "http://localhost/cars?sort=popular")
                                    .param("name", "NovaMarca")
                                    .param("comments", "Please add."));
            // Assertions
            resultActions
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/cars?submitted=brand"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }

    @Test
    void requestBrand_validationError_redirectsBackOnReferer() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(16L));

        try {
            final MockMvc mockMvc = catalogMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            post("/brand-requests")
                                    .with(ControllerTestMvcSupport.authenticationPrincipalRequestPostProcessor())
                                    .header("Referer", "http://localhost/reviews")
                                    .param("name", ""));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/reviews"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }

    @Test
    void requestBodyType_authenticated_redirectsWithSubmittedSlug() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(17L));

        try {
            final MockMvc mockMvc = catalogMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            post("/body-type-requests")
                                    .with(ControllerTestMvcSupport.authenticationPrincipalRequestPostProcessor())
                                    .param("name", "Pickup")
                                    .param("comments", ""));
            // Assertions
            resultActions
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/cars?submitted=body-type"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }

    @Test
    void requestAdmin_whenPendingAlreadyExists_redirectsBack() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(18L));
        when(adminRequestService.hasPendingRequest(eq(18L))).thenReturn(true);

        try {
            final MockMvc mockMvc = catalogMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            post("/admin-requests")
                                    .with(ControllerTestMvcSupport.authenticationPrincipalRequestPostProcessor())
                                    .header("Referer", "http://localhost/profile")
                                    .param("motivation", "I want to help.")
                                    .param("bio", "Experienced moderator.")
                                    .param("justification", "Fair and consistent."));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/profile"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }

    @Test
    void requestAdmin_createsPendingAndRedirectsWithSubmittedModeratorTab() throws Exception {
        // Arrange
        ControllerTestMvcSupport.bindPrincipal(testUser(19L));
        when(adminRequestService.hasPendingRequest(eq(19L))).thenReturn(false);

        try {
            final MockMvc mockMvc = catalogMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            post("/admin-requests")
                                    .with(ControllerTestMvcSupport.authenticationPrincipalRequestPostProcessor())
                                    .header("Referer", "http://localhost/cars?page=3")
                                    .param("motivation", "Motivation text here.")
                                    .param("bio", "Bio text here.")
                                    .param("justification", "Justification text here."));
            // Assertions
            resultActions
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/cars?page=3&submitted=moderator"));
        } finally {
            ControllerTestMvcSupport.clearSecurityContext();
        }
    }
}
