package ar.edu.itba.paw.webapp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ErrorPageControllerTest {

    private final ErrorPageController controller = new ErrorPageController();

    private MockMvc mockMvc() {
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
                .andExpect(model().attribute("titleCode", "error.page.400.title"))
                .andExpect(model().attribute("descriptionCode", "error.page.400.description"));
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
                .andExpect(model().attribute("titleCode", "error.page.404.title"))
                .andExpect(model().attribute("descriptionCode", "error.page.404.description"));
    }
}
