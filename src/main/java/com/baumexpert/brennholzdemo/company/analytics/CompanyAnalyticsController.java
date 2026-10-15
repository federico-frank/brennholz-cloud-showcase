package com.baumexpert.brennholzdemo.company.analytics;

import com.baumexpert.brennholzdemo.company.Company;
import com.baumexpert.brennholzdemo.company.CompanyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/company/analytics")
public class CompanyAnalyticsController {

    @Value("${grafana.dashboard.base-url}")
    private String grafanaBaseUrl;

    @Value("${grafana.dashboard.uid}")
    private String dashboardUid;

    @Value("${grafana.dashboard.slug}")
    private String dashboardSlug;

    private final CompanyRepository companyRepository;

    public CompanyAnalyticsController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping
    public String showAnalytics(Model model, Principal principal) {
        // Sicherheits-Check: Redirect zum Login, falls unangemeldet aufgerufen
        if (principal == null) {
            return "redirect:/login";
        }

        // Der Principal Name entspricht der E-Mail (definiert in CustomUserDetailsService)
        String userEmail = principal.getName();

        // Lade die Company basierend auf der E-Mail
        Optional<Company> companyOpt = companyRepository.findByEmail(userEmail);

        if (companyOpt.isEmpty()) {
            // Fallback, falls der eingeloggte Nutzer (noch) kein Company Profil angelegt hat.
            // Leitet auf das Profil-Dashboard oder die Startseite um.
            return "redirect:/company/profile";
        }

        Company company = companyOpt.get();
        String currentCompanyId = String.valueOf(company.getId());

        // URL dynamisch mit der echten ID generieren (var-company_id filtert das Dashboard).
        // Kiosk blendet Navigation, Seitenleiste und Anmelden aus; Zeitfilter und Aktualisieren bleiben (F-041 S4).
        // refresh=5s: Zahlen aktualisieren sich von selbst (F-041 S5)
        String grafanaUrl = String.format("%s/%s/%s?orgId=1&theme=light&kiosk&refresh=5s"
                        + "&_dash.hideTimePicker=false&_dash.hideVariables=true&_dash.hideLinks=true"
                        + "&var-company_id=%s",
                grafanaBaseUrl,
                dashboardUid,
                dashboardSlug,
                currentCompanyId
        );

        model.addAttribute("grafanaIframeUrl", grafanaUrl);

        return "company/analytics/dashboard";
    }
}