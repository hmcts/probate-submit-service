package uk.gov.hmcts.probate.functional.payments;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;
import uk.gov.hmcts.probate.functional.TestRetryRule;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringIntegrationSerenityRunner.class)
public class GrantOfRepresentationPaymentTests extends IntegrationTestBase {

    @Rule
    public TestRetryRule retryRule = new TestRetryRule(3);

    private Boolean setUp = false;

    private String caseData;
    private String paymentInitiatedData;
    private String paymentSuccessData;

    private String caseId;

    @Before
    public void init() {
        if (!setUp) {
            caseData = utils.getJsonFromFile("gop.singleExecutor.partial.json");

            paymentInitiatedData = utils.getJsonFromFile("gop.paymentInitiated.json");
            paymentSuccessData = utils.getJsonFromFile("gop.singleExecutor.full.json");

            setUp = true;
        }

        if (retryRule.firstAttempt) {
            caseId = utils.createTestCase(caseData);
        }
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
            .body("caseInfo.state", equalTo("CaseCreated"))
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
