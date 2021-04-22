package uk.gov.hmcts.probate.functional.payments;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CaveatsPaymentTests extends IntegrationTestBase {

    private Boolean setUp = false;

    private String caveatData;
    private String paymentCaveatData;

    private String caveatId;

    @Before
    public void init() {
        if (!setUp) {
            caveatData = utils.getJsonFromFile("caveat.partial.json");
            paymentCaveatData = utils.getJsonFromFile("caveat.full.json");

            setUp = true;
        }

        caveatId = utils.createCaveatTestCase(caveatData);

    }

    @Test
    public void updatePaAppCreatedCaveatWithSuccessfulPaymentReturns200() {
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
    public void updatePaAppCreatedCaveatWithoutPaymentReturns500() {
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
    public void updateCaveatAsCitizenReturns403() {
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
}
