package com.baumexpert.brennholzdemo.company.analytics;

import com.baumexpert.brennholzdemo.company.Company;
import com.baumexpert.brennholzdemo.company.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * F-041 S4: Grafana zeigt nur Kennzahlen, Zeitfilter und Aktualisieren – keine Navigation, kein Bearbeiten.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CompanyAnalyticsControllerTest {

    private static final String EMAIL = "betrieb@test.de";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyRepository companyRepository;

    @Test
    void grafanaWirdImKioskModusMitZeitfilterEingebettet() throws Exception {
        Company company = mock(Company.class);
        when(company.getId()).thenReturn(42L);
        when(companyRepository.findByEmail(EMAIL)).thenReturn(Optional.of(company));

        mockMvc.perform(get("/company/analytics").with(user(EMAIL).roles("COMPANY")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("grafanaIframeUrl", allOf(
                        containsString("&kiosk&"),
                        containsString("&refresh=5s"),
                        containsString("_dash.hideTimePicker=false"),
                        containsString("_dash.hideVariables=true"),
                        containsString("_dash.hideLinks=true"),
                        containsString("var-company_id=42"),
                        not(containsString("kiosk=tv")))));
    }
}
