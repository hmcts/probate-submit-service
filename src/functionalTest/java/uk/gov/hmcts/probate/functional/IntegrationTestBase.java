package uk.gov.hmcts.probate.functional;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.junit.spring.SpringIntegration;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringIntegrationSerenityRunner.class)
@ContextConfiguration(classes = FunctionalTestContextConfiguration.class)
@Ignore
public class IntegrationTestBase {

    @Autowired
    protected FunctionalTestUtils utils;

    protected String submitServiceUrl;

    protected String persistenceServiceUrl;

    protected String submissionId;

    protected String idamUrl;

    private static String SESSION_ID = "tom@email.com";

    @Autowired
    public void submitServiceConfiguration(@Value("${probate.submit.url}") String submitServiceUrl,
                                           @Value("${probate.persistence.url}") String persistenceServiceUrl,
                                           @Value("${user.auth.provider.oauth2.url}") String idamUrl) {
        this.submitServiceUrl = submitServiceUrl;
        this.persistenceServiceUrl = persistenceServiceUrl;
        this.idamUrl = idamUrl;
    }

    @Rule
    public SpringIntegration springIntegration;

    IntegrationTestBase() {
        this.springIntegration = new SpringIntegration();
    }

    void populateFormDataTable() {
        RestAssured.baseURI = persistenceServiceUrl;
        RequestSpecification request = RestAssured.given();

        request.header("Content-Type", "application/json");
        request.header("Session-Id", SESSION_ID);
        request.body(utils.getJsonFromFile("formData.json"));
        request.post(persistenceServiceUrl + "/formdata");

        request.header("Content-Type", "application/json");
        request.header("Session-Id", SESSION_ID);
        request.body(utils.getJsonFromFile("submitData.json"));
        Response response = request.post(persistenceServiceUrl + "/submissions");
        submissionId = response.jsonPath().getString("id");
    }
}