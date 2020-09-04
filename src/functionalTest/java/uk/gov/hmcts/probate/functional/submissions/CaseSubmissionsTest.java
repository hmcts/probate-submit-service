package uk.gov.hmcts.probate.functional.submissions;

import io.restassured.RestAssured;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CaseSubmissionsTest extends IntegrationTestBase {

    public static final String APPLICATION_ID = "appId";

    @Test
    public void submitCaveatsCaseReturns200() {
        String caseData = utils.getJsonFromFile("caveat.partial.json");
        String applicationId = RandomStringUtils.randomNumeric(16).toLowerCase();

        caseData = caseData.replace(APPLICATION_ID, applicationId);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .body(caseData)
                .when()
                .post("/submissions/" + applicationId)
                .then()
                .assertThat()
                .statusCode(200)
                .body("probateCaseDetails.caseData", notNullValue())
                .body("probateCaseDetails.caseInfo.caseId", notNullValue())
                .body("probateCaseDetails.caseInfo.state", equalTo("PAAppCreated"))
                .extract().jsonPath().prettify();
    }

    @Test
    public void submitCaveatsCaseWithInvalidDataReturns400() {
        String applicationId = RandomStringUtils.randomNumeric(16).toLowerCase();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .body("")
                .when()
                .post("/submissions/" + applicationId)
                .then()
                .assertThat()
                .statusCode(400);
    }
}
