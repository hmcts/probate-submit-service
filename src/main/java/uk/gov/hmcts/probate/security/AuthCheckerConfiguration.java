package uk.gov.hmcts.probate.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Configuration
public class AuthCheckerConfiguration {

    @Value("#{'${authorised.services}'.split(',\\s*')}")
    private List<String> authorisedServices;

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor() {
        return request -> authorisedServices;
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
        return any -> Collections.emptyList();
    }

    @Bean
    public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
        Pattern pattern = Pattern.compile("^/users/([^/]+)/.+$");

        return request -> {
            Matcher matcher = pattern.matcher(request.getRequestURI());
            boolean matched = matcher.find();
            return Optional.ofNullable(matched ? matcher.group(1) : null);
        };
    }

    @Bean
    @ConditionalOnProperty("idam.s2s-authorised.services")
    public ProbateServiceAuthFilter probateServiceAuthFilter(ServiceAuthorisationApi authorisationApi,
                                             @Value("${idam.s2s-authorised.services}") List<String> authorisedServices,
                                             AuthenticationManager authenticationManager) {

        AuthTokenValidator authTokenValidator = new ServiceAuthTokenValidator(authorisationApi);
        ProbateServiceAuthFilter probateServiceAuthFilter =
                new ProbateServiceAuthFilter(authTokenValidator, authorisedServices);
        probateServiceAuthFilter.setAuthenticationManager(authenticationManager);
        return probateServiceAuthFilter;
    }

    @Bean
    @ConditionalOnMissingBean(name = "preAuthenticatedAuthenticationProvider")
    public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
        PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider =
                new PreAuthenticatedAuthenticationProvider();
        preAuthenticatedAuthenticationProvider.setPreAuthenticatedUserDetailsService(
                token -> new User((String) token.getPrincipal(), "N/A", Collections.emptyList())
        );
        return preAuthenticatedAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider) {
        return new ProviderManager(Collections.singletonList(preAuthenticatedAuthenticationProvider));
    }
}
