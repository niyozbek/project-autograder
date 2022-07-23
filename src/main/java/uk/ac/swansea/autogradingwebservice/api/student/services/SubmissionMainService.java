package uk.ac.swansea.autogradingwebservice.api.student.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.swansea.autogradingwebservice.api.lecturer.entities.Problem;
import uk.ac.swansea.autogradingwebservice.api.lecturer.entities.TestCase;
import uk.ac.swansea.autogradingwebservice.api.lecturer.services.ProblemService;
import uk.ac.swansea.autogradingwebservice.api.lecturer.services.TestCaseService;
import uk.ac.swansea.autogradingwebservice.api.student.controllers.dto.SubmissionDto;
import uk.ac.swansea.autogradingwebservice.api.student.entities.Submission;
import uk.ac.swansea.autogradingwebservice.api.student.entities.SubmissionTestResult;
import uk.ac.swansea.autogradingwebservice.api.student.messaging.SubmissionSender;
import uk.ac.swansea.autogradingwebservice.api.student.services.dto.ExecutionDto;
import uk.ac.swansea.autogradingwebservice.api.student.services.dto.ExecutionResultDto;
import uk.ac.swansea.autogradingwebservice.api.student.services.dto.RuntimeDto;
import uk.ac.swansea.autogradingwebservice.exceptions.BadRequestException;
import uk.ac.swansea.autogradingwebservice.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Objects;

@Service
public class SubmissionMainService {
    @Autowired
    private ProblemService problemService;
    @Autowired
    private TestCaseService testCaseService;
    @Autowired
    private ExecutionService executionService;
    @Autowired
    private SubmissionDetailService submissionDetailService;
    @Autowired
    private SubmissionTestResultService submissionTestResultService;
    @Autowired
    private SubmissionService submissionService;
    @Autowired
    private SubmissionSender submissionSender;

    public Submission submitSolution(Long id, SubmissionDto submissionDto, Long studentId)
            throws ResourceNotFoundException, BadRequestException {
        Problem problem = problemService.getProblem(id);
        if (!Objects.equals(problem.getStatus(), Problem.Status.ACTIVE)) {
            throw new BadRequestException();
        }

        Submission submission = submissionService.createSubmission(problem.getId(),
                studentId,
                submissionDto.getLanguage(),
                submissionDto.getVersion(),
                submissionDto.getFilename(),
                submissionDto.getCode());

        int totalTestCases = testCaseService.countAllTestCasesByProblemId(submission.getProblemId());
        submissionTestResultService.createSubmissionTestResult(submission.getId(), totalTestCases, 0, 0);
        submissionSender.send(submission.getId());
        return submission;
    }

    public void runSubmission(Long submissionId) throws ResourceNotFoundException {
        Submission submission = submissionService.getSubmission(submissionId);
        runTestCases(submission);
    }

    private void runTestCases(Submission submission) throws ResourceNotFoundException {
        List<TestCase> testCases = testCaseService.getAllTestCasesByProblemId(submission.getProblemId());
        ExecutionDto executionDto = ExecutionDto.builder()
                .language(submission.getLanguage())
                .version(submission.getVersion())
                .filename(submission.getFilename())
                .code(submission.getCode())
                .build();
        SubmissionTestResult submissionTestResult = submissionTestResultService.getSubmissionTestResult(submission.getId());
        testCases.forEach(testCase -> {
            executionDto.setInput(testCase.getInput());
            executionDto.setExpectedOutput(testCase.getExpectedOutput());
            ExecutionResultDto executionResultDto = executionService.submit(executionDto);
            // save result to submissionDetails
            submissionDetailService.createSubmissionDetail(submission.getId(),
                    testCase, executionResultDto.getOutput(), executionResultDto.getIsValid());
            submissionTestResult.setProcessedTestCases(submissionTestResult.getProcessedTestCases() + 1);
            if (executionResultDto.getIsValid()) {
                submissionTestResult.setCorrectTestCases(submissionTestResult.getCorrectTestCases() + 1);
            }
            submissionTestResultService.updateSubmissionTestResult(submissionTestResult);
        });
    }

    public List<RuntimeDto> getRuntime(Long id) {
        // maybe consider id later
        return executionService.getRuntimes();
    }
}