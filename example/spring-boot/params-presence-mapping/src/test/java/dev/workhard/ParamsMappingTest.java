package dev.workhard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ParamsMappingTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new EntryController(), new ErrorPageController()).build();
    }

    @Test
    void 쿼리가_없으면_일반_진입으로_간다() throws Exception {
        mvc.perform(get("/main")).andExpect(status().isOk()).andExpect(content().string("entry"));
    }

    @Test
    void serverError_1이면_에러_안내로_간다() throws Exception {
        mvc.perform(get("/main?serverError=1")).andExpect(content().string("server-error"));
    }

    @Test
    void 다른_값이면_404를_준다() throws Exception {
        mvc.perform(get("/main?serverError=abc")).andExpect(status().isNotFound());
    }

    @Test
    void 값이_비어_있어도_키가_있으면_에러_매핑으로_간다() throws Exception {
        mvc.perform(get("/main?serverError")).andExpect(status().isNotFound());
    }
}
