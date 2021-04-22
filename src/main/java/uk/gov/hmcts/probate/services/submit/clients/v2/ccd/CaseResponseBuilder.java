package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseResponseBuilder {

    private final CaseDetailsToCaseDataMapper caseDetailsToCaseDataMapper;

    public ProbateCaseDetails createCaseResponse(CaseDetails caseDetails) {
        CaseInfo caseInfo = getCaseInfo(caseDetails);

        return ProbateCaseDetails.builder()
            .caseData(caseDetailsToCaseDataMapper.map(caseDetails))
            .caseInfo(caseInfo)
            .build();
    }

    private CaseInfo getCaseInfo(CaseDetails caseDetails) {
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(caseDetails.getId().toString());
        caseInfo.setState(CaseState.getState(caseDetails.getState()));
        caseInfo.setCaseCreatedDate(
            caseDetails.getCreatedDate() != null ? caseDetails.getCreatedDate().toLocalDate() : null);
        return caseInfo;
    }
}
