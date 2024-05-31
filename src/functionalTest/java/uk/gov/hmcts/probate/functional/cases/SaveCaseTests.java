package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(SerenityJUnit5Extension.class)
public class SaveCaseTests extends IntegrationTestBase {
    String gopCaseId;
    String intestacyCaseId;

    @BeforeEach
    public void init() {
        String gopCaseData = utils.getJsonFromFile("gop.singleExecutor.partial.json");
        gopCaseId = utils.createTestCase(gopCaseData);

        String intestacyCaseData = utils.getJsonFromFile("intestacy.partial.json");
        intestacyCaseId = utils.createTestCase(intestacyCaseData);
    }

    @Test
    public void saveSingleExecutorGopCaseReturns200() {
        String gopCaseData = utils.getJsonFromFile("gop.singleExecutor.full.json");

        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .body(gopCaseData)
            .when()
            .post("/cases/" + gopCaseId)
            .then()
            .assertThat()
            .statusCode(200)
            .body("caseData", notNullValue())
            .body("caseInfo.caseId", notNullValue())
            .body("caseInfo.state", equalTo("Pending"))
            .extract().jsonPath().prettify();
    }

    @Test
    public void saveMultipleExecutorGopCaseReturns200() {
        String gopCaseData = utils.getJsonFromFile("gop.multipleExecutors.full.json");

        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .body(gopCaseData)
            .when()
            .post("/cases/" + gopCaseId)
            .then()
            .assertThat()
            .statusCode(200)
            .body("caseData", notNullValue())
            .body("caseInfo.caseId", notNullValue())
            .body("caseInfo.state", equalTo("Pending"))
            .extract().jsonPath().prettify();
    }

    @Test
    public void saveIntestacyCaseReturns200() {
        String intestacyCaseData = utils.getJsonFromFile("intestacy.full.json");

        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .body(intestacyCaseData)
            .when()
            .post("/cases/" + intestacyCaseId)
            .then()
            .assertThat()
            .statusCode(200)
            .body("caseData", notNullValue())
            .body("caseInfo.caseId", notNullValue())
            .body("caseInfo.state", equalTo("Pending"))
            .extract().jsonPath().prettify();
    }

    @Test
    public void saveCasesWorkerIntestacyCaseReturns200() {
        String intestacyCaseData = utils.getJsonFromFile("intestacy.full.json");
        String applicationId = RandomStringUtils.randomNumeric(16).toLowerCase();
        intestacyCaseData = intestacyCaseData.replace("appId", applicationId);

        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .body(intestacyCaseData)
            .when()
            .post("/cases/caseworker/" + applicationId)
            .then()
            .assertThat()
            .body("caseData", notNullValue())
            .body("caseInfo.caseId", notNullValue())
            .body("caseInfo.state", equalTo("Pending"));
    }

    @Test
    public void saveCasesWorkerIntestacyCaseReturns403() {
        String intestacyCaseData = utils.getJsonFromFile("intestacy.full.json");
        String applicationId = RandomStringUtils.randomNumeric(16).toLowerCase();
        intestacyCaseData = intestacyCaseData.replace("appId", applicationId);

        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCaseworkerHeaders())
            .body(intestacyCaseData)
            .when()
            .post("/cases/caseworker/" + applicationId)
            .then()
            .assertThat()
            .statusCode(500);
    }

    @Test
    public void saveCaseWithInvalidDataReturns400() {
        String caseId = RandomStringUtils.randomNumeric(16).toLowerCase();

        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .body("")
            .when()
            .post("/cases/" + caseId)
            .then()
            .assertThat()
            .statusCode(400);
    }

    @Test
    public void initiateGopCaseReturns200() {
        String gopCaseData = utils.getJsonFromFile("gop.singleExecutor.partial.json");

        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .body(gopCaseData)
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
    public void initiateIntestacyCaseReturns200() {
        String intestacyCaseData = utils.getJsonFromFile("intestacy.partial.json");

        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .body(intestacyCaseData)
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
    public void initiateCaseWithInvalidDataReturns400() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .body("")
            .when()
            .post("/cases/initiate")
            .then()
            .assertThat()
            .statusCode(400);
    }
}
