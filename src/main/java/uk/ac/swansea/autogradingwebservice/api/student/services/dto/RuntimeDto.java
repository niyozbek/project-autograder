package uk.ac.swansea.autogradingwebservice.api.student.services.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RuntimeDto implements Serializable {
    private String language;
    private String version;
}
