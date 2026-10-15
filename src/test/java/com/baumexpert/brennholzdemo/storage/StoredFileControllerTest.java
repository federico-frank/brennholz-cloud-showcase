package com.baumexpert.brennholzdemo.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * F-040 S7: Bilder kommen über /bilder/{key} aus dem konfigurierten Speicher (lokal oder S3) – auch ohne Login.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StoredFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    void liefertBildOhneLoginMitCacheHeader() throws Exception {
        when(fileStorageService.load("abc.png")).thenReturn(Optional.of(new StoredFile(new byte[]{1, 2, 3}, "image/png")));

        mockMvc.perform(get("/bilder/abc.png"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("Cache-Control", containsString("max-age")))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void unbekanntesBildIst404() throws Exception {
        when(fileStorageService.load(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/bilder/fehlt.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    void alterSchluesselMitLeerzeichenFunktioniert() throws Exception {
        when(fileStorageService.load("5042_Bildschirmfoto 2026.png"))
                .thenReturn(Optional.of(new StoredFile(new byte[]{9}, "image/png")));

        // URI statt Vorlage, sonst kodiert MockMvc das %20 doppelt
        mockMvc.perform(get(URI.create("/bilder/5042_Bildschirmfoto%202026.png")))
                .andExpect(status().isOk());
    }
}
