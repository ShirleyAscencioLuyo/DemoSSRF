import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.edu.vallegrande.vgmsstudyprogramme.application.service.StudyProgramService;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.dto.StudyProgramCreateDto;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.dto.StudyProgramUpdateDto;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.model.StudyProgram;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.repository.StudyProgramRepository;
import reactor.core.publisher.Mono;

class StudyProgramServiceTest {

    @Mock
    private StudyProgramRepository studyProgramRepository;

    @InjectMocks
    private StudyProgramService studyProgramService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Crear un nuevo programa de estudio exitosamente")
    void testCreate() {
        StudyProgramCreateDto createDto = new StudyProgramCreateDto();
        createDto.setName("Costura y Acabados");
        createDto.setModule("Modulo II");
        createDto.setTrainingLevel("Profesional Técnico");
        createDto.setStudyPlanType("Modular");
        createDto.setCredits("30");
        createDto.setHours("300");
        createDto.setCetproId("66fa22db2b7ad126abc276dc");

        StudyProgram savedProgram = new StudyProgram();
        savedProgram.setProgramId("1");
        savedProgram.setName("Nuevo Programa");

        when(studyProgramRepository.save(any(StudyProgram.class))).thenReturn(Mono.just(savedProgram));

        Mono<StudyProgram> result = studyProgramService.create(createDto);

        assertEquals("Nuevo Programa", result.block().getName());
        verify(studyProgramRepository).save(any(StudyProgram.class));
    }

    @Test
    @DisplayName("Intentar actualizar un programa que no existe")
    void testUpdateProgramNotFound() {
        String programId = "1";
        StudyProgramUpdateDto updateDto = new StudyProgramUpdateDto();
        updateDto.setName("Programa Actualizado");

        when(studyProgramRepository.findById(programId)).thenReturn(Mono.empty());

        Mono<StudyProgram> result = studyProgramService.update(programId, updateDto);

        assertNull(result.block());
    }

    @Test
    @DisplayName("Actualizar un programa de estudio con datos inválidos")
    void testUpdateWithInvalidData() {
        String programId = "1";
        StudyProgram program = new StudyProgram();
        program.setProgramId(programId);
        program.setName("Programa Existente");

        StudyProgramUpdateDto updateDto = new StudyProgramUpdateDto();
        updateDto.setName("123Invalid");

        when(studyProgramRepository.findById(programId)).thenReturn(Mono.just(program));

        Mono<StudyProgram> result = studyProgramService.update(programId, updateDto);

        assertThrows(IllegalArgumentException.class, result::block);
    }

    @Test
    @DisplayName("Cambiar el estado de un programa de estudio")
    void testChangeStatus() {
        StudyProgram program = new StudyProgram();
        program.setStatus("A");
        when(studyProgramRepository.findById("1")).thenReturn(Mono.just(program));
        when(studyProgramRepository.save(any(StudyProgram.class))).thenReturn(Mono.just(program));
        Mono<StudyProgram> result = studyProgramService.changeStatus("1", "I");
        assertNotNull(result);
        assertEquals("I", result.block().getStatus());
    }

    @Test
    @DisplayName("Obtener un programa de estudio por su ID")
    void testGetById() {
        StudyProgram program = new StudyProgram();
        program.setName("Program 1");
        when(studyProgramRepository.findById("1")).thenReturn(Mono.just(program));
        Mono<StudyProgram> result = studyProgramService.getById("1");
        assertNotNull(result);
        assertEquals("Program 1", result.block().getName());
    }

}
