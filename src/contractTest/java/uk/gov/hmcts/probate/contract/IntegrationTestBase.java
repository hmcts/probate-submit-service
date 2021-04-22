package uk.gov.hmcts.probate.contract;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import net.thucydides.junit.spring.SpringIntegration;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.probate.contract.util.ContractTestUtils;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestContextConfiguration.class)
public abstract class IntegrationTestBase {

    @Rule
    public SpringIntegration springIntegration;
    @Autowired
    protected SolCcdServiceAuthTokenGenerator solCcdServiceAuthTokenGenerator;
    @Autowired
    protected ContractTestUtils contractTestUtils;
    private String solCcdServiceUrl;


    public IntegrationTestBase() {
        this.springIntegration = new SpringIntegration();

    }

    @Autowired
    public void solCcdServiceUrl(@Value("${ccd.data.store.api.url}") String solCcdServiceUrl) {
        this.solCcdServiceUrl = solCcdServiceUrl;
        RestAssured.baseURI = solCcdServiceUrl;
        RestAssured.defaultParser = Parser.JSON;
    }
}
