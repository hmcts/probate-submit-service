package uk.gov.hmcts.probate.services.submit.controllers.v2;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.probate.services.submit.services.v2.DraftService;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.OK;

@Api(tags = {"DraftsController"})
@SwaggerDefinition(tags = {@Tag(name = "DraftsController", description = "Drafts API")})
@RestController
@RequiredArgsConstructor
public class DraftsController {

    private final DraftService draftService;

    @ApiOperation(value = "Save case draft to CCD", notes = "Save case draft to CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Draft save to CCD successful"),
            @ApiResponse(code = 400, message = "Draft save to CCD  failed")
    })
    @RequestMapping(path = "/v2/drafts/{applicantEmail}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> saveDraft(@PathVariable("applicantEmail") String applicantEmail,
                                                        @Valid @RequestBody ProbateCaseDetails caseRequest) {
        return new ResponseEntity(draftService.saveDraft(applicantEmail, caseRequest), OK);
    }
}
