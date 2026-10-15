package com.baumexpert.brennholzdemo.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * F-041 S1: Prometheus muss die Kennzahlen ohne Login abholen können.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ActuatorAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void prometheusEndpunktIstOhneLoginErreichbar() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andExpect(content().string(containsString("# TYPE")));
    }

    @Test
    void healthEndpunktIstOhneLoginErreichbar() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void betriebsStatistikBleibtGeschuetzt() throws Exception {
        mockMvc.perform(get("/company/analytics"))
                .andExpect(status().is3xxRedirection());
    }
}
