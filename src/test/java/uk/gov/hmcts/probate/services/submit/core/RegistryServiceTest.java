package uk.gov.hmcts.probate.services.submit.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.probate.model.cases.RegistryLocation;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
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
        assertEquals(RegistryLocation.CTSC, grantOfRepresentationData.getRegistryLocation());
        assertEquals(CTSC_ADDRESS, grantOfRepresentationData.getRegistryAddress());
        assertEquals(CTSC_EMAIL, grantOfRepresentationData.getRegistryEmailAddress());

        CaveatData caveatData = CaveatData.builder()
            .build();
        registryService.updateRegistry(caveatData);

        assertEquals(RegistryLocation.CTSC, caveatData.getRegistryLocation());
    }

    @Test
    public void shouldSetCardiffRegistryForWelshCase() {
        GrantOfRepresentationData grantOfRepresentationData =
            GrantOfRepresentationData.builder().languagePreferenceWelsh(Boolean.TRUE)
                .build();

        registryService.updateRegistry(grantOfRepresentationData);
        assertEquals(RegistryLocation.CARDIFF, grantOfRepresentationData.getRegistryLocation());
        assertEquals(CTSC_ADDRESS, grantOfRepresentationData.getRegistryAddress());
        assertEquals(CTSC_EMAIL, grantOfRepresentationData.getRegistryEmailAddress());

        CaveatData caveatData = CaveatData.builder().languagePreferenceWelsh(Boolean.TRUE)
            .build();
        registryService.updateRegistry(caveatData);

        assertEquals(RegistryLocation.CARDIFF, caveatData.getRegistryLocation());
    }

    @Test
    public void shouldSetCardiffRegistryForWelshCaseEvenIfRegistryPreviouslyPopulated() {
        GrantOfRepresentationData grantOfRepresentationData =
            GrantOfRepresentationData.builder().languagePreferenceWelsh(Boolean.TRUE)
                .registryLocation(RegistryLocation.CTSC).build();

        registryService.updateRegistry(grantOfRepresentationData);
        assertEquals(RegistryLocation.CARDIFF, grantOfRepresentationData.getRegistryLocation());
        assertEquals(CTSC_ADDRESS, grantOfRepresentationData.getRegistryAddress());
        assertEquals(CTSC_EMAIL, grantOfRepresentationData.getRegistryEmailAddress());

        CaveatData caveatData =
            CaveatData.builder().registryLocation(RegistryLocation.CTSC).languagePreferenceWelsh(Boolean.TRUE).build();
        registryService.updateRegistry(caveatData);

        assertEquals(RegistryLocation.CARDIFF, caveatData.getRegistryLocation());
    }

    @Test
    public void shouldNotChangeRegistryIfRegistryPreviouslyPopulatedAndLanguagePreferenceNotWelsh() {
        GrantOfRepresentationData grantOfRepresentationData =
            GrantOfRepresentationData.builder().languagePreferenceWelsh(Boolean.FALSE)
                .registryLocation(RegistryLocation.BIRMINGHAM).build();

        registryService.updateRegistry(grantOfRepresentationData);
        assertEquals(RegistryLocation.BIRMINGHAM, grantOfRepresentationData.getRegistryLocation());


        CaveatData caveatData =
            CaveatData.builder().registryLocation(RegistryLocation.MANCHESTER).languagePreferenceWelsh(Boolean.FALSE)
                .build();
        registryService.updateRegistry(caveatData);

        assertEquals(RegistryLocation.MANCHESTER, caveatData.getRegistryLocation());
    }

    @Test
    public void shouldSetCtcsRegistryForEnglishCaseIfPreviouslyCardiff() {
        GrantOfRepresentationData grantOfRepresentationData =
            GrantOfRepresentationData.builder().languagePreferenceWelsh(Boolean.FALSE)
                .registryLocation(RegistryLocation.CARDIFF)
                .build();

        registryService.updateRegistry(grantOfRepresentationData);
        assertEquals(RegistryLocation.CTSC, grantOfRepresentationData.getRegistryLocation());
        assertEquals(CTSC_ADDRESS, grantOfRepresentationData.getRegistryAddress());
        assertEquals(CTSC_EMAIL, grantOfRepresentationData.getRegistryEmailAddress());

        CaveatData caveatData =
            CaveatData.builder().registryLocation(RegistryLocation.CARDIFF).languagePreferenceWelsh(Boolean.FALSE)
                .build();
        registryService.updateRegistry(caveatData);

        assertEquals(RegistryLocation.CTSC, caveatData.getRegistryLocation());
    }

}
