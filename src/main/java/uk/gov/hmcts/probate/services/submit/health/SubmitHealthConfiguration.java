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

    @Bean
    @ConditionalOnProperty(prefix = "services.coreCaseData", name = "enabled", matchIfMissing = true)
    public SubmitHealthIndicator ccdServiceHealthIndicator() {
    	return new SubmitHealthIndicator(servicesCcdBaseUrl, restTemplate);
    }


}
