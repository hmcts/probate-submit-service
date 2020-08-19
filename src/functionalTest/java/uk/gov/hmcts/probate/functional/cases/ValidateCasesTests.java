package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;

import static junit.framework.TestCase.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
public class ValidateCasesTests extends IntegrationTestBase {

    private Boolean setUp = false;

    private String validCaseId;
    private String invalidCaseId;

    @Before
    public void init() {
        if (!setUp) {
            String validCaseData = utils.getJsonFromFile("success.validateCaseData.json");
            validCaseId = utils.createTestCase(validCaseData);

            String invalidCaseData = utils.getJsonFromFile("failure.validateCaseData.json");
            invalidCaseId = utils.createTestCase(invalidCaseData);

            setUp = true;
        }
    }

    @Test
    public void validateCaseReturns200() throws InterruptedException {
        int statusCode = 0;

        for (int i = 5; i > 0 && statusCode != 200; i--) {
            Response response = RestAssured.given()
                    .relaxedHTTPSValidation()
                    .headers(utils.getHeaders())
                    .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                    .when()
                    .put("/cases/" + validCaseId + "/validations");
            statusCode = response.getStatusCode();
            Thread.sleep(1000);
        }

        assertEquals(200, statusCode);
    }

    @Test
    public void validateCaseReturns400() throws InterruptedException {
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
