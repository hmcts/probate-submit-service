package uk.gov.hmcts.probate.contract.ccdintegrationtests;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.probate.contract.IntegrationTestBase;

import java.io.IOException;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@ExtendWith(SerenityJUnit5Extension.class)
public class ProbatePaCcdIntegrationTests extends IntegrationTestBase {

    private static final String CCD_EVENT_TOKEN_FIELD = "event_token";
    private static final String APPLY_FOR_GRANT_TOKEN_URL_PATH = "/event-triggers/applyForGrant/token";
    private static final String CREATE_CASE_TOKEN_URL_PATH = "/cases/%s/event-triggers/createCase/token";
    private static final String PRIMARY_APPLICANT_EMAIL_ADDRESS_FIELD = "primaryApplicantEmailAddress";
    private static final String CASE_QUERY_PARAM_PREFIX = "case.";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void validatePostSuccessCcdCase() throws IOException {
        ObjectNode requestJson = getRequestJsonWithToken(APPLY_FOR_GRANT_TOKEN_URL_PATH, "success.pa.ccd.json");

        String caseJsonResponse = given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .body(requestJson)
            .when().post("/citizens/" + contractTestUtils.getUserId()
                + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
            .then()
            .statusCode(201).extract().body().asString();
        logger.info("validatePostSuccessCCDCase response: {}", caseJsonResponse);
    }

    @Test
    public void validateUpdatePaymentOnCcdCase() throws IOException {
        ObjectNode saveCaseRequestJson = getRequestJsonWithToken(APPLY_FOR_GRANT_TOKEN_URL_PATH,
            "success.pa.ccd.json");
        Long caseId = given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .body(saveCaseRequestJson.toString())
            .when().post("/citizens/" + contractTestUtils.getUserId()
                + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
            .then()
            .statusCode(201).extract().jsonPath().getLong("id");

        ObjectNode updatePaymentRequestJson =
            getRequestJsonWithToken(String.format(CREATE_CASE_TOKEN_URL_PATH, caseId.toString()),
                "success.pa.ccd.update.payment.json");
        String caseResponse = given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .body(updatePaymentRequestJson.toString())
            .when().post("/citizens/" + contractTestUtils.getUserId()
                + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases/" + caseId.toString() + "/events")
            .then()
            .statusCode(201).extract().body().asString();
        logger.info("validateUpdatePaymentOnCCDCase response: {}", caseResponse);
    }

    @Test
    public void validateGetCcdCaseWithQueryParameters() throws IOException {
        ObjectNode saveCaseRequestJson = getRequestJsonWithToken(APPLY_FOR_GRANT_TOKEN_URL_PATH,
            "success.pa.ccd.json");
        String email = RandomStringUtils.randomAlphanumeric(10);
        ObjectNode dataNode = (ObjectNode) saveCaseRequestJson.get("data");
        dataNode.put(PRIMARY_APPLICANT_EMAIL_ADDRESS_FIELD, email);

        given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .body(saveCaseRequestJson.toString())
            .when().post("/citizens/" + contractTestUtils.getUserId()
            + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
            .then()
            .statusCode(201);

        String responseJson = given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .when().get("/citizens/" + contractTestUtils.getUserId()
                + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases?"
                + CASE_QUERY_PARAM_PREFIX + PRIMARY_APPLICANT_EMAIL_ADDRESS_FIELD + "=" + email

            )
            .then()
            .body(".", hasSize(1))
            .body("[0].case_data." + PRIMARY_APPLICANT_EMAIL_ADDRESS_FIELD, is(equalTo(email)))
            .extract().body().asString();
        logger.info("validateGetCCDCaseWithQueryParameters response: {}", responseJson);
    }


    @Test
    public void verifyJurisdictionInTheSuccessResponse() throws IOException {
        ObjectNode requestJson = getRequestJsonWithToken(APPLY_FOR_GRANT_TOKEN_URL_PATH,
            "success.pa.ccd.json");

        given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .body(requestJson.toString())
            .when().post("/citizens/" + contractTestUtils.getUserId()
            + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
            .then()
            .statusCode(201).and().body("jurisdiction", equalToIgnoringCase("PROBATE"));

    }

    @Test
    public void verifyStateIsPresentInTheSuccessResponse() throws IOException {
        ObjectNode requestJson = getRequestJsonWithToken(APPLY_FOR_GRANT_TOKEN_URL_PATH,
            "success.pa.ccd.json");

        given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .body(requestJson.toString())
            .when().post("/citizens/" + contractTestUtils.getUserId()
            + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
            .then()
            .statusCode(201).and().body("state", equalToIgnoringCase("PaAppCreated"));

    }

    @Test
    public void verifyCaseTypeIdPresentInTheSuccessResponse() throws IOException {
        ObjectNode requestJson = getRequestJsonWithToken(APPLY_FOR_GRANT_TOKEN_URL_PATH,
            "success.pa.ccd.json");

        given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .body(requestJson.toString())
            .when().post("/citizens/" + contractTestUtils.getUserId()
            + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
            .then()
            .statusCode(201).and().body("case_type_id", equalToIgnoringCase("GrantOfRepresentation"));

    }


    @Test
    public void verifyCreatedDateIsPresentInTheSuccessResponse() throws IOException {
        ObjectNode requestJson = getRequestJsonWithToken(APPLY_FOR_GRANT_TOKEN_URL_PATH,
            "success.pa.ccd.json");

        given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .body(requestJson.toString())
            .when().post("/citizens/" + contractTestUtils.getUserId()
            + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
            .then()
            .statusCode(201).and().extract().body().asString().contains("created_date");
    }

    @Test
    public void verifyLastModifiedIsPresentInTheSuccessResponse() throws IOException {
        ObjectNode requestJson = getRequestJsonWithToken(APPLY_FOR_GRANT_TOKEN_URL_PATH,
            "success.pa.ccd.json");

        given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .body(requestJson.toString())
            .when().post("/citizens/" + contractTestUtils.getUserId()
            + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
            .then()
            .statusCode(201).and().extract().body().asString().contains("last_modified");
    }

    @Test
    public void verifyIdIsPresentInTheSuccessResponse() throws IOException {
        ObjectNode requestJson = getRequestJsonWithToken(APPLY_FOR_GRANT_TOKEN_URL_PATH,
            "success.pa.ccd.json");

        given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .body(requestJson.toString())
            .when().post("/citizens/" + contractTestUtils.getUserId()
            + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
            .then()
            .statusCode(201).and().extract().body().asString().contains("id");
    }


    @Test
    public void verifyCaseDataIsPresentInTheSuccessResponse() throws IOException {
        ObjectNode requestJson = getRequestJsonWithToken(APPLY_FOR_GRANT_TOKEN_URL_PATH,
            "success.pa.ccd.json");

        given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .body(requestJson.toString())
            .when().post("/citizens/" + contractTestUtils.getUserId()
            + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
            .then()
            .statusCode(201).and().extract().body().asString().contains("case_data");
    }

    @Test
    public void verifyDataClassificationIsPresentInTheSuccessResponse() throws IOException {
        ObjectNode requestJson = getRequestJsonWithToken(APPLY_FOR_GRANT_TOKEN_URL_PATH,
            "success.pa.ccd.json");

        given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .body(requestJson.toString())
            .when().post("/citizens/" + contractTestUtils.getUserId()
            + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
            .then()
            .statusCode(201).and().extract().body().asString().contains("data_classification");
    }

    @Test
    public void validateFailureWithInvalidCcdCasePayload() throws IOException {
        ObjectNode requestObjectNode = (ObjectNode) contractTestUtils.getJsonNodeFromFile("failure.pa.ccd.json");
        requestObjectNode.put(CCD_EVENT_TOKEN_FIELD, "abc");

        given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .body(requestObjectNode.toString())
            .when().post("/citizens/" + contractTestUtils.getUserId()
            + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases")
            .then().assertThat().statusCode(500);
    }


    private ObjectNode getRequestJsonWithToken(String tokenUrlPath, String jsonFileName) throws IOException {
        String token = generateEventToken(tokenUrlPath);
        ObjectNode objectNode = (ObjectNode) contractTestUtils.getJsonNodeFromFile(jsonFileName);
        objectNode.put(CCD_EVENT_TOKEN_FIELD, token);
        return objectNode;
    }

    private String generateEventToken(String url) {
        return given()
            .headers(contractTestUtils.getHeadersWithUserId())
            .when().get("/citizens/" + contractTestUtils.getUserId()
                + "/jurisdictions/PROBATE/case-types/GrantOfRepresentation" + url)
            .then().assertThat().statusCode(200).extract().path("token");
    }
}
