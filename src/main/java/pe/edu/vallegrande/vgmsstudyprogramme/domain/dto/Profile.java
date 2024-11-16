package pe.edu.vallegrande.vgmsstudyprogramme.domain.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Profile {

    @Id
    private String id;
    private String name;
    private String modularCode;
    private String dreGre;
    private String managementType;
    private String status;

}
