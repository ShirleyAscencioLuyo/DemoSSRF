package pe.edu.vallegrande.vgmsstudyprogramme.domain.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class StudyProgramCreateDto {

    @Id
    private  String programId;
    private String name;
    private String module;
    private String trainingLevel;
    private String studyPlanType;
    private String credits;
    private String hours;
    private String status;
    private String cetproId;

}
