package uk.gov.hmcts.probate.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.AuthCheckerServiceAndUserFilter;

@Profile("!SECURITY_MOCK")
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final AuthCheckerServiceAndUserFilter filter;

    public SecurityConfiguration(RequestAuthorizer<Service> serviceRequestAuthorizer,
                                 AuthenticationManager authenticationManager,
                                 RequestAuthorizer<User> userRequestAuthorizer) {
        filter = new AuthCheckerServiceAndUserFilter(serviceRequestAuthorizer, userRequestAuthorizer);
        filter.setAuthenticationManager(authenticationManager);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/cases/**",
                        "/submissions/**",
                        "/payments/**",
                        "/ccd-case-update/**",
                        "/health",
                        "/health/liveness"
                )
                .addFilter(filter)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
            );
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {

        return web -> web.ignoring().requestMatchers(
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/health",
                "/health/liveness",
                "/info",
                "/"
        );
    }
}
