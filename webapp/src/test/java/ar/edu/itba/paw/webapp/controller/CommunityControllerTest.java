package ar.edu.itba.paw.webapp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class CommunityControllerTest {

    @Test
    void communitiesPage_rendersHubView() throws Exception {
        // Arrange
        final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CommunityController()).build();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/communities"));
        // Assertions
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("communities.jsp"));
    }

    @Test
    void createCommunityPage_rendersCreateCommunityView() throws Exception {
        // Arrange
        final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CommunityController()).build();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/communities/new"));
        // Assertions
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("community-create.jsp"));
    }

    @Test
    void communityDetail_knownSlug_rendersDetailView() throws Exception {
        // Arrange
        final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CommunityController()).build();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/communities/classics"));
        // Assertions
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("community-detail.jsp"));
    }

    @Test
    void communityPostDetail_knownPost_rendersPostDetailView() throws Exception {
        // Arrange
        final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CommunityController()).build();
        // Exercise
        final ResultActions resultActions =
            mockMvc.perform(get("/communities/classics/posts/falcon-60"));
        // Assertions
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("community-post-detail.jsp"));
    }

    @Test
    void createCommunityPost_knownSlug_rendersPostFormView() throws Exception {
        // Arrange
        final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CommunityController()).build();
        // Exercise
        final ResultActions resultActions =
            mockMvc.perform(get("/communities/classics/submit"));
        // Assertions
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("community-post-form.jsp"));
    }
}
