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

    private String gopCaseData;
    private String gopCaseId;
    private String caveatCaseData;
    private String caveatCaseId;

    @Before
    public void init() throws InterruptedException {
        if (!setUp) {
            gopCaseData = utils.getJsonFromFile("success.saveCaseData.json");
            gopCaseId = utils.createTestCase(gopCaseData);

            caveatCaseData = utils.getJsonFromFile("success.caveatPaymentDetails.json");
            caveatCaseId = utils.createCaveatTestCase(caveatCaseData);

            setUp = true;
        }
    }

    @Test
    public void updatePaymentDetailsReturns200() {
        String paymentCaseData = utils.getJsonFromFile("success.updatePaymentDetails.json");
        paymentCaseData = paymentCaseData.replace("1234123412341234", gopCaseId);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .body(paymentCaseData)
                .when()
                .post("/payments/" + gopCaseId + "/cases")
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
        gopCaseData = gopCaseData.replace("1234123412341234", gopCaseId);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .body(gopCaseData)
                .when()
                .post("/payments/" + gopCaseId + "/cases")
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
                .headers(utils.getCitizenHeaders())
                .body(paymentCaseData)
                .when()
                .post("/payments/" + randomCaseId + "/cases")
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void updateCaveatPaymentDetailsAsCaseworkerReturns200() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCaseworkerHeaders())
                .body(caveatCaseData)
                .when()
                .post("/ccd-case-update/" + caveatCaseId)
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("CaveatRaised"))
                .extract().jsonPath().prettify();
    }

    @Test
    public void updateCaveatPaymentDetailsAsCitizenReturns403() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .body(caveatCaseData)
                .when()
                .post("/ccd-case-update/" + caveatCaseId)
                .then()
                .assertThat()
                .statusCode(403);
    }

    @Test
    public void updateCaveatPaymentDetailsWithMissingDataReturns400() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCaseworkerHeaders())
                .body("")
                .when()
                .post("/ccd-case-update/" + caveatCaseId)
                .then()
                .assertThat()
                .statusCode(400);
    }
}
