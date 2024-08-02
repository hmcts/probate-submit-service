package uk.gov.hmcts.probate.services.submit.controllers.v2;

import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

@PactBroker(scheme = "${pact.broker.scheme}", host = "${pact.broker.baseUrl}", port = "${pact.broker.port}", tags = {
    "${pact.broker.consumer.tag}"})
@IgnoreNoPactsToVerify
public abstract class ControllerProviderTest {

    @Autowired
    ObjectMapper objectMapper;

    @Value("${pact.provider.version}")
    private String providerVersion;

    @BeforeEach
    public void setUpTest() {
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        System.getProperties().setProperty("pact.provider.version", providerVersion);
    }

    protected JSONObject createJsonObject(String fileName) throws JSONException, IOException {
        File file = getFile(fileName);
        String jsonString = new String(Files.readAllBytes(file.toPath()));
        return new JSONObject(jsonString);
    }

    protected ProbateCaseDetails getProbateCaseDetails(String fileName) throws JSONException, IOException {
        File file = getFile(fileName);
        ProbateCaseDetails probateCaseDetails = objectMapper.readValue(file, ProbateCaseDetails.class);
        return probateCaseDetails;
    }

    private File getFile(String fileName) throws FileNotFoundException {
        return ResourceUtils.getFile(this.getClass().getResource("/json/" + fileName));
    }
}
