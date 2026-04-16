package uk.gov.hmcts.probate.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FeignClientConfiguration {

    @Setter
    @Getter
    private int timeout;

    @Bean
    @Primary
    public CloseableHttpClient primaryCloseableHttpClient() {
        RequestConfig config = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofMilliseconds(timeout))
                .build();

        return HttpClients.custom()
                .useSystemProperties()
                .setDefaultRequestConfig(config)
                .build();
    }
}
