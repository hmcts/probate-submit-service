package uk.gov.hmcts.probate.functional;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import net.thucydides.junit.spring.SpringIntegration;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestContextConfiguration.class)
public abstract class IntegrationTestBase {

    @Autowired
    protected TestUtils utils;

    String submitServiceUrl;
    String persistenceServiceUrl;

    private static String SESSION_ID = "tom@email.com";

    @Autowired
    public void submitServiceConfiguration(@Value("${probate.submit.url}") String submitServiceUrl,
                                           @Value("${probate.persistence.url}") String persistenceServiceUrl) {
        this.submitServiceUrl = submitServiceUrl;
        this.persistenceServiceUrl = persistenceServiceUrl;
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
        request.post(persistenceServiceUrl + "/submission");
    }
}