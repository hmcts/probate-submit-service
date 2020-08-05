package uk.gov.hmcts.probate.functional.cases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.parsing.Parser;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.probate.functional.model.IdamData;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;
import uk.gov.hmcts.probate.functional.model.Role;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringIntegrationSerenityRunner.class)
public class SaveCaseTests extends IntegrationTestBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String EMAIL_PLACEHOLDER = "XXXXXXXXXX";
    private static final String PASSWORD = "Probate123";

    private ObjectMapper objectMapper;
    private String email;

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

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(Headers.headers(new Header("Content-Type", ContentType.JSON.toString())))
                .baseUri(idamUrl)
                .body(objectMapper.writeValueAsString(idamData))
                .when()
                .post("/testing-support/accounts")
                .then()
                .statusCode(201);
    }

    @Test
    public void shouldSuccessfullySaveIntestacyGrantOfRepresentationCaseUsingApplicationId() {
        String applicationId = "id";

        String probateCaseDetails = utils.getJsonFromFile("intestacyGrantOfRepresentation_full.json");
        probateCaseDetails = probateCaseDetails.replace(EMAIL_PLACEHOLDER, email);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders(email, PASSWORD))
                .body(probateCaseDetails)
                .when()
                .post("/cases/" + applicationId)
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("Pending"))
                .extract().jsonPath().prettify();
    }
}
