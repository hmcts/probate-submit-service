package uk.gov.hmcts.probate.functional.payments;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringIntegrationSerenityRunner.class)
public class PaymentDetailsTests extends IntegrationTestBase {

    private Boolean setUp = false;

    String testCaseId;

    @Before
    public void init() throws InterruptedException {
        if (!setUp) {
            String caseData = utils.getJsonFromFile("success.saveCaseData.json");
            testCaseId = utils.createTestCase(caseData);

            setUp = true;
        }
    }

    @Test
    public void updatePaymentDetailsReturns200() {
        String paymentCaseData = utils.getJsonFromFile("success.updatePaymentDetails.json");
        paymentCaseData = paymentCaseData.replace("1234123412341234", testCaseId);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(paymentCaseData)
                .when()
                .post("/payments/" + testCaseId + "/cases")
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("PAAppCreated"))
                .extract().jsonPath().prettify();
    }

    @Test
    public void updatePaymentDetailsWithIncorrectDataReturns400() {
        String caseDataWithoutPaymentInfo = utils.getJsonFromFile("success.saveCaseData.json");
        caseDataWithoutPaymentInfo = caseDataWithoutPaymentInfo.replace("1234123412341234", testCaseId);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(caseDataWithoutPaymentInfo)
                .when()
                .post("/payments/" + testCaseId + "/cases")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void updatePaymentDetailsWithIncorrectIdReturns404() {
        String randomCaseId = RandomStringUtils.randomNumeric(16).toLowerCase();

        String paymentCaseData = utils.getJsonFromFile("success.updatePaymentDetails.json");
        paymentCaseData = paymentCaseData.replace("1234123412341234", randomCaseId);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(paymentCaseData)
                .when()
                .post("/payments/" + randomCaseId + "/cases")
                .then()
                .assertThat()
                .statusCode(404);
    }
}
