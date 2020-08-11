package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringIntegrationSerenityRunner.class)
public class AccessCasesTests extends IntegrationTestBase {

    @Test
    public void grantCaseAccessToUserReturns200() {
//        RestAssured.given()
//                .relaxedHTTPSValidation()
//                .headers(utils.getHeaders())
//                .when()
//                .post("/cases/1234123412341234/caseworker/grantaccess/applicant/megan@test.com")
//                .then()
//                .assertThat()
//                .statusCode(200)
//                .body("caseData", notNullValue())
//                .body("caseInfo.caseId", notNullValue())
//                .body("caseInfo.state", equalTo("Pending"))
//                .extract().jsonPath().prettify();
    }
}
