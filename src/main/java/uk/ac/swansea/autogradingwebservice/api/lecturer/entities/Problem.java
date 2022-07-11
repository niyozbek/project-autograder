package uk.ac.swansea.autogradingwebservice.api.lecturer.entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "problems")
@Data
public class Problem {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private Integer status;
    private Long lecturer_id;
}