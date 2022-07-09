package uk.ac.swansea.autogradingwebservice.api.submission.services;

import com.github.codeboy.piston4j.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.swansea.autogradingwebservice.api.submission.controllers.dto.RuntimeDto;
import uk.ac.swansea.autogradingwebservice.api.submission.controllers.dto.SubmissionDto;
import uk.ac.swansea.autogradingwebservice.api.submission.controllers.dto.SubmissionResultDto;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SubmissionService {
    @Autowired
    private ModelMapper modelMapper;

    /**
     * You can also execute the code without getting the runtime.
     * However, this is not recommended since it won't work unless you know the correct version of the runtime
     *
     * @param dto input
     * @return result
     */
    public SubmissionResultDto submit(SubmissionDto dto) {
        Piston api = Piston.getDefaultApi(); //get the api at https://emkc.org/api/v2/piston
        CodeFile codeFile = new CodeFile(dto.getFileName(), dto.getCode()); //create the codeFile containing the javascript code
        ExecutionRequest request = new ExecutionRequest(dto.getLanguage(), dto.getVersion(), codeFile); //create the request using the codeFile, a language and a version
        request.setStdin(dto.getInput());
        ExecutionResult result = api.execute(request); //execute the request

        // get crucial data into variables
        SubmissionResultDto resultDto = new SubmissionResultDto();
        String output = result.getOutput().getOutput();
        // remove \n from the result
        if (output.length() > 1) {
            output = output.substring(0, output.length() - 1);
        }

        String expectedOutput = dto.getExpectedOutput();
        // populate dto fields
        resultDto.setOutput(output);
        resultDto.setExpectedOutput(expectedOutput);
        resultDto.setIsValid(Objects.equals(output, expectedOutput));
        return resultDto;
    }

    public List<RuntimeDto> getRuntimes() {
        Piston api = Piston.getDefaultApi(); //get the api at https://emkc.org/api/v2/piston
        return api.getRuntimes()
                .stream()
                .map(element -> modelMapper.map(element, RuntimeDto.class))
                .collect(Collectors.toList());
    }
}