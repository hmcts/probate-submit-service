package uk.gov.hmcts.probate.functional;

import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;

@RunWith(SerenityRunner.class)
public class SubmitServicePersistenceClientTests extends IntegrationTestBase {

    private static String SESSION_ID = "tom@email.com";
    private static boolean INITIALISED = false;

    @Before
    public void setUp() {
        if (INITIALISED) return;
        populateFormDataTable();
        INITIALISED = true;
    }

    @Test
    public void loadFormDataByIdSuccess() {
        validateLoadFormDataIdSuccess();
    }

    @Test
    public void loadFormDataByIdFailure() {
        validateLoadFormDataIdFailure("invalid_id", 404);
    }

    @Test
    public void updateFormDataSuccess() {
        validateUpdateFormDataSuccess();
    }

    @Test
    public void updateFormDataFailure() {
        validateUpdateFormDataFailure(400);
    }


    private void validateLoadFormDataIdSuccess() {
        SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(persistenceServiceUrl + "/formdata/" + SESSION_ID)
                .then().assertThat().statusCode(200);
    }

    private void validateLoadFormDataIdFailure(String emailId, int errorCode) {
        Response response = SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(persistenceServiceUrl + "/formdata/" + emailId)
                .thenReturn();

        response.then().assertThat().statusCode(errorCode);
    }

    private void validateUpdateFormDataSuccess() {
        SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .body(utils.getJsonFromFile("formData.json"))
                .when().put(persistenceServiceUrl + "/formdata/" + SESSION_ID)
                .then().assertThat().statusCode(200);
    }

    private void validateUpdateFormDataFailure(int errorCode) {
        Response response = SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().put(persistenceServiceUrl + "/formdata/" + SESSION_ID)
                .thenReturn();

        response.then().assertThat().statusCode(errorCode);
    }
}
