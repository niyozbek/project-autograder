package uk.ac.swansea.autogradingwebservice.api.submission.controllers.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SubmissionDto {
    @NotBlank
    private String language;
    @NotBlank
    private String version;
    @NotBlank
    private String fileName;
    @NotBlank
    private String code;
    private String input;
    private String expectedOutput;
}
