package uk.gov.hmcts.probate.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
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
    private static long SUBMISSION_REFERENCE = 123;
    private static boolean INITIALISED = false;

    @Before
    public void setUp() {
        if (INITIALISED) return;
        populateFormDataTable();
        INITIALISED = true;
    }

    @Test
    public void saveSubmissionSuccess() {
        validateSaveSubmissionSuccess();
    }

    @Test
    public void saveSubmissionFailure() {
        validateSaveSubmissionFailure("invalid_id", 400);
    }

    @Test
    public void loadSubmissionSuccess() {
        validateLoadSubmissionSuccess(submissionId);
    }

    @Test
    public void loadSubmissionFailure() {
        validateLoadSubmissionFailure(999, 404);
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
    public void loadFormDataBySubmissionReferenceSuccess() throws IOException {
        validateLoadFormDataBySubmissionReferenceSuccess();
    }

    @Test
    public void loadFormDataBySubmissionReferenceFailure() {
        validateLoadFormDataBySubmissionReferenceFailure(999, 404);
    }

    @Test
    public void updateFormDataSuccess() {
        validateUpdateFormDataSuccess();
    }

    @Test
    public void updateFormDataFailure() {
        validateUpdateFormDataFailure(400);
    }

    @Test
    public void getNextSequenceNumberSuccess() {
        validateSequenceNumberSuccess();
    }

    @Test
    public void getNextSequenceNumberFailure() {
        String errorMessage = "Registry not configured: dundee";
        validateSequenceNumberFailure("dundee", 404, errorMessage);
    }

    private void validateSaveSubmissionSuccess() {
        SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .body(utils.getJsonFromFile("submitData.json"))
                .when().get(persistenceServiceUrl + "/submissions/")
                .then().assertThat().statusCode(200);
    }

    private void validateSaveSubmissionFailure(String sessionId, int errorCode) {
        Response response = SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(sessionId))
                .when().post(persistenceServiceUrl + "/submissions")
                .thenReturn();

        response.then().assertThat().statusCode(errorCode);
    }

    private void validateLoadSubmissionSuccess(String submissionId) {
        SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(persistenceServiceUrl + "/submissions/" + submissionId)
                .then().assertThat().statusCode(200);
    }

    private void validateLoadSubmissionFailure(long sequenceId, int errorCode) {
        Response response = SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(persistenceServiceUrl + "/submissions/" + sequenceId)
                .thenReturn();

        response.then().assertThat().statusCode(errorCode);
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

    private void validateLoadFormDataBySubmissionReferenceSuccess() throws IOException {
        SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(persistenceServiceUrl + "/formdata/search/findBySubmissionReference?submissionReference=" + SUBMISSION_REFERENCE)
                .then().assertThat().statusCode(200);
    }

    private void validateLoadFormDataBySubmissionReferenceFailure(long submissionReference, int errorCode) {
        Response response = SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(persistenceServiceUrl + "/formdata/search/findBySubmissionReference?submissionReference=" + submissionReference)
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

    private void validateSequenceNumberSuccess() {
        SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(persistenceServiceUrl + "/sequence-number/oxford")
                .then().assertThat().statusCode(200);
    }

    private void validateSequenceNumberFailure(String registryName, int errorCode, String errorMsg) {
        Response response = SerenityRest.given().relaxedHTTPSValidation()
                .headers(utils.getHeaders(SESSION_ID))
                .when().get(persistenceServiceUrl + "/sequence-number/" + registryName)
                .thenReturn();

        response.then().assertThat().statusCode(errorCode)
                .and().body("error", equalTo("Not Found"))
                .and().body("message", equalTo(errorMsg));
    }
}
