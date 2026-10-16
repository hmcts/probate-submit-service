package uk.gov.hmcts.probate.functional.submissions;

import io.restassured.RestAssured;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class CaseSubmissionsTest extends IntegrationTestBase {

    static final String APPLICATION_ID = "appId";

    @Test
    void submitCaveatsCaseReturns200() {
        String caseData = utils.getJsonFromFile("caveat.partial.json");
        String applicationId = RandomStringUtils.secure().nextNumeric(16).toLowerCase();

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
    void submitCaveatsCaseWithInvalidDataReturns400() {
        String applicationId = RandomStringUtils.secure().nextNumeric(16).toLowerCase();

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
