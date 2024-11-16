package pe.edu.vallegrande.vgmsstudyprogramme.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import pe.edu.vallegrande.vgmsstudyprogramme.application.service.StudyProgramService;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.dto.Profile;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.dto.StudyProgramCreateDto;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.dto.StudyProgramUpdateDto;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.dto.StudyProgramIdsDto;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.model.StudyProgram;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
@RestController
@RequestMapping("/study-program")
public class StudyProgramController {

    private final StudyProgramService studyProgramServiceImpl;
    private final RestTemplate restTemplate;

    @Autowired
    public StudyProgramController(StudyProgramService studyProgramServiceImpl, RestTemplate restTemplate) {
        this.studyProgramServiceImpl = studyProgramServiceImpl;
        this.restTemplate = restTemplate;
    }

    // Endpoint vulnerable
    @PostMapping("/fetch-external-data")
    public ResponseEntity<String> fetchExternalData(@RequestBody Map<String, String> request) {
        try {
            String url = request.get("url"); // Extrae el valor de la clave "url"
            String result = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la URL.");
        }
    }

    // Endpoint seguro con mitigación de SSRF
    @PostMapping("/fetch-external-data-safe")
    public ResponseEntity<String> fetchExternalDataSafe(@RequestBody String url) {
        // Validar si la URL es interna (localhost o 127.0.0.1)
        if (url.startsWith("http://localhost") || url.startsWith("http://127.0.0.1") || url.startsWith("http://0.0.0.0")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado a URLs internas.");
        }

        // Realizar la solicitud si es una URL externa válida
        String result = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(result);
    }

    // Otros endpoints de tu controlador (no modificados)
    @GetMapping("/profile")
    public Flux<Profile> testProfileClient() {
        return studyProgramServiceImpl.listActive();
    }

    @GetMapping("/list/active")
    public ResponseEntity<Flux<StudyProgram>> listActive() {
        return ResponseEntity.ok(studyProgramServiceImpl.getByStatus("A"));
    }

    @GetMapping("/list/inactive")
    public ResponseEntity<Flux<StudyProgram>> listInactive() {
        return ResponseEntity.ok(studyProgramServiceImpl.getByStatus("I"));
    }

    @PostMapping("/create")
    public Mono<StudyProgram> create(@RequestBody StudyProgramCreateDto studyProgramCreateDto) {
        return studyProgramServiceImpl.create(studyProgramCreateDto);
    }

    @PutMapping("/update/{id}")
    public Mono<StudyProgram> update(@PathVariable("id") String programId,
                                     @RequestBody StudyProgramUpdateDto studyProgramUpdateDto) {
        return studyProgramServiceImpl.update(programId, studyProgramUpdateDto);
    }

    @PutMapping("/activate/{id}")
    public ResponseEntity<Mono<StudyProgram>> activate(@PathVariable("id") String programId) {
        return ResponseEntity.ok(studyProgramServiceImpl.changeStatus(programId, "A"));
    }

    @PutMapping("/inactive/{id}")
    public ResponseEntity<Mono<StudyProgram>> deactivate(@PathVariable("id") String programId) {
        return ResponseEntity.ok(studyProgramServiceImpl.changeStatus(programId, "I"));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<Mono<Profile>> getCetproProfile(@PathVariable("id") String cetproId) {
        return ResponseEntity.ok(studyProgramServiceImpl.getCetproProfile(cetproId));
    }

    @GetMapping("/{id}")
    public Mono<StudyProgram> getById(@PathVariable("id") String programId) {
        return studyProgramServiceImpl.getById(programId);
    }

    @GetMapping("/cetpro/{cetproId}")
    public ResponseEntity<Flux<StudyProgram>> getProgramsByCetpro(@PathVariable String cetproId) {
        return ResponseEntity.ok(studyProgramServiceImpl.getByCetproId(cetproId));
    }

    @PostMapping("/{cetproId}/programs")
    public Flux<StudyProgram> assignProgramsToCetpro(@PathVariable String cetproId, @RequestBody StudyProgramIdsDto studyProgramIdsDto) {
        return studyProgramServiceImpl.assignProgramsToCetpro(cetproId, studyProgramIdsDto);
    }
}
