package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;
import uk.gov.hmcts.probate.functional.TestRetryRule;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringIntegrationSerenityRunner.class)
public class ValidateCasesTests extends IntegrationTestBase {

    @Rule
    public TestRetryRule retryRule = new TestRetryRule(3);

    private Boolean setUp = false;

    String testCaseId;
    String invalidCaseId;

    @Before
    public void init() {
        if (!setUp) {
            String caseData = utils.getJsonFromFile("gop.singleExecutor.partial.json");
            testCaseId = utils.createTestCase(caseData);

            setUp = true;
        }
    }

    @Test
    public void validateCaseReturns200() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .put("/cases/" + testCaseId + "/validations")
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
                .headers(utils.getCitizenHeaders())
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .put("/cases/" + randomCaseId + "/validations")
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void validateCaseWithInvalidDataReturns400() {
        if (retryRule.firstAttempt) {
            String invalidCaseData = utils.getJsonFromFile("intestacy.invalid.json");
            invalidCaseId = utils.createTestCase(invalidCaseData);
        }

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .put("/cases/" + invalidCaseId + "/validations")
                .then()
                .assertThat()
                .statusCode(404)
                .extract().jsonPath().prettify();
    }

    @Test
    public void validateCaseWithMissingCaseTypeReturns400() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .when()
                .put("/cases/" + testCaseId + "/validations")
                .then()
                .assertThat()
                .statusCode(400);
    }
}
