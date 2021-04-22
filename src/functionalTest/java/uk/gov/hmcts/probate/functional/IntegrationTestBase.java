package uk.gov.hmcts.probate.functional;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.probate.functional.util.TestUtils;

@RunWith(SpringIntegrationSerenityRunner.class)
@ContextConfiguration(classes = TestContextConfiguration.class)
public abstract class IntegrationTestBase {

    @Autowired
    protected TestUtils utils;

    @Rule
    public SpringIntegrationMethodRule springIntegration;

    public IntegrationTestBase() {
        this.springIntegration = new SpringIntegrationMethodRule();
    }

    @Autowired
    public void submitServiceConfiguration(@Value("${probate.submit.url}") String submitServiceUrl) {
        RestAssured.baseURI = submitServiceUrl;
    }
}