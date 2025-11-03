package com.epam.rd.autocode.spring.project.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class HomeSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetLoginPage_WhenAnonymous_ShouldReturnLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }
}
