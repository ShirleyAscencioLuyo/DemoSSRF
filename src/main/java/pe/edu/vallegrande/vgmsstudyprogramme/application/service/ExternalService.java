package pe.edu.vallegrande.vgmsstudyprogramme.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.vgmsstudyprogramme.domain.dto.Profile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ExternalService {

    @Value("${services.cetpro-profile.url}")
    private String profileUrl;

    private final WebClient.Builder webClientBuilder;

    public ExternalService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<Profile> getCetproProfileById(String cetproId) {
        return fetchData(profileUrl, cetproId, Profile.class);
    }

    public Flux<Profile> listActiveProfiles() {
        return fetchDataList(profileUrl + "/list/active", Profile.class);
    }

    private <T> Mono<T> fetchData(String baseUrl, String id, Class<T> responseType) {
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder.path(baseUrl).pathSegment(id).build())
                .retrieve()
                .bodyToMono(responseType)
                .onErrorResume(e -> {
                    log.error("Error fetching data: ", e);
                    return Mono.empty();
                });
    }


    private <T> Flux<T> fetchDataList(String baseUrl, Class<T> responseType) {
        return webClientBuilder.build()
                .get()
                .uri(baseUrl)
                .retrieve()
                .bodyToFlux(responseType)
                .onErrorResume(e -> {
                    log.error("Error fetching data: ", e);
                    return Flux.empty();
                });
    }
}
