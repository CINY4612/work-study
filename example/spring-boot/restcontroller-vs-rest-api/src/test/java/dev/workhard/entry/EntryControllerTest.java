package dev.workhard.entry;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class EntryControllerTest {

    @Test
    void restControllerCanJustRedirect() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new EntryController.Redirecting()).build();
        mvc.perform(get("/enter"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/pages/home"))
            .andExpect(content().string(""));
    }

    @Test
    void controllerStringIsViewName() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new EntryController.Page()).build();
        // 본문이 아니라 "pages" 뷰로 forward 된다
        mvc.perform(get("/home")).andExpect(forwardedUrl("pages"));
    }

    @Test
    void restControllerStringIsBody() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new EntryController.Text()).build();
        mvc.perform(get("/text")).andExpect(content().string("pages"));
    }

    @Test
    void controllerWithResponseEntityBehavesTheSame() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new EntryController.PlainControllerRedirect()).build();
        mvc.perform(get("/enter2"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/pages/home"));
    }
}
