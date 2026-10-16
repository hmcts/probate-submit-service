package uk.gov.hmcts.probate;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import feign.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableFeignClients
@SpringBootApplication()
public class SubmitApplication {
    public static void main(String[] args) {
        ApplicationInsights.attach();
        SpringApplication.run(SubmitApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
