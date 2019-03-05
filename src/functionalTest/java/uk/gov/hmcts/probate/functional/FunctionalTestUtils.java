package uk.gov.hmcts.probate.functional;

import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@ContextConfiguration(classes = FunctionalTestContextConfiguration.class)
@Component
public class FunctionalTestUtils {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";
    @Autowired
    protected FunctionalTestTokenGenerator functionalTestTokenGenerator;


    public String getJsonFromFile(String fileName) {
        try {
            File file = ResourceUtils.getFile(this.getClass().getResource("/json/" + fileName));
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Headers getHeaders(String sessionId) {
        return Headers.headers(
                new Header(CONTENT_TYPE, ContentType.JSON.toString()),
                new Header("Session-ID", sessionId));
    }

    public Headers submitHeaders(String sessionId) {
        return Headers.headers(
                new Header(CONTENT_TYPE, ContentType.JSON.toString()),
                new Header("UserId", sessionId),
                new Header(AUTHORIZATION, "DUMMY_KEY"));
    }

    public Headers getHeaders(String userName, String password) {
        return Headers.headers(
                new Header("ServiceAuthorization", functionalTestTokenGenerator.generateServiceAuthorisation()),
                new Header(CONTENT_TYPE, ContentType.JSON.toString()),
                new Header(AUTHORIZATION, functionalTestTokenGenerator.generateAuthorisation(userName, password)));
    }
}
