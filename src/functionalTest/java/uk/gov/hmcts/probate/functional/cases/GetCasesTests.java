package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;

@RunWith(SpringIntegrationSerenityRunner.class)
public class GetCasesTests extends IntegrationTestBase {

    @Value("${idam.username}")
    private String email;

    private static final String EMAIL_PLACEHOLDER = "XXXXXXXXXX";

    private String caseId;

    @Before
    public void setUpCases() {
        String caseData = utils.getJsonFromFile("intestacyGrantOfRepresentation_partial_draft.json");

        caseData = caseData.replace(EMAIL_PLACEHOLDER, email);
        Response response = RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(caseData)
                .when()
                .post("/cases/initiate");

        JsonPath jsonPath = JsonPath.from(response.getBody().asString());
        caseId = jsonPath.get("caseInfo.caseId");
    }

    @Test
    public void getCaseByApplicantEmailReturns200() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType",CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .get("/cases/applicantEmail/" + email)
                .then()
                .assertThat()
                .statusCode(200)
                .extract().jsonPath().prettify();
    }

    @Test
    public void getCaseByIdReturns200() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType",CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .get("/cases/" + caseId)
                .then()
                .assertThat()
                .statusCode(200)
                .extract().jsonPath().prettify();
    }
}
