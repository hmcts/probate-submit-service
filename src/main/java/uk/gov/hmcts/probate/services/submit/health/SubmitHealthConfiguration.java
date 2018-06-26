package uk.gov.hmcts.probate.services.submit.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SubmitHealthConfiguration
{	
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${services.coreCaseData.baseUrl}")
    private String servicesCcdBaseUrl;

    @Value("${services.persistence.baseUrl}")
    private String servicesPersistenceBaseUrl;

    @Value("${idam.s2s-auth.url}")
    private String idamS2sAuthUrl;

    @Bean
    @ConditionalOnProperty(prefix = "services.coreCaseData", name = "enabled", matchIfMissing = true)
    public SubmitHealthIndicator ccdServiceHealthIndicator() {
    	return new SubmitHealthIndicator(servicesCcdBaseUrl, restTemplate); 
    }  
    
    @Bean
    public SubmitHealthIndicator persistenceServiceHealthIndicator() {
    	return new SubmitHealthIndicator(servicesPersistenceBaseUrl, restTemplate); 
    }

    @Bean
    public SubmitHealthIndicator serviceAuthHealthIndicator() {
        return new SubmitHealthIndicator(idamS2sAuthUrl, restTemplate);
    }
}
