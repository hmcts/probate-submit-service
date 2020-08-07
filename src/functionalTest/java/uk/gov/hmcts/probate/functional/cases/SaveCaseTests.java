package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringIntegrationSerenityRunner.class)
public class SaveCaseTests extends IntegrationTestBase {

    @Test
    public void shouldSuccessfullySaveIntestacyGrantOfRepresentationCaseUsingApplicationId() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile("intestacyGrantOfRepresentation_full.json"))
                .when()
                .post("/cases/" + "1234-1234-1234-1234")
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("Pending"))
                .extract().jsonPath().prettify();
    }
}
