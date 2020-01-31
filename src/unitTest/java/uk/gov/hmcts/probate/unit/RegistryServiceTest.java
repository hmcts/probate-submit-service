package uk.gov.hmcts.probate.unit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.probate.services.submit.core.RegistryService;
import uk.gov.hmcts.reform.probate.model.cases.RegistryLocation;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RegistryServiceTest {

    private static final String CTSC_EMAIL = "ctsc@email.com";
    private static final String CTSC_ADDRESS = "Line 1 Ox\n" +
        "Line 2 Ox\n" +
        "Line 3 Ox\n" +
        "PostCode Ox\n";

    @Autowired
    private RegistryService registryService;

    @Test
    public void shouldGetRegistry() {
        GrantOfRepresentationData grantOfRepresentationData = GrantOfRepresentationData.builder()
            .build();

        registryService.updateRegistry(grantOfRepresentationData);
        assertThat(grantOfRepresentationData.getRegistryLocation(), is(RegistryLocation.CTSC));
        assertThat(grantOfRepresentationData.getRegistryAddress(), is(CTSC_ADDRESS));
        assertThat(grantOfRepresentationData.getRegistryEmailAddress(), is(CTSC_EMAIL));

        CaveatData caveatData = CaveatData.builder()
            .build();
        registryService.updateRegistry(caveatData);

        assertThat(caveatData.getRegistryLocation(), is(RegistryLocation.CTSC));
    }


}
