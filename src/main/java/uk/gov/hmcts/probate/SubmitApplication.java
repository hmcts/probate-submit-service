package uk.gov.hmcts.probate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SubmitApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubmitApplication.class, args);
    }

}
