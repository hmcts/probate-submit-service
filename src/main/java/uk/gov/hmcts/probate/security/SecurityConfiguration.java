package uk.gov.hmcts.probate.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Profile("!SECURITY_MOCK")
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private ProbateServiceAuthFilter probateServiceAuthFilter;

    public SecurityConfiguration(ProbateServiceAuthFilter probateServiceAuthFilter) {
        this.probateServiceAuthFilter = probateServiceAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .addFilterBefore(probateServiceAuthFilter, AnonymousAuthenticationFilter.class)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/cases/**",
                    "/submissions/**",
                    "/payments/**",
                    "/ccd-case-update/**").permitAll()
                .anyRequest().authenticated())
            .build();
    }


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/swagger-ui.html",
                "/swagger-resources/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/health",
                "/health/**",
                "/info",
                "/");
    }
}
