package uk.gov.hmcts.probate;

import feign.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.ccd.client",
    "uk.gov.hmcts.probate.services",
    "uk.gov.hmcts.reform.authorisation"})
@SpringBootApplication()
public class SubmitApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubmitApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
