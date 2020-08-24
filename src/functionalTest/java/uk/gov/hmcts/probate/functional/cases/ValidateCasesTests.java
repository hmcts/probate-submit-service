package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringIntegrationSerenityRunner.class)
public class ValidateCasesTests extends IntegrationTestBase {

    @Test
    public void validateCaseReturns200() {
        String caseId = utils.getTestCaseId();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .put("/cases/" + caseId + "/validations")
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("Pending"))
                .extract().jsonPath().prettify();
    }

    @Test
    public void validateCaseIncorrectIdReturns404() {
        String randomCaseId = RandomStringUtils.randomNumeric(16).toLowerCase();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .put("/cases/" + randomCaseId + "/validations")
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void validateCaseWithInvalidDataReturns400() throws InterruptedException {
        String invalidCaseData = utils.getJsonFromFile("failure.validateCaseData.json");
        String invalidCaseId = utils.createTestCase(invalidCaseData);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .put("/cases/" + invalidCaseId + "/validations")
                .then()
                .assertThat()
                .statusCode(400)
                .extract().jsonPath().prettify();
    }

    @Test
    public void validateCaseWithMissingCaseTypeReturns400() throws InterruptedException {
        String caseId = utils.getTestCaseId();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .when()
                .put("/cases/" + caseId + "/validations")
                .then()
                .assertThat()
                .statusCode(400);
    }
}
