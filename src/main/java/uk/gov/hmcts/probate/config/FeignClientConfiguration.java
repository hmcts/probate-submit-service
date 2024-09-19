package uk.gov.hmcts.probate.config;

import feign.Client;
import feign.hc5.ApacheHttp5Client;
import lombok.Getter;
import lombok.Setter;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FeignClientConfiguration {

    @Setter
    @Getter
    private int timeout;

    @Bean
    public Client getFeignHttpClient() {
        return new ApacheHttp5Client(getHttpClient());
    }

    @Bean("restTemplateForPact")
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(getHttpClient()));
        return restTemplate;
    }

    private CloseableHttpClient getHttpClient() {
        RequestConfig config = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofMilliseconds(timeout))
                .build();

        return HttpClients.custom()
                .useSystemProperties()
                .setDefaultRequestConfig(config)
                .build();
    }
}
