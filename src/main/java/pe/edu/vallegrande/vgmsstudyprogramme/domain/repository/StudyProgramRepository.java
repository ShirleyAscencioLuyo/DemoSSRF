package pe.edu.vallegrande.vgmsstudyprogramme.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.model.StudyProgram;
import reactor.core.publisher.Flux;

public interface StudyProgramRepository  extends ReactiveMongoRepository<StudyProgram, String> {

    Flux<StudyProgram> findByProgramIdAndStatus(String programId, String status);
    Flux<StudyProgram> findByStatus(String status);
    Flux<StudyProgram> findByCetproIdAndStatus(String cetproId, String status);

}
