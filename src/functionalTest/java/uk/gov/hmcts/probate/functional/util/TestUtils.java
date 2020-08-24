package uk.gov.hmcts.probate.functional.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.probate.functional.TestContextConfiguration;
import uk.gov.hmcts.probate.functional.TestTokenGenerator;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@ContextConfiguration(classes = TestContextConfiguration.class)
@Component
public class TestUtils {

    @Value("${idam.username}")
    public String email;

    @Value("${probate.submit.url}")
    public String submitServiceUrl;

    public static final String EMAIL_PLACEHOLDER = "testusername@test.com";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";

    @Autowired
    protected TestTokenGenerator testTokenGenerator;

    private String serviceToken;

    @PostConstruct
    public void init() throws JsonProcessingException, InterruptedException {
        serviceToken = testTokenGenerator.generateServiceAuthorisation();
        testTokenGenerator.createNewUser();

        RestAssured.baseURI = submitServiceUrl;
    }

    public String getJsonFromFile(String fileName) {
        try {
            File file = ResourceUtils.getFile(this.getClass().getResource("/json/" + fileName));
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String createTestCase(String caseData) throws InterruptedException {
        caseData = caseData.replace(EMAIL_PLACEHOLDER, email);

        Response response = RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(getHeaders())
                .body(caseData)
                .when()
                .post("/cases/initiate");
        Thread.sleep(3000); // ensure CCD has time to update fully

        JsonPath jsonPath = JsonPath.from(response.getBody().asString());
        return jsonPath.get("caseInfo.caseId");
    }

    public Headers getHeaders() {
        return Headers.headers(
                new Header("ServiceAuthorization", serviceToken),
                new Header(CONTENT_TYPE, ContentType.JSON.toString()),
                new Header(AUTHORIZATION, testTokenGenerator.generateAuthorisation()));
    }
}
