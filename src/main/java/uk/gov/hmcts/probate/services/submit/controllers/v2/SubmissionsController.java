package uk.gov.hmcts.probate.services.submit.controllers.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.probate.services.submit.services.SubmissionsService;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.SubmitResult;

@Slf4j
@Tag(name = "SubmissionsController", description = "Submissions API")
@RestController
@RequiredArgsConstructor
public class SubmissionsController {

    public static final String APPLICATION_ID = "applicationId";
    private final SubmissionsService submissionsService;

    @Operation(summary = "Save case draft to CCD", description = "Save case draft to CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Draft save to CCD successful"),
        @ApiResponse(responseCode = "400", description = "Draft save to CCD  failed")
    })
    @PostMapping(path = "/submissions/{applicationId}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<SubmitResult> createCase(@PathVariable(APPLICATION_ID) String applicationId,
                                                   @RequestBody ProbateCaseDetails caseRequest) {
        CaseData caseData = caseRequest.getCaseData();
        log.info("Submitting for case type: {}", caseData.getClass().getSimpleName());
        SubmitResult submitResult = submissionsService.createCase(applicationId.toLowerCase(), caseRequest);
        return ResponseEntity.ok(submitResult);
    }
}
