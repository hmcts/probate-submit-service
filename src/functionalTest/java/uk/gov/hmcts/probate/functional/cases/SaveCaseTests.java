package uk.gov.hmcts.probate.functional.cases;

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
public class SaveCaseTests extends IntegrationTestBase {

    private Boolean setUp = false;

    String gopCaseId;
    String intestacyCaseId;

    @Before
    public void init() throws InterruptedException {
        if (!setUp) {
            String gopCaseData = utils.getJsonFromFile("gop.singleExecutor.partial.json");
            gopCaseId = utils.createTestCase(gopCaseData);

            String intestacyCaseData =  utils.getJsonFromFile("intestacy.partial.json");
            intestacyCaseId = utils.createTestCase(intestacyCaseData);

            setUp = true;
        }
    }

    @Test
    public void saveGOPCaseReturns200() {
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
    public void initiateGOPCaseReturns200() {
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
