package dev.workhard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AppTest {
    @Autowired MockMvc mvc;

    @Test
    void blankTitleIsRejected() throws Exception {
        mvc.perform(post("/todos").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validTitleIsAccepted() throws Exception {
        mvc.perform(post("/todos").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"study\"}"))
                .andExpect(status().isOk());
    }
}
