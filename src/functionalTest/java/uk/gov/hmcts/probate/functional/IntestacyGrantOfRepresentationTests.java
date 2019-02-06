package uk.gov.hmcts.probate.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.parsing.Parser;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SerenityRunner.class)
@Ignore //NOSONAR
public class IntestacyGrantOfRepresentationTests extends IntegrationTestBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String EMAIL_PLACEHOLDER = "XXXXXXXXXX";
    private static final String PASSWORD = "Probate123";
    private static final String USER_GROUP_NAME = "probate-private-beta";

    private ObjectMapper objectMapper;

    private String email;

    private String caseId;

    @Before
    public void setUp() throws JsonProcessingException {
        RestAssured.defaultParser = Parser.JSON;
        objectMapper = new ObjectMapper();
        String forename = RandomStringUtils.randomAlphanumeric(5).toLowerCase();
        String surname = RandomStringUtils.randomAlphanumeric(5).toLowerCase();
        email = forename + "." + surname + "@email.com";
        logger.info("Generate user name: {}", email);

        IdamData idamData = IdamData.builder()
                .email(email)
                .forename(forename)
                .surname(surname)
                .password(PASSWORD)
                .roles(Arrays.asList(Role.builder().code("citizen").build()))
                .build();

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(Headers.headers(new Header("Content-Type", ContentType.JSON.toString())))
                .baseUri(idamUrl)
                .body(objectMapper.writeValueAsString(idamData))
                .when()
                .post("/testing-support/accounts")
                .then()
                .statusCode(204);

    }

    @Test
    public void shouldCreateDraftThenSubmitAndFinallyUpdatePayment() throws IOException {
        shouldCreateDraftSuccessfully();
        shouldUpdateFullDraftSuccessfully();
        shouldSubmitSuccessfully();
        shouldUpdatePaymentSuccessfully();
    }

    private void shouldCreateDraftSuccessfully() throws IOException {
        String draftJsonStr = utils.getJsonFromFile("intestacyGrantOfRepresentation_partial_draft.json");
        draftJsonStr = draftJsonStr.replace(EMAIL_PLACEHOLDER, email);

        String responseJsonStr = SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders(email, PASSWORD))
                .body(draftJsonStr)
                .when()
                .post(submitServiceUrl + "/drafts/" + email)
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("Draft"))
                .extract().jsonPath().prettify();
        Map<String, Object> actualJsonMap = objectMapper.readValue(responseJsonStr, Map.class);
        Map<String, String> caseInfo = (Map<String, String>) actualJsonMap.get("caseInfo");
        caseId = caseInfo.get("caseId");
    }

    private void shouldUpdateFullDraftSuccessfully() {
        String draftJsonStr = utils.getJsonFromFile("intestacyGrantOfRepresentation_full.json");
        draftJsonStr = draftJsonStr.replace(EMAIL_PLACEHOLDER, email);

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders(email, PASSWORD))
                .body(draftJsonStr)
                .when()
                .post(submitServiceUrl + "/drafts/" + email)
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", equalTo(caseId))
                .body("caseInfo.state", equalTo("Draft"));
    }

    private void shouldSubmitSuccessfully() {
        String submitJsonStr = utils.getJsonFromFile("intestacyGrantOfRepresentation_full.json");
        submitJsonStr = submitJsonStr.replace(EMAIL_PLACEHOLDER, email);

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders(email, PASSWORD))
                .body(submitJsonStr)
                .when()
                .post(submitServiceUrl + "/submissions/" + email)
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", equalTo(caseId))
                .body("caseInfo.state", equalTo("PAAppCreated"));
    }

    private void shouldUpdatePaymentSuccessfully() {
        String paymentJsonStr = utils.getJsonFromFile("intestacyGrantOfRepresentation_payment.json");

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders(email, PASSWORD))
                .body(paymentJsonStr)
                .when()
                .post(submitServiceUrl + "/payments/" + email)
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", equalTo(caseId))
                .body("caseInfo.state", equalTo("CaseCreated"));
    }
}
