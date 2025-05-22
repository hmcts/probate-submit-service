package uk.gov.hmcts.probate.functional.payments;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.BeforeEach;
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
    private String lastModifiedDateTime;
    private static final int SLEEP_TIME = 2000;
    private static final String DUMMY_DATE_2099 = "2099-01-01T12:12:12.123";
    private static final String DUMMY_DATE_2019 = "2019-01-01T12:12:12.123";

    @BeforeEach
    void init() throws InterruptedException {
        caseData = utils.getJsonFromFile("gop.singleExecutor.partial.json");
        paymentInitiatedData = utils.getJsonFromFile("gop.paymentInitiated.json");
        paymentSuccessData = utils.getJsonFromFile("gop.singleExecutor.full.json");

        JsonPath gopCase = utils.createCaseAndExtractJson(caseData);
        caseId = gopCase.getString("caseInfo.caseId");
        lastModifiedDateTime = gopCase.getString("caseInfo.lastModifiedDateTime");
        Thread.sleep(SLEEP_TIME);
    }

    @Test
    void updatePendingCaseWithInitiatedPaymentReturns200() {
        paymentInitiatedData = paymentInitiatedData.replace(DUMMY_DATE_2099, lastModifiedDateTime);

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
    void updatePendingCaseWithoutPaymentReturns400() {
        caseData = caseData.replace(DUMMY_DATE_2019, lastModifiedDateTime);

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
    void updatePendingCaseWithSuccessfulPaymentReturns422() {
        paymentSuccessData = paymentSuccessData.replace(DUMMY_DATE_2099, lastModifiedDateTime);

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
    void updatePaAppCreatedCaseWithSuccessfulPaymentReturns200() throws InterruptedException {
        initiatePayment();
        paymentSuccessData = paymentSuccessData.replace(DUMMY_DATE_2099, lastModifiedDateTime);

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
        caseData = caseData.replace(DUMMY_DATE_2019, lastModifiedDateTime);

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
    void updatePaAppCreatedCaseWithInitiatedPaymentReturns422() {
        initiatePayment();
        paymentInitiatedData = paymentInitiatedData.replace(DUMMY_DATE_2099, lastModifiedDateTime);

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

    void initiatePayment() {
        paymentInitiatedData = paymentInitiatedData.replace(DUMMY_DATE_2099, lastModifiedDateTime);

        JsonPath gopCase = RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .body(paymentInitiatedData)
            .when()
            .post("/payments/" + caseId + "/cases")
            .then()
            .extract().jsonPath();
        lastModifiedDateTime = gopCase.getString("caseInfo.lastModifiedDateTime");
    }
}
