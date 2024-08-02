package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;

import java.util.Date;

@ExtendWith(SerenityJUnit5Extension.class)
public class ExpireCaveatsTests extends IntegrationTestBase {

    @Test
    public void expireCaveatByDateReturns200() {
        Date currentDate = new Date();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .queryParam("expiryDate", currentDate)
                .when()
                .get("/cases/caveats/expire")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void expireCaveatMissingDateReturns400() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getCitizenHeaders())
                .when()
                .get("/cases/caveats/expire")
                .then()
                .assertThat()
                .statusCode(400);
    }
}
