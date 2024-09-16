package uk.gov.hmcts.probate.functional.payments;

import io.restassured.RestAssured;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SerenityJUnit5Extension.class)
public class GrantOfRepresentationPaymentTests extends IntegrationTestBase {

    private String caseData;
    private String paymentInitiatedData;
    private String paymentSuccessData;

    private String caseId;
    private static final int SLEEP_TIME = 2000;

    @BeforeAll
    public void init() throws Exception {
        caseData = utils.getJsonFromFile("gop.singleExecutor.partial.json");

        paymentInitiatedData = utils.getJsonFromFile("gop.paymentInitiated.json");
        paymentSuccessData = utils.getJsonFromFile("gop.singleExecutor.full.json");

        caseId = utils.createTestCase(caseData);
        Thread.sleep(SLEEP_TIME);
    }

    @Test
    public void updatePendingCaseWithInitiatedPaymentReturns200() {
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
    public void updatePendingCaseWithoutPaymentReturns400() {
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
    public void updatePendingCaseWithSuccessfulPaymentReturns422() {
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
    public void updatePaAppCreatedCaseWithSuccessfulPaymentReturns200() throws InterruptedException {
        initiatePayment();

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
            .body("caseInfo.state", equalTo("CasePrinted"))
            .extract().jsonPath().prettify();
    }

    @Test
    public void updatePaAppCreatedCaseWithoutPaymentReturns400() throws InterruptedException {
        initiatePayment();

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
    public void updatePaAppCreatedCaseWithInitiatedPaymentReturns422() {
        initiatePayment();

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


    public void initiatePayment() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .body(paymentInitiatedData)
            .when()
            .post("/payments/" + caseId + "/cases");
    }
}
