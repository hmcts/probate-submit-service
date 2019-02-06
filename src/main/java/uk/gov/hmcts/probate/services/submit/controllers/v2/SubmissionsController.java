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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.probate.services.submit.services.SubmissionsService;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.validation.groups.SubmissionGroup;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Api(tags = {"SubmissionsController"})
@SwaggerDefinition(tags = {@Tag(name = "SubmissionsController", description = "Submissions API")})
@RestController
@RequiredArgsConstructor
public class SubmissionsController {

    private final SubmissionsService submissionsService;

    @ApiOperation(value = "Save case draft to CCD", notes = "Save case draft to CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Draft save to CCD successful"),
            @ApiResponse(code = 400, message = "Draft save to CCD  failed")
    })
    @PostMapping(path = "/submissions/{applicantEmail}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> submit(@PathVariable("applicantEmail") String applicantEmail,
                                                     @Validated(SubmissionGroup.class) @RequestBody ProbateCaseDetails caseRequest) {
        log.info("Submitting for case type: {}", caseRequest.getCaseData().getClass().getSimpleName());
        return new ResponseEntity(submissionsService.submit(applicantEmail.toLowerCase(), caseRequest), OK);
    }
}
