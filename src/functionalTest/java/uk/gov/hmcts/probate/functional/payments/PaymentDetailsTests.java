package uk.gov.hmcts.probate.functional.payments;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

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
    public void updatePaymentDetailsReturns200() {

        String paymentCaseData = utils.getJsonFromFile("success.updatePaymentDetails.json");
        paymentCaseData = paymentCaseData.replace("1234123412341234", caseId);


        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(paymentCaseData)
                .when()
                .post("/payments/" + caseId + "/cases")
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("PAAppCreated"))
                .extract().jsonPath().prettify();
    }


}
