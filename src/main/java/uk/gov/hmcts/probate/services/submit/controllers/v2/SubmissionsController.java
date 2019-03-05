package uk.gov.hmcts.probate.services.submit.controllers.v2;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.probate.services.submit.services.SubmissionsService;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.SubmitResult;
import uk.gov.hmcts.reform.probate.model.validation.groups.SubmissionGroup;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Api(tags = {"SubmissionsController"})
@SwaggerDefinition(tags = {@Tag(name = "SubmissionsController", description = "Submissions API")})
@RestController
@RequiredArgsConstructor
public class SubmissionsController {

    public static final String APPLICANT_EMAIL = "applicantEmail";
    private final SubmissionsService submissionsService;

    @ApiOperation(value = "Save case draft to CCD", notes = "Save case draft to CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Draft save to CCD successful"),
            @ApiResponse(code = 400, message = "Draft save to CCD  failed")
    })
    @PostMapping(path = "/submissions/{applicantEmail}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<SubmitResult> createCase(@PathVariable(APPLICANT_EMAIL) String applicantEmail,
                                                   @Validated(SubmissionGroup.class) @RequestBody ProbateCaseDetails caseRequest) {
        CaseData caseData = caseRequest.getCaseData();
        log.info("Submitting for case type: {}", caseData.getClass().getSimpleName());
        SubmitResult submitResult = submissionsService.createCase(applicantEmail.toLowerCase(), caseRequest);
        return getCorrectResponse(submitResult);
    }


    @ApiOperation(value = "Save case draft to CCD", notes = "Save case draft to CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Draft save to CCD successful"),
            @ApiResponse(code = 400, message = "Draft save to CCD  failed")
    })
    @PostMapping(path = "/submissions/update/{applicantEmail}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<SubmitResult> updateDraftToCase(@PathVariable(APPLICANT_EMAIL) String applicantEmail,
                                                          @Validated(SubmissionGroup.class) @RequestBody ProbateCaseDetails caseRequest) {
        CaseData caseData = caseRequest.getCaseData();
        log.info("Submitting for case type: {}", caseData.getClass().getSimpleName());
        SubmitResult submitResult = submissionsService.updateDraftToCase(applicantEmail.toLowerCase(), caseRequest);
        return getCorrectResponse(submitResult);
    }

    private ResponseEntity<SubmitResult> getCorrectResponse(SubmitResult submitResult) {
        ResponseEntity responseEntity = null;
        if (submitResult.getValidatorResults().isPresent() && !submitResult.isValid() ) {
            responseEntity=  new ResponseEntity(submitResult, BAD_REQUEST);
        }else {
            responseEntity = new ResponseEntity(submitResult, OK);
        }
        return responseEntity;
    }
}
