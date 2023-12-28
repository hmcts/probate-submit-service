package uk.gov.hmcts.probate.services.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.probate.security.ProbateServiceAuthFilter;

@Profile("SECURITY_MOCK")
@Configuration
@EnableWebSecurity
@Order(1)
public class SecurityMockConfiguration {

    private ProbateServiceAuthFilter probateServiceAuthFilter;

    @Autowired
    public SecurityMockConfiguration(ProbateServiceAuthFilter probateServiceAuthFilter) {
        this.probateServiceAuthFilter = probateServiceAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .addFilterBefore(probateServiceAuthFilter, BearerTokenAuthenticationFilter.class)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/cases/**",
                    "/submissions/**",
                    "/payments/**",
                    "/ccd-case-update/**").authenticated()
                    .anyRequest().permitAll());

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/swagger-ui.html",
                "/swagger-resources/**",
                "/swagger-ui/**",
                "/health",
                "/health/liveness",
                "/info",
                "/");
    }
}
