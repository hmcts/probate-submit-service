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

    private String caseData;
    private String paymentInitiatedData;
    private String paymentSuccessData;

    private String caveatData;
    private String paymentCaveatData;

    @Before
    public void init() {
        if (!setUp) {
            caseData = utils.getJsonFromFile("gop.singleExecutor.partial.json");

            paymentInitiatedData = utils.getJsonFromFile("gop.paymentInitiated.json");
            paymentSuccessData = utils.getJsonFromFile("gop.singleExecutor.full.json");

            caveatData = utils.getJsonFromFile("caveat.partial.json");

            paymentCaveatData = utils.getJsonFromFile("caveat.full.json");

            setUp = true;
        }
    }

    @Test
    public void updatePendingCaseWithInitiatedPaymentReturns200() throws InterruptedException {
        String caseId = utils.createTestCase(caseData);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .body(paymentInitiatedData)
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


    @Test
    public void updatePendingCaseWithoutPaymentReturns400() throws InterruptedException {
        String caseId = utils.createTestCase(caseData);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .body(caseData)
                .when()
                .post("/payments/" + caseId + "/cases")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void updatePendingCaseWithSuccessfulPaymentReturns422() throws InterruptedException {
        String caseId = utils.createTestCase(caseData);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .body(paymentSuccessData)
                .when()
                .post("/payments/" + caseId + "/cases")
                .then()
                .assertThat()
                .statusCode(422);
    }

    @Test
    public void updatePAAppCreatedCaseWithSuccessfulPaymentReturns200() throws InterruptedException {
        String caseId = createPaymentInitiatedTestCase();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .body(paymentSuccessData)
                .when()
                .post("/payments/" + caseId + "/cases")
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("CaseCreated"))
                .extract().jsonPath().prettify();
    }

    @Test
    public void updatePAAppCreatedCaseWithoutPaymentReturns400() throws InterruptedException {
        String caseId = createPaymentInitiatedTestCase();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .body(caseData)
                .when()
                .post("/payments/" + caseId + "/cases")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void updatePAAppCreatedCaseWithInitiatedPaymentReturns422() throws InterruptedException {
        String caseId = createPaymentInitiatedTestCase();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .body(paymentInitiatedData)
                .when()
                .post("/payments/" + caseId + "/cases")
                .then()
                .assertThat()
                .statusCode(422);
    }

    @Test
    public void updatePAAppCreatedCaveatWithSuccessfulPaymentReturns200() throws InterruptedException {
        String caveatId = utils.createCaveatTestCase(caveatData);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCaseworkerHeaders())
                .body(paymentCaveatData)
                .when()
                .post("/ccd-case-update/" + caveatId)
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("CaveatRaised"))
                .extract().jsonPath().prettify();
    }


    @Test
    public void updatePAAppCreatedCaveatWithoutPaymentReturns500() throws InterruptedException {
        String caveatId = utils.createCaveatTestCase(caveatData);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCaseworkerHeaders())
                .body(caveatData)
                .when()
                .post("/ccd-case-update/" + caveatId)
                .then()
                .assertThat()
                .statusCode(500);
    }


    @Test
    public void updateCaveatAsCitizenReturns403() throws InterruptedException {
        String caveatId = utils.createCaveatTestCase(caveatData);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .body(paymentCaveatData)
                .when()
                .post("/ccd-case-update/" + caveatId)
                .then()
                .assertThat()
                .statusCode(403);
    }

    public String createPaymentInitiatedTestCase() throws InterruptedException {
        String caseId = utils.createTestCase(caseData);

        RestAssured.given()
                    .relaxedHTTPSValidation()
                    .headers(utils.getCitizenHeaders())
                    .body(paymentInitiatedData)
                    .when()
                    .post("/payments/" + caseId + "/cases");

            Thread.sleep(10000); // ensure CCD has time to update fully

        return caseId;
    }
}
