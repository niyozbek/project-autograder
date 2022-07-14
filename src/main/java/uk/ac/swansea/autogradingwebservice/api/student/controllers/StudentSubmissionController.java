package uk.ac.swansea.autogradingwebservice.api.student.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uk.ac.swansea.autogradingwebservice.api.student.controllers.dto.SubmissionBriefDto;
import uk.ac.swansea.autogradingwebservice.api.student.controllers.dto.SubmissionDetailDto;
import uk.ac.swansea.autogradingwebservice.api.student.controllers.dto.SubmissionDto;
import uk.ac.swansea.autogradingwebservice.api.student.entities.Submission;
import uk.ac.swansea.autogradingwebservice.api.student.services.SubmissionService;
import uk.ac.swansea.autogradingwebservice.config.MyUserDetails;
import uk.ac.swansea.autogradingwebservice.exceptions.BadRequestException;
import uk.ac.swansea.autogradingwebservice.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Get all submissions+
 * Get a specific submission+
 * View every test case of every submission+
 */
@RestController
@RequestMapping("api/student/submission")
public class StudentSubmissionController {
    @Autowired
    private SubmissionService submissionService;
    @Autowired
    private ModelMapper modelMapper;

    /**
     * Get list of submitted solutions by the student
     * also for a specific problem
     *
     * @return list of submissions
     */
    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT')")
    public List<SubmissionBriefDto> getSubmissions(Authentication authentication,
                                                   @RequestParam(required = false) Long problemId,
                                                   @RequestParam(defaultValue = "0") Integer pageNo,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        MyUserDetails user = (MyUserDetails) authentication.getPrincipal();
        List<Submission> submissionList;
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        if (problemId != null) {
            submissionList = submissionService
                    .getSubmissionsByProblemIdAndStudentId(problemId, user.getId(), pageable);
        } else {
            submissionList = submissionService
                    .getSubmissionsByStudentId(user.getId(), pageable);
        }
        return convertToDto(submissionList);
    }

    /**
     * Get list specific submission
     *
     * @return submission
     */
    @GetMapping("{submissionId}")
    @PreAuthorize("hasAuthority('STUDENT')")
    public SubmissionDto getSubmission(Authentication authentication,
                                       @PathVariable Long submissionId)
            throws BadRequestException, ResourceNotFoundException {
        MyUserDetails user = (MyUserDetails) authentication.getPrincipal();
        return convertToDto(submissionService.getSubmission(submissionId, user.getId()));
    }

    /**
     * Get test cases and results
     */
    @GetMapping("{submissionId}/detail")
    @PreAuthorize("hasAuthority('STUDENT')")
    public List<SubmissionDetailDto> getSubmissionDetails(Authentication authentication,
                                                          @PathVariable Long submissionId) throws BadRequestException, ResourceNotFoundException {
        MyUserDetails user = (MyUserDetails) authentication.getPrincipal();
        return submissionService.getSubmissionDetail(submissionId, user.getId())
                .stream()
                .map(submissionDetail -> SubmissionDetailDto.builder()
                        .id(submissionDetail.getId())
                        .submissionId(submissionDetail.getSubmissionId())
                        .input(submissionDetail.getTestCase().getInput())
                        .expectedOutput(submissionDetail.getTestCase().getExpectedOutput())
                        .actualOutput(submissionDetail.getActualOutput())
                        .testCaseIsPassed(submissionDetail.getTestCaseIsPassed())
                        .build()
                ).collect(Collectors.toList());
    }

    private List<SubmissionBriefDto> convertToDto(List<Submission> submissionList) {
        return submissionList.stream()
                .map(this::convertToBriefDto)
                .collect(Collectors.toList());
    }

    private SubmissionBriefDto convertToBriefDto(Submission submission) {
        return modelMapper.map(submission, SubmissionBriefDto.class);
    }

    private SubmissionDto convertToDto(Submission problem) {
        return modelMapper.map(problem, SubmissionDto.class);
    }
}
