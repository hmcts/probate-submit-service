package uk.gov.hmcts.probate;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.probate.services.submit.Registry;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Configuration
@ConfigurationProperties
public class ApplicationConfig {
    private List<Registry> registries = new ArrayList<>();

    @Autowired
    public List<Registry> getRegistries() {
        return registries;
    }

    @Bean
    public Map<Integer, Registry> registryMap() {
        return registries
                .stream()
                .collect(Collectors.toMap(s -> s.getId(), s -> s));
    }

    @Bean
    @ConfigurationProperties(prefix = "mail")
    public JavaMailSenderImpl javaMailSender() {
        return new JavaMailSenderImpl();
    }


    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate(getClientHttpRequestFactory());
    }
    
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        int timeout = 5000;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(config)
                .build();
        return new HttpComponentsClientHttpRequestFactory(client);
    }
}
