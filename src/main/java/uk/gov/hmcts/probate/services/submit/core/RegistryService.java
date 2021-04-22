package uk.gov.hmcts.probate.services.submit.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.probate.services.submit.model.v2.Registry;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.RegistryLocation;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class RegistryService {

    private final List<Registry> registries;

    private int registryCounter = 1;

    public void updateRegistry(CaseData caseData) {
        Registry nextRegistry = getNextRegistry();
        log.info("Updating with registry name: {} id: {}", nextRegistry.getName(), nextRegistry.getId());
        if (CaseType.getCaseType(caseData).equals(CaseType.CAVEAT)) {
            CaveatData caveatData = (CaveatData) caseData;
            determineRegistryLocationName(nextRegistry, caveatData);
        }
        if (CaseType.getCaseType(caseData).equals(CaseType.GRANT_OF_REPRESENTATION)) {
            GrantOfRepresentationData grantOfRepresentationData = (GrantOfRepresentationData) caseData;
            determineRegistryLocationName(nextRegistry, grantOfRepresentationData);
            grantOfRepresentationData.setRegistryAddress(nextRegistry.getAddress());
            grantOfRepresentationData.setRegistryEmailAddress(nextRegistry.getEmail());
            grantOfRepresentationData.setRegistrySequenceNumber(new Long(registryCounter));
        }
    }

    private void determineRegistryLocationName(Registry nextRegistry,
                                               GrantOfRepresentationData grantOfRepresentationData) {
        if (grantOfRepresentationData.getLanguagePreferenceWelsh() != null && grantOfRepresentationData
            .getLanguagePreferenceWelsh()) {
            grantOfRepresentationData.setRegistryLocation(RegistryLocation.CARDIFF);
        } else if (grantOfRepresentationData.getRegistryLocation() == null || (
            grantOfRepresentationData.getLanguagePreferenceWelsh() != null && !grantOfRepresentationData
                .getLanguagePreferenceWelsh() && grantOfRepresentationData.getRegistryLocation()
                .equals(RegistryLocation.CARDIFF))) {
            grantOfRepresentationData
                .setRegistryLocation(RegistryLocation.findRegistryLocationByName(nextRegistry.getName()));
        }
    }

    private void determineRegistryLocationName(Registry nextRegistry, CaveatData caveatData) {
        if (caveatData.getLanguagePreferenceWelsh() != null && caveatData.getLanguagePreferenceWelsh()) {
            caveatData.setRegistryLocation(RegistryLocation.CARDIFF);
        } else if (caveatData.getRegistryLocation() == null || (caveatData.getLanguagePreferenceWelsh() != null
            && !caveatData.getLanguagePreferenceWelsh() && caveatData.getRegistryLocation()
            .equals(RegistryLocation.CARDIFF))) {
            caveatData.setRegistryLocation(RegistryLocation.findRegistryLocationByName(nextRegistry.getName()));
        }
    }

    private synchronized Registry getNextRegistry() {
        Registry nextRegistry = registries.get(registryCounter % registries.size());
        registryCounter++;
        return nextRegistry;
    }
}
