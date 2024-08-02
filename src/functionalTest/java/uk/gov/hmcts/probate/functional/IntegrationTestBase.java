package uk.gov.hmcts.probate.functional;

import io.restassured.RestAssured;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.probate.functional.util.TestUtils;

@ExtendWith({SerenityJUnit5Extension.class, SpringExtension.class})
@ContextConfiguration(classes = TestContextConfiguration.class)
public abstract class IntegrationTestBase {

    @Autowired
    protected TestUtils utils;

    @Autowired
    public void submitServiceConfiguration(@Value("${probate.submit.url}") String submitServiceUrl) {
        RestAssured.baseURI = submitServiceUrl;
    }
}