package uk.gov.hmcts.probate.services.submit;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI draftsApi() {
        return new OpenAPI()
            .components(new Components()
                .addHeaders("Authorization", new Header().description("User authorization header")
                .required(true)
                .schema(new StringSchema()))
            .addHeaders("ServiceAuthorization", new Header().description("Service authorization header")
            .required(true)
            .schema(new StringSchema())))
            .info(draftsApiInfo());
    }

    private Info draftsApiInfo() {
        return new Info()
                .title("Submit Service API documentation")
                .description("Submit Service API documentation");
    }
}
