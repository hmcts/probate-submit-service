package uk.gov.hmcts.probate.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;

import static org.hamcrest.Matchers.equalTo;

public class SubmitServiceFunctionalTests extends IntegrationTestBase {
    private static String SESSION_ID = "tom@email.com";
    private static boolean INITIALISED = false;

    @Before
    public void setUp() {
        if (INITIALISED) return;
        populateFormDataTable();
        INITIALISED = true;
    }
    
    private void populateFormDataTable() {
        RestAssured.baseURI = persistenceServiceUrl;
        RequestSpecification request = RestAssured.given();

        request.header("Content-Type", "application/json");
        request.header("Session-Id", SESSION_ID);
        request.body(utils.getJsonFromFile("formData.json"));
        request.post(persistenceServiceUrl + "/formdata");
    }

    public void submitSuccess() {
        validateSubmitSuccess();
    }

    public void submitFailure() {
        validateSubmitFailure();
    }

    public void resubmitSuccess() {
        validateReSubmitSuccess();
    }

    public void resubmitFailure() {
        validateReSubmitFailure();
    }

    private void validateSubmitSuccess() {
        SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(submitServiceUrl + "/submit")
                .then().assertThat().statusCode(200);
    }

    private void validateSubmitFailure(int errorCode, String errorMsg) {
        Response response = SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(persistenceServiceUrl + "/submit")
                .thenReturn();

        response.then().assertThat().statusCode(errorCode)
                .and().body("error", equalTo("Not Found"))
                .and().body("message", equalTo(errorMsg));
    }

    private void validateReSubmitSuccess() {
        SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(submitServiceUrl + "/resubmit/" + submissionId)
                .then().assertThat().statusCode(200);
    }

    private void validateReSubmitFailure(int errorCode, String errorMsg) {
        Response response = SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(persistenceServiceUrl + "/resubmit/" + submissionId)
                .thenReturn();

        response.then().assertThat().statusCode(errorCode)
                .and().body("error", equalTo("Not Found"))
                .and().body("message", equalTo(errorMsg));
    }
}
