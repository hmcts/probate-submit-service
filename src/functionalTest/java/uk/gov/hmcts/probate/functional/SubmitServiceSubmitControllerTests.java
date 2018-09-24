package uk.gov.hmcts.probate.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;

public class SubmitServiceSubmitControllerTests extends IntegrationTestBase {
    private static String SESSION_ID = "tom@email.com";
    private static boolean INITIALISED = false;
    private static long SUBMISSION_ID = 59;
    private static String INVALID_ID = "invalid_id";
    private static int ERROR_CODE = 400;

    @Before
    public void setUp() {
        if (INITIALISED) return;
        populateFormDataTable();
        INITIALISED = true;
    }

    @Test
    public void submitSuccess() {
        validateSubmitSuccess();
    }

    @Test
    public void submitFailure() {
        validateSubmitFailure(ERROR_CODE);
    }

    @Test
    public void resubmitSuccess() {
        validateReSubmitSuccess();
    }

    @Test
    public void resubmitFailure() {
        validateReSubmitFailure(ERROR_CODE);
    }

    private void validateSubmitSuccess() {
        SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.submitHeaders(SESSION_ID))
                .body(utils.getJsonFromFile("submitData.json"))
                .when().post(submitServiceUrl + "/submit")
                .then().assertThat().statusCode(200);
    }

    private void validateSubmitFailure(int errorCode) {
        Response response = SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.submitHeaders(SESSION_ID))
                .when().post(submitServiceUrl + "/submit")
                .thenReturn();

        response.then().assertThat().statusCode(errorCode);
    }

    private void validateReSubmitSuccess() {
        SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(submitServiceUrl + "/resubmit/" + SUBMISSION_ID)
                .then().assertThat().statusCode(200);
    }

    private void validateReSubmitFailure(int errorCode) {
        Response response = SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(submitServiceUrl + "/resubmit/" + INVALID_ID)
                .thenReturn();

        response.then().assertThat().statusCode(errorCode);
    }
}
