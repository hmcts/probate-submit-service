package uk.gov.hmcts.probate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.probate.services.submit.clients.v2.ccd.CcdClientApiErrorDecoder;

@Configuration
public class CcdClientConfiguration {


    @Bean
    public CcdClientApiErrorDecoder ccdClientApiErrorDecoder(ObjectMapper objectMapper) {
        return new CcdClientApiErrorDecoder(objectMapper);
    }

}
