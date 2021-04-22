package uk.gov.hmcts.probate.services.submit.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.probate.model.cases.RegistryLocation;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RegistryServiceTest {

    private static final String CTSC_EMAIL = "ctsc@email.com";
    private static final String CTSC_ADDRESS = "Line 1 Ox\n"
        + "Line 2 Ox\n"
        + "Line 3 Ox\n"
        + "PostCode Ox\n";

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

    @Test
    public void shouldSetCardiffRegistryForWelshCase() {
        GrantOfRepresentationData grantOfRepresentationData =
            GrantOfRepresentationData.builder().languagePreferenceWelsh(Boolean.TRUE)
                .build();

        registryService.updateRegistry(grantOfRepresentationData);
        assertThat(grantOfRepresentationData.getRegistryLocation(), is(RegistryLocation.CARDIFF));
        assertThat(grantOfRepresentationData.getRegistryAddress(), is(CTSC_ADDRESS));
        assertThat(grantOfRepresentationData.getRegistryEmailAddress(), is(CTSC_EMAIL));

        CaveatData caveatData = CaveatData.builder().languagePreferenceWelsh(Boolean.TRUE)
            .build();
        registryService.updateRegistry(caveatData);

        assertThat(caveatData.getRegistryLocation(), is(RegistryLocation.CARDIFF));
    }

    @Test
    public void shouldSetCardiffRegistryForWelshCaseEvenIfRegistryPreviouslyPopulated() {
        GrantOfRepresentationData grantOfRepresentationData =
            GrantOfRepresentationData.builder().languagePreferenceWelsh(Boolean.TRUE)
                .registryLocation(RegistryLocation.CTSC).build();

        registryService.updateRegistry(grantOfRepresentationData);
        assertThat(grantOfRepresentationData.getRegistryLocation(), is(RegistryLocation.CARDIFF));
        assertThat(grantOfRepresentationData.getRegistryAddress(), is(CTSC_ADDRESS));
        assertThat(grantOfRepresentationData.getRegistryEmailAddress(), is(CTSC_EMAIL));

        CaveatData caveatData =
            CaveatData.builder().registryLocation(RegistryLocation.CTSC).languagePreferenceWelsh(Boolean.TRUE).build();
        registryService.updateRegistry(caveatData);

        assertThat(caveatData.getRegistryLocation(), is(RegistryLocation.CARDIFF));
    }

    @Test
    public void shouldNotChangeRegistryIfRegistryPreviouslyPopulatedAndLanguagePreferenceNotWelsh() {
        GrantOfRepresentationData grantOfRepresentationData =
            GrantOfRepresentationData.builder().languagePreferenceWelsh(Boolean.FALSE)
                .registryLocation(RegistryLocation.BIRMINGHAM).build();

        registryService.updateRegistry(grantOfRepresentationData);
        assertThat(grantOfRepresentationData.getRegistryLocation(), is(RegistryLocation.BIRMINGHAM));


        CaveatData caveatData =
            CaveatData.builder().registryLocation(RegistryLocation.MANCHESTER).languagePreferenceWelsh(Boolean.FALSE)
                .build();
        registryService.updateRegistry(caveatData);

        assertThat(caveatData.getRegistryLocation(), is(RegistryLocation.MANCHESTER));
    }

    @Test
    public void shouldSetCtcsRegistryForEnglishCaseIfPreviouslyCardiff() {
        GrantOfRepresentationData grantOfRepresentationData =
            GrantOfRepresentationData.builder().languagePreferenceWelsh(Boolean.FALSE)
                .registryLocation(RegistryLocation.CARDIFF)
                .build();

        registryService.updateRegistry(grantOfRepresentationData);
        assertThat(grantOfRepresentationData.getRegistryLocation(), is(RegistryLocation.CTSC));
        assertThat(grantOfRepresentationData.getRegistryAddress(), is(CTSC_ADDRESS));
        assertThat(grantOfRepresentationData.getRegistryEmailAddress(), is(CTSC_EMAIL));

        CaveatData caveatData =
            CaveatData.builder().registryLocation(RegistryLocation.CARDIFF).languagePreferenceWelsh(Boolean.FALSE)
                .build();
        registryService.updateRegistry(caveatData);

        assertThat(caveatData.getRegistryLocation(), is(RegistryLocation.CTSC));
    }

}
