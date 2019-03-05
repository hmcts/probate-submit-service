package uk.gov.hmcts.probate.services.submit.validation.validator;

import com.google.common.collect.Lists;
import uk.gov.hmcts.reform.probate.model.IhtFormType;
import uk.gov.hmcts.reform.probate.model.PaymentStatus;
import uk.gov.hmcts.reform.probate.model.Relationship;
import uk.gov.hmcts.reform.probate.model.cases.Address;
import uk.gov.hmcts.reform.probate.model.cases.AliasName;
import uk.gov.hmcts.reform.probate.model.cases.ApplicationType;
import uk.gov.hmcts.reform.probate.model.cases.CasePayment;
import uk.gov.hmcts.reform.probate.model.cases.CollectionMember;
import uk.gov.hmcts.reform.probate.model.cases.MaritalStatus;
import uk.gov.hmcts.reform.probate.model.cases.RegistryLocation;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.Declaration;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantType;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.SpouseNotApplyingReason;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class GrantOfRepresentationCreator {

    public static GrantOfRepresentationData createIntestacyCase() {
        GrantOfRepresentationData grantOfRepresentationData = new GrantOfRepresentationData();
        grantOfRepresentationData.setApplicationType(ApplicationType.PERSONAL);
        grantOfRepresentationData.setGrantType(GrantType.INTESTACY);
        grantOfRepresentationData.setPrimaryApplicantEmailAddress("jon.snow@thenorth.com");
        grantOfRepresentationData.setPrimaryApplicantForenames("Jon");
        grantOfRepresentationData.setPrimaryApplicantSurname("Snow");
        Address primaryApplicantAddress = new Address();
        primaryApplicantAddress.setAddressLine1("Pret a Manger St. Georges Hospital Blackshaw Road London SW17 0QT");
        grantOfRepresentationData.setPrimaryApplicantAddress(primaryApplicantAddress);
        grantOfRepresentationData.setPrimaryApplicantAddressFound(true);
        grantOfRepresentationData
            .setPrimaryApplicantFreeTextAddress("Pret a Manger St. Georges Hospital Blackshaw Road");
        grantOfRepresentationData.setPrimaryApplicantPhoneNumber("123455678");
        grantOfRepresentationData.setPrimaryApplicantRelationshipToDeceased(Relationship.PARTNER);

        grantOfRepresentationData.setDeceasedSpouseNotApplyingReason(SpouseNotApplyingReason.MENTALLY_INCAPABLE);
        grantOfRepresentationData.setDeceasedSurname("Stark");
        grantOfRepresentationData.setDeceasedForenames("Ned");
        grantOfRepresentationData.setDeceasedDateOfBirth(LocalDate.of(1930, 1, 1));
        grantOfRepresentationData.setDeceasedDateOfDeath(LocalDate.of(2018, 1, 1));
        Address deceasedAddress = new Address();
        deceasedAddress.setAddressLine1("Winterfell, Westeros");
        grantOfRepresentationData.setDeceasedAddress(deceasedAddress);
        grantOfRepresentationData.setDeceasedFreeTextAddress("Winterfell, Westeros");
        grantOfRepresentationData.setDeceasedAddressFound(true);
        grantOfRepresentationData.setDeceasedAnyOtherNames(true);
        grantOfRepresentationData.setDeceasedHasAssetsOutsideUK(Boolean.FALSE);
        CollectionMember<AliasName> aliasNameCollectionMember = new CollectionMember<>();
        AliasName aliasName = new AliasName();
        aliasName.setForenames("King");
        aliasName.setLastName("North");
        aliasNameCollectionMember.setValue(aliasName);
        grantOfRepresentationData.setDeceasedAliasNameList(Lists.newArrayList(aliasNameCollectionMember));
        grantOfRepresentationData.setDeceasedMartialStatus(MaritalStatus.MARRIED);
        grantOfRepresentationData.setDeceasedDivorcedInEnglandOrWales(false);
        grantOfRepresentationData.setDeceasedOtherChildren(true);
        grantOfRepresentationData.setChildrenDied(false);
        grantOfRepresentationData.setGrandChildrenSurvivedUnderEighteen(false);
        grantOfRepresentationData.setChildrenOverEighteenSurvived(true);
        grantOfRepresentationData.setDeceasedAnyChildren(false);
        grantOfRepresentationData.setDeceasedAnyOtherNames(false);

        grantOfRepresentationData.setRegistryLocation(RegistryLocation.BIRMINGHAM);
        grantOfRepresentationData.setDeceasedHasAssetsOutsideUK(true);
        grantOfRepresentationData.setAssetsOverseasNetValue(10050L);
        grantOfRepresentationData.setIhtFormId(IhtFormType.IHT205);
        grantOfRepresentationData.setIhtFormCompletedOnline(true);
        grantOfRepresentationData.setIhtGrossValue(100000L);
        grantOfRepresentationData.setIhtNetValue(100000L);
        grantOfRepresentationData.setIhtReferenceNumber("GOT123456");

        Declaration declaration = new Declaration();
        declaration.setDeclarationCheckbox(true);
        grantOfRepresentationData.setDeclaration(declaration);

        grantOfRepresentationData.setExtraCopiesOfGrant(5L);
        grantOfRepresentationData.setOutsideUkGrantCopies(6L);

        final CollectionMember<CasePayment> paymentCollectionMember = new CollectionMember<>();
        CasePayment payment = new CasePayment();
        payment.setStatus(PaymentStatus.SUCCESS);

        Date date = Date.from(LocalDate.of(2018, 12, 3).atStartOfDay(ZoneId.systemDefault()).toInstant());
        payment.setDate(date);
        payment.setReference("RC-1537-1988-5489-1985");
        payment.setAmount(22050L);
        payment.setMethod("online");
        payment.setTransactionId("r23k178busa0rp2mh27m0vchja");
        payment.setSiteId("P223");
        paymentCollectionMember.setValue(payment);
        grantOfRepresentationData.setPayments(Lists.newArrayList(paymentCollectionMember));
        grantOfRepresentationData.setUploadDocumentUrl("http://document-management/document/12345");
        return grantOfRepresentationData;
    }

    private GrantOfRepresentationCreator() {
    }
}
