package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;

import static junit.framework.TestCase.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
public class ValidateCasesTests extends IntegrationTestBase {

    @Test
    public void validateCaseReturns200() throws InterruptedException {
        int statusCode = 0;
        String caseId = utils.getTestCaseId();

        for (int i = 5; i > 0 && statusCode != 200; i--) {
            Response response = RestAssured.given()
                    .relaxedHTTPSValidation()
                    .headers(utils.getHeaders())
                    .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                    .when()
                    .put("/cases/" + caseId + "/validations");
            statusCode = response.getStatusCode();
            Thread.sleep(1000);
        }

        assertEquals(200, statusCode);
    }

    @Test
    public void validateCaseReturns400() throws InterruptedException {
        String invalidCaseData = utils.getJsonFromFile("failure.validateCaseData.json");
        String invalidCaseId = utils.createTestCase(invalidCaseData);

        int statusCode = 0;
        for (int i = 5; i > 0 && statusCode != 400; i--) {
            Response response = RestAssured.given()
                    .relaxedHTTPSValidation()
                    .headers(utils.getHeaders())
                    .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                    .when()
                    .put("/cases/" + invalidCaseId + "/validations");
            statusCode = response.getStatusCode();
            Thread.sleep(1000);
        }

        assertEquals(400, statusCode);
    }
}
