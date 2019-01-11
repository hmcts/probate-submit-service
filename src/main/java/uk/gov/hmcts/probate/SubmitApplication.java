package uk.gov.hmcts.probate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.healthcheck.ServiceAuthHealthIndicator;

@SpringBootApplication(exclude= {ServiceAuthHealthIndicator.class})
public class SubmitApplication {

  public static void main(String[] args) {
    SpringApplication.run(SubmitApplication.class, args);
  }

  @Bean
  @ConfigurationProperties(prefix = "mail")
  public JavaMailSenderImpl javaMailSender() {
    return new JavaMailSenderImpl();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
