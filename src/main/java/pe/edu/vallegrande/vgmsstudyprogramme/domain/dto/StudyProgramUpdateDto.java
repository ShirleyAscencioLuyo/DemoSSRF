package pe.edu.vallegrande.vgmsstudyprogramme.domain.dto;

import lombok.Data;

@Data
public class StudyProgramUpdateDto {

    private String name;
    private String module;
    private String trainingLevel;
    private String studyPlanType;
    private String credits;
    private String hours;
    private String cetproId;

}
