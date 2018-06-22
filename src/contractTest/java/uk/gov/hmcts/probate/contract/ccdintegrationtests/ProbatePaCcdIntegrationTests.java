package uk.gov.hmcts.probate.contract.ccdintegrationtests;

import static org.hamcrest.Matchers.equalToIgnoringCase;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.contract.IntegrationTestBase;

@RunWith(SerenityRunner.class)
public class ProbatePaCcdIntegrationTests extends IntegrationTestBase {

    String token;


    @Ignore
    public void validatePostSuccessCCDCase() {
        generateEventToken();

        String rep = contractTestUtils.getJsonFromFile("success.pa.ccd.json").replace("\"event_token\": \"sampletoken\"", "\"event_token\":\"" + token + "\"");

        SerenityRest.given()
                .headers(contractTestUtils.getHeadersWithUserId())
                .body(rep)
                .when().post("/citizens/" + contractTestUtils.getUserId() + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases").
                then()
                .statusCode(201);
    }


    @Ignore
    public void verifyJurisdictionInTheSuccessResponse() {
        generateEventToken();

        String rep = contractTestUtils.getJsonFromFile("success.pa.ccd.json").replace("\"event_token\": \"sampletoken\"", "\"event_token\":\"" + token + "\"");

        SerenityRest.given()
                .headers(contractTestUtils.getHeadersWithUserId())
                .body(rep)
                .when().post("/citizens/" + contractTestUtils.getUserId() + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
                .then()
                .statusCode(201).and().body("jurisdiction", equalToIgnoringCase("PROBATE"));

    }

    @Ignore
    public void verifyStateIsPresentInTheSuccessResponse() {
        generateEventToken();

        String rep = contractTestUtils.getJsonFromFile("success.pa.ccd.json").replace("\"event_token\": \"sampletoken\"", "\"event_token\":\"" + token + "\"");

        SerenityRest.given()
                .headers(contractTestUtils.getHeadersWithUserId())
                .body(rep)
                .when().post("/citizens/" + contractTestUtils.getUserId() + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
                .then()
                .statusCode(201).and().body("state", equalToIgnoringCase("CaseCreated"));

    }

    @Ignore
    public void verifyCaseTypeIDPresentInTheSuccessResponse() {
        generateEventToken();

        String rep = contractTestUtils.getJsonFromFile("success.pa.ccd.json").replace("\"event_token\": \"sampletoken\"", "\"event_token\":\"" + token + "\"");

        SerenityRest.given()
                .headers(contractTestUtils.getHeadersWithUserId())
                .body(rep)
                .when().post("/citizens/" + contractTestUtils.getUserId() + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
                .then()
                .statusCode(201).and().body("case_type_id", equalToIgnoringCase("GrantOfRepresentation"));

    }


    @Ignore
    public void verifySecurityClassificationIsPresentInTheSuccessResponse() {
        generateEventToken();

        String rep = contractTestUtils.getJsonFromFile("success.pa.ccd.json").replace("\"event_token\": \"sampletoken\"", "\"event_token\":\"" + token + "\"");

        SerenityRest.given()
                .headers(contractTestUtils.getHeadersWithUserId())
                .body(rep)
                .when().post("/citizens/" + contractTestUtils.getUserId() + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
                .then()
                .statusCode(201).and().body("security_classification", equalToIgnoringCase("PUBLIC"));

    }

    @Ignore
    public void verifyCreatedDateIsPresentInTheSuccessResponse() {
        generateEventToken();

        String rep = contractTestUtils.getJsonFromFile("success.pa.ccd.json").replace("\"event_token\": \"sampletoken\"", "\"event_token\":\"" + token + "\"");

        SerenityRest.given()
                .headers(contractTestUtils.getHeadersWithUserId())
                .body(rep)
                .when().post("/citizens/" + contractTestUtils.getUserId() + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
                .then()
                .statusCode(201).and().extract().body().asString().contains("created_date");
    }

    @Ignore
    public void verifyLastModifiedIsPresentInTheSuccessResponse() {
        generateEventToken();

        String rep = contractTestUtils.getJsonFromFile("success.pa.ccd.json").replace("\"event_token\": \"sampletoken\"", "\"event_token\":\"" + token + "\"");

        SerenityRest.given()
                .headers(contractTestUtils.getHeadersWithUserId())
                .body(rep)
                .when().post("/citizens/" + contractTestUtils.getUserId() + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
                .then()
                .statusCode(201).and().extract().body().asString().contains("last_modified");
    }

    @Ignore
    public void verifyIdIsPresentInTheSuccessResponse() {
        generateEventToken();

        String rep = contractTestUtils.getJsonFromFile("success.pa.ccd.json").replace("\"event_token\": \"sampletoken\"", "\"event_token\":\"" + token + "\"");

        SerenityRest.given()
                .headers(contractTestUtils.getHeadersWithUserId())
                .body(rep)
                .when().post("/citizens/" + contractTestUtils.getUserId() + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
                .then()
                .statusCode(201).and().extract().body().asString().contains("id");
    }


    @Ignore
    public void verifycaseDataIsPresentInTheSuccessResponse() {
        generateEventToken();

        String rep = contractTestUtils.getJsonFromFile("success.pa.ccd.json").replace("\"event_token\": \"sampletoken\"", "\"event_token\":\"" + token + "\"");

        SerenityRest.given()
                .headers(contractTestUtils.getHeadersWithUserId())
                .body(rep)
                .when().post("/citizens/" + contractTestUtils.getUserId() + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
                .then()
                .statusCode(201).and().extract().body().asString().contains("case_data");
    }

    @Ignore
    public void verifyDataClassificationIsPresentInTheSuccessResponse() {
        generateEventToken();

        String rep = contractTestUtils.getJsonFromFile("success.pa.ccd.json").replace("\"event_token\": \"sampletoken\"", "\"event_token\":\"" + token + "\"");

        SerenityRest.given()
                .headers(contractTestUtils.getHeadersWithUserId())
                .body(rep)
                .when().post("/citizens/" + contractTestUtils.getUserId() + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
                .then()
                .statusCode(201).and().extract().body().asString().contains("data_classification");
    }

    @Ignore
    public void validateFailureWithInvalidCCDCasePayload() {

        String rep = contractTestUtils.getJsonFromFile("failure.pa.ccd.json").replace("\"event_token\": \"sampletoken\"", "\"event_token\":\"abc\"");

        SerenityRest.given()
                .headers(contractTestUtils.getHeadersWithUserId())
                .body(rep)
                .when().post("/citizens/" + contractTestUtils.getUserId() + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
                .then().assertThat().statusCode(500);
    }

    private void generateEventToken() {
        token =
                SerenityRest.given()
                        .headers(contractTestUtils.getHeadersWithUserId())
                        .when().get("/citizens/" + contractTestUtils.getUserId() + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/event-triggers/applyForGrant/token")
                        .then().assertThat().statusCode(200).extract().path("token");
    }
}
