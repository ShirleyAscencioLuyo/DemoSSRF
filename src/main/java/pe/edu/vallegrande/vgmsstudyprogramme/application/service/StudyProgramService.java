package pe.edu.vallegrande.vgmsstudyprogramme.application.service;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.dto.Profile;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.dto.StudyProgramCreateDto;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.dto.StudyProgramIdsDto;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.dto.StudyProgramUpdateDto;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.model.StudyProgram;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.repository.StudyProgramRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

@Service
public class StudyProgramService {

    private static final Logger logger = LoggerFactory.getLogger(StudyProgramService.class);
    private static final Pattern LETTERS_ONLY_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$");
    private static final String MODULE_I = "Modulo I";
    private static final String MODULE_II = "Modulo II";
    private static final String[] TRAINING_LEVELS = {"Técnico", "Profesional Técnico", "Auxiliar Técnico"};
    private static final String[] STUDY_PLAN_TYPES = {"Regular", "Modular", "Semiescolarizado", "a distancia"};

    private final StudyProgramRepository studyProgramRepository;
    private final ExternalService externalService;
    private final ModelMapper modelMapper;

    @Autowired
    public StudyProgramService(StudyProgramRepository studyProgramRepository, ExternalService externalService) {
        this.studyProgramRepository = studyProgramRepository;
        this.externalService = externalService;
        this.modelMapper = new ModelMapper();
    }

    public Flux<StudyProgram> getByStatus(String status) {
        logger.info("Obteniendo programas de estudio con estado: {}", status);
        return studyProgramRepository.findByStatus(status);
    }

    public Mono<StudyProgram> create(StudyProgramCreateDto studyProgramCreateDto) {
        return validateCreateDto(studyProgramCreateDto)
                .flatMap(validDto -> {
                    validDto.setStatus("A");
                    StudyProgram studyProgram = modelMapper.map(validDto, StudyProgram.class);
                    logger.info("Creando programa de estudio: {}", studyProgram);
                    return studyProgramRepository.save(studyProgram);
                });
    }

    public Mono<StudyProgram> update(String programId, StudyProgramUpdateDto studyProgramUpdateDto) {
        logger.info("Actualizando programa de estudio con ID: {}", programId);
        return studyProgramRepository.findById(programId)
                .flatMap(program -> {
                    return validateUpdateDto(program, studyProgramUpdateDto)
                            .flatMap(validDto -> {
                                updateProgramFields(program, validDto);
                                return studyProgramRepository.save(program);
                            });
                })
                .doOnError(error -> logger.error("Error al actualizar el programa de estudio: {}", error.getMessage()));
    }

    public Mono<StudyProgram> changeStatus(String programId, String status) {
        logger.info("Cambiando el estado del programa de estudio con ID: {} a {}", programId, status);
        return studyProgramRepository.findById(programId)
                .flatMap(sp -> {
                    sp.setStatus(status);
                    return studyProgramRepository.save(sp);
                });
    }

    public Mono<Profile> getCetproProfile(String cetproId) {
        logger.info("Obteniendo perfil de CETPRO con ID: {}", cetproId);
        return externalService.getCetproProfileById(cetproId);
    }

    public Flux<Profile> listActive() {
        logger.info("Listando perfiles activos");
        return externalService.listActiveProfiles();
    }

    public Mono<StudyProgram> getById(String programId) {
        logger.info("Obteniendo programa de estudio con ID: {}", programId);
        return studyProgramRepository.findById(programId);
    }

    public Flux<StudyProgram> getByCetproId(String cetproId) {
        logger.info("Obteniendo programas de estudio para CETPRO ID: {}", cetproId);
        return studyProgramRepository.findByCetproIdAndStatus(cetproId, "A");
    }

    public Flux<StudyProgram> assignProgramsToCetpro(String cetproId, StudyProgramIdsDto studyProgramIdsDto) {
        logger.info("Asignando programas de estudio a CETPRO ID: {}", cetproId);
        return Flux.fromIterable(studyProgramIdsDto.getStudyProgramIds())
                .flatMap(studyProgramId -> studyProgramRepository.findById(studyProgramId)
                        .flatMap(studyProgram -> {
                            studyProgram.setCetproId(cetproId);
                            studyProgram.setStatus("A");
                            return studyProgramRepository.save(studyProgram);
                        }));
    }

    // Método de validación para create DTO
    private Mono<StudyProgramCreateDto> validateCreateDto(StudyProgramCreateDto dto) {
        return validateCommonFields(dto.getName(), dto.getModule(), dto.getTrainingLevel(),
                dto.getStudyPlanType(), dto.getCredits(), dto.getHours(), dto.getCetproId())
                .then(Mono.just(dto));
    }

    // Método de validación para update DTO
    private Mono<StudyProgramUpdateDto> validateUpdateDto(StudyProgram program, StudyProgramUpdateDto dto) {
        if (dto.getName() != null && isNullOrInvalidName(dto.getName())) {
            return Mono.error(new IllegalArgumentException("El nombre solo puede contener letras."));
        }
        return validateCommonFields(dto.getName(), dto.getModule(), dto.getTrainingLevel(),
                dto.getStudyPlanType(), dto.getCredits(), dto.getHours(), dto.getCetproId())
                .then(Mono.just(dto));
    }

    // Método de validación común
    private Mono<Void> validateCommonFields(String name, String module,
                                            String trainingLevel, String studyPlanType,
                                            String credits, String hours, String cetproId) {
        if (isNullOrInvalidName(name)) {
            return Mono.error(new IllegalArgumentException("El nombre es obligatorio y solo puede contener letras.")).then();
        }
        if (isNullOrInvalidModule(module)) {
            return Mono.error(new IllegalArgumentException("El número de módulo es obligatorio (Modulo I, Modulo II).")).then();
        }
        if (isNullOrInvalidTrainingLevel(trainingLevel)) {
            return Mono.error(new IllegalArgumentException("El nivel de formación es obligatorio (Técnico, Profesional Técnico, Auxiliar Técnico).")).then();
        }
        if (isNullOrInvalidStudyPlanType(studyPlanType)) {
            return Mono.error(new IllegalArgumentException("El plan de estudio es obligatorio (Regular, Modular, Semiescolarizado o a distancia).")).then();
        }
        if (isNullOrInvalidCredits(credits)) {
            return Mono.error(new IllegalArgumentException("Los créditos son obligatorios y deben ser un número válido hasta 3 dígitos.")).then();
        }
        if (isNullOrInvalidHours(hours)) {
            return Mono.error(new IllegalArgumentException("Las horas son obligatorias y deben ser un número válido hasta 3 dígitos.")).then();
        }
        if (cetproId == null) {
            return Mono.error(new IllegalArgumentException("El ID de CETPRO es obligatorio.")).then();
        }
        return Mono.empty();
    }

    private boolean isNullOrInvalidName(String name) {
        return name == null || !LETTERS_ONLY_PATTERN.matcher(name).matches();
    }

    private boolean isNullOrInvalidModule(String module) {
        return module == null || (!MODULE_I.equals(module) && !MODULE_II.equals(module));
    }

    private boolean isNullOrInvalidTrainingLevel(String level) {
        for (String validLevel : TRAINING_LEVELS) {
            if (validLevel.equals(level)) {
                return false;
            }
        }
        return true;
    }

    private boolean isNullOrInvalidStudyPlanType(String type) {
        for (String validType : STUDY_PLAN_TYPES) {
            if (validType.equals(type)) {
                return false;
            }
        }
        return true;
    }

    private boolean isNullOrInvalidCredits(String credits) {
        return credits == null || !credits.matches("\\d{1,3}");
    }

    private boolean isNullOrInvalidHours(String hours) {
        return hours == null || !hours.matches("\\d{1,3}");
    }

    private void updateProgramFields(StudyProgram program, StudyProgramUpdateDto validDto) {
        if (validDto.getName() != null) {
            program.setName(validDto.getName());
        }
        if (validDto.getModule() != null) {
            program.setModule(validDto.getModule());
        }
        if (validDto.getTrainingLevel() != null) {
            program.setTrainingLevel(validDto.getTrainingLevel());
        }
        if (validDto.getStudyPlanType() != null) {
            program.setStudyPlanType(validDto.getStudyPlanType());
        }
        if (validDto.getCredits() != null) {
            program.setCredits(validDto.getCredits());
        }
        if (validDto.getHours() != null) {
            program.setHours(validDto.getHours());
        }
        if (validDto.getCetproId() != null) {
            program.setCetproId(validDto.getCetproId());
        }
    }
}
