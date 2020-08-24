package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringIntegrationSerenityRunner.class)
public class SaveCaseTests extends IntegrationTestBase {

    @Test
    public void saveCaseAsCitizenReturns200() {
        String caseData = utils.getJsonFromFile("success.saveCaseData.json");
        String caseId = RandomStringUtils.randomNumeric(16).toLowerCase();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(caseData)
                .when()
                .post("/cases/" + caseId)
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("Pending"))
                .extract().jsonPath().prettify();
    }

    @Test
    public void saveCaseAsCitizenWithInvalidDataReturns400() {
        String caseId = RandomStringUtils.randomNumeric(16).toLowerCase();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body("")
                .when()
                .post("/cases/" + caseId)
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void initiateCaseAsCitizenReturns200() {
        String caseData = utils.getJsonFromFile("success.saveCaseData.json");

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(caseData)
                .when()
                .post("/cases/initiate")
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("Pending"))
                .extract().jsonPath().prettify();
    }

    @Test
    public void initiateCaseAsCitizenWithInvalidDataReturns400() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body("")
                .when()
                .post("/cases/initiate")
                .then()
                .assertThat()
                .statusCode(400);
    }
}
