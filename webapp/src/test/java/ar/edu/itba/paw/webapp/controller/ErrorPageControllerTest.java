package ar.edu.itba.paw.webapp.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ErrorPageControllerTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ErrorPageController controller;

    private MockMvc mockMvc() {
        when(messageSource.getMessage(any(String.class), any(), any(Locale.class)))
                .thenAnswer(inv -> "msg." + inv.getArgument(0));
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void error400_usesFallbackStatusWhenMissingCode() throws Exception {
        // Arrange
        final MockMvc mockMvc = mockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/error/400"));
        // Assertions
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error.jsp"))
                .andExpect(model().attribute("statusCode", 400))
                .andExpect(model().attribute("title", "msg.error.page.400.title"))
                .andExpect(model().attribute("description", "msg.error.page.400.description"));
    }

    @Test
    void error403_rendersErrorView() throws Exception {
        // Arrange
        final MockMvc mockMvc = mockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/error/403"));
        // Assertions
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(view().name("error.jsp"))
                .andExpect(model().attribute("statusCode", 403));
    }

    @Test
    void error404_rendersErrorView() throws Exception {
        // Arrange
        final MockMvc mockMvc = mockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/error/404"));
        // Assertions
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(view().name("error.jsp"))
                .andExpect(model().attribute("statusCode", 404));
    }

    @Test
    void error405_rendersErrorView() throws Exception {
        // Arrange
        final MockMvc mockMvc = mockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/error/405"));
        // Assertions
        resultActions
                .andExpect(status().isMethodNotAllowed())
                .andExpect(view().name("error.jsp"))
                .andExpect(model().attribute("statusCode", 405));
    }

    @Test
    void error413_rendersErrorView() throws Exception {
        // Arrange
        final MockMvc mockMvc = mockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/error/413"));
        // Assertions
        resultActions
                .andExpect(status().isPayloadTooLarge())
                .andExpect(view().name("error.jsp"))
                .andExpect(model().attribute("statusCode", 413));
    }

    @Test
    void error415_rendersErrorView() throws Exception {
        // Arrange
        final MockMvc mockMvc = mockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/error/415"));
        // Assertions
        resultActions
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(view().name("error.jsp"))
                .andExpect(model().attribute("statusCode", 415));
    }

    @Test
    void error500_rendersErrorView() throws Exception {
        // Arrange
        final MockMvc mockMvc = mockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/error/500"));
        // Assertions
        resultActions
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error.jsp"))
                .andExpect(model().attribute("statusCode", 500));
    }

    @Test
    void error404_prefersServletErrorAttributeOverFallback() throws Exception {
        // Arrange
        when(messageSource.getMessage(eq("error.page.404.title"), any(), any(Locale.class)))
                .thenReturn("t");
        when(messageSource.getMessage(eq("error.page.404.description"), any(), any(Locale.class)))
                .thenReturn("d");
        final MockMvc mockMvc =
                MockMvcBuilders.standaloneSetup(controller).build();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(get("/error/404").requestAttr("javax.servlet.error.status_code", 502));
        // Assertions
        resultActions
                .andExpect(status().isBadGateway())
                .andExpect(view().name("error.jsp"))
                .andExpect(model().attribute("statusCode", 502))
                .andExpect(model().attribute("title", "t"))
                .andExpect(model().attribute("description", "d"));
    }
}
