package com.baumexpert.brennholzdemo.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * F-043 S4 / F-047: Bis zum Release von v1.0 ist die gesamte Anwendung für Suchmaschinen gesperrt.
 */
@SpringBootTest
@AutoConfigureMockMvc
class NoIndexTest {

    private static final String NOINDEX = "noindex, nofollow, noarchive";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void jedeAntwortTraegtNoindex() throws Exception {
        mockMvc.perform(get("/")).andExpect(header().string("X-Robots-Tag", NOINDEX));
        mockMvc.perform(get("/company/login")).andExpect(header().string("X-Robots-Tag", NOINDEX));
        // auch Weiterleitungen, Bilder und unbekannte Adressen
        mockMvc.perform(get("/company/profile")).andExpect(header().string("X-Robots-Tag", NOINDEX));
        mockMvc.perform(get("/bilder/gibt-es-nicht.png")).andExpect(header().string("X-Robots-Tag", NOINDEX));
    }

    @Test
    void robotsTxtErlaubtCrawlenDamitNoindexGelesenWird() throws Exception {
        mockMvc.perform(get("/robots.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User-agent: *")))
                .andExpect(content().string(not(containsString("Disallow: /\n"))));
    }
}
