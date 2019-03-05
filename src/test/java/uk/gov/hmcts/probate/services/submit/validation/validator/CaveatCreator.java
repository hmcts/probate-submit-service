package uk.gov.hmcts.probate.services.submit.validation.validator;

import uk.gov.hmcts.reform.probate.model.cases.Address;
import uk.gov.hmcts.reform.probate.model.cases.ApplicationType;
import uk.gov.hmcts.reform.probate.model.cases.CollectionMember;
import uk.gov.hmcts.reform.probate.model.cases.FullAliasName;
import uk.gov.hmcts.reform.probate.model.cases.RegistryLocation;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;

import java.time.LocalDate;
import java.util.Arrays;

public class CaveatCreator {

    public static CaveatData createCaveatCase() {

        CaveatData caveatData = new CaveatData();
        caveatData.setApplicationType(ApplicationType.PERSONAL);

        caveatData.setCaveatorAddress(getAddress("caveator"));
        caveatData.setCaveatorEmailAddress("caveator@email.com");
        caveatData.setCaveatorForenames("caveator forename");
        caveatData.setCaveatorSurname("caveator surname");

        caveatData.setDeceasedAddress(getAddress("deceased"));
        CollectionMember<FullAliasName> fullAliasNameCollectionMember = new CollectionMember<>();
        fullAliasNameCollectionMember.setValue(FullAliasName.builder().fullAliasName("fullAliasName").build());
        caveatData.setDeceasedFullAliasNameList(Arrays.asList(fullAliasNameCollectionMember));
        caveatData.setDeceasedDateOfBirth(LocalDate.of(1966, 3, 4));
        caveatData.setDeceasedDateOfDeath(LocalDate.of(2018, 11, 20));
        caveatData.setDeceasedForenames("deceased forename");
        caveatData.setDeceasedSurname("deceased surname");
        caveatData.setRegistryLocation(RegistryLocation.OXFORD);
        caveatData.setExpiryDate(LocalDate.of(2019, 2, 14));

        return caveatData;

    }

    private static Address getAddress(String name) {
        Address address = new Address();
        address.setAddressLine1(name + " address line 1");
        address.setAddressLine2(name + " address line 2");
        address.setAddressLine3(name + " address line 3");
        address.setCounty(name + " county");
        address.setPostTown(name + " post town");
        address.setPostCode(name + " post code");
        address.setCountry(name + " country");
        return address;
    }

    private CaveatCreator() {
    }
}
