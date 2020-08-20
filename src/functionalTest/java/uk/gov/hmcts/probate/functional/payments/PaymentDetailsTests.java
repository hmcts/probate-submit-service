package uk.gov.hmcts.probate.functional.payments;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;

import static junit.framework.TestCase.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
public class PaymentDetailsTests extends IntegrationTestBase {

    private Boolean setUp = false;

    private String caseId;

    @Before
    public void init() {
        if (!setUp) {
            String caseData = utils.getJsonFromFile("success.saveCaseData.json");
            caseId = utils.createTestCase(caseData);

            setUp = true;
        }
    }

    @Test
    public void updatePaymentDetailsReturns200() throws InterruptedException {
        String paymentCaseData = utils.getJsonFromFile("success.updatePaymentDetails.json");
        paymentCaseData = paymentCaseData.replace("1234123412341234", caseId);

        int statusCode = 0;
        for (int i = 5; i > 0 && statusCode != 200; i--) {
            Response response = RestAssured.given()
                    .relaxedHTTPSValidation()
                    .headers(utils.getHeaders())
                    .body(paymentCaseData)
                    .when()
                    .post("/payments/" + caseId + "/cases");
            statusCode = response.getStatusCode();
            Thread.sleep(1000);
        }

        assertEquals(200, statusCode);
    }


}
