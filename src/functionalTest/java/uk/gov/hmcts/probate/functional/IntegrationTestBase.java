package uk.gov.hmcts.probate.functional;

import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import io.restassured.RestAssured;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.probate.functional.util.TestUtils;

@RunWith(SpringIntegrationSerenityRunner.class)
@ContextConfiguration(classes = TestContextConfiguration.class)
@Ignore
public class IntegrationTestBase {

    @Autowired
    protected TestUtils utils;

    protected String submitServiceUrl;

    protected String idamUrl;

    @Rule
    public SpringIntegrationMethodRule springIntegration;

    public IntegrationTestBase() {
        this.springIntegration = new SpringIntegrationMethodRule();
    }

    @Autowired
    public void submitServiceConfiguration(@Value("${probate.submit.url}") String submitServiceUrl,
                                           @Value("${user.auth.provider.oauth2.url}") String idamUrl) {
        this.submitServiceUrl = submitServiceUrl;
        this.idamUrl = idamUrl;

        RestAssured.baseURI = submitServiceUrl;
    }
}