package com.ethobservatory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dashboardApisAreAvailable() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latestBlock").exists());

        mockMvc.perform(get("/api/transactions").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());

        mockMvc.perform(get("/api/repeated"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes").exists())
                .andExpect(jsonPath("$.edges").exists());

        mockMvc.perform(get("/api/search").param("q", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").exists())
                .andExpect(jsonPath("$.addresses").exists())
                .andExpect(jsonPath("$.blocks").exists());
    }
}
