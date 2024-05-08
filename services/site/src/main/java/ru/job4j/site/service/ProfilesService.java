package ru.job4j.site.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.InterviewDTO;
import ru.job4j.site.dto.ProfileDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CheckDev пробное собеседование
 * ProfileService класс обработки логики с моделью ProfileDTO
 *
 * @author Dmitry Stepanov
 * @version 23.09.2023T03:05
 */
@Service
@AllArgsConstructor
@Slf4j
public class ProfilesService {
    private static final String URL_PROFILES = "/profiles/";
    private final WebClientAuthCall webClientAuthCall;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Метод получает из сервиса Auth один профиль по ID
     *
     * @param id Int ID
     * @return Optional<ProfileDTO>
     */
    public Optional<ProfileDTO> getProfileById(int id) {
        var uri = URL_PROFILES + id;
        ResponseEntity<ProfileDTO> profile = webClientAuthCall
                .doGetReqParam(uri)
                .block();
        return Optional.ofNullable(profile.getBody());
    }

    /**
     * Метод получает из сервиса Auth список всех профилей.
     *
     * @return List<ProfileDTO>
     */
    public List<ProfileDTO> getAllProfile() {
        var responseEntity = webClientAuthCall
                .doGetReqParamAll(URL_PROFILES)
                .block();
        return responseEntity.getBody();
    }

    /**
     * Метод принимает список InterviewDTO и возвращает Map с ключом id из InterviewDTO
     * и значением ProfileDTO.
     *
     * @param list List<InterviewDTO>
     * @return Map<Integer, ProfileDTO>
     */
    public Map<Integer, ProfileDTO> getProfileMap(List<InterviewDTO> list) {
        List<Integer> profileIds = list.stream().map(InterviewDTO::getSubmitterId).toList();
        Map<Integer, ProfileDTO> map = getByProfilesIds(profileIds).stream()
                .collect(Collectors.toMap(ProfileDTO::getId, dto -> dto));
        return list.stream()
                .collect(Collectors.toMap(
                        InterviewDTO::getId,
                        dto -> map.get(dto.getSubmitterId())
                ));
    }

    /**
     * Метод принимает список ID из ProfileDTO и возвращает список ProfileDTO.
     *
     * @param profileIds List<Integer>
     * @return List<ProfileDTO>
     */
    public List<ProfileDTO> getByProfilesIds(List<Integer> profileIds) {
        var tids = parseIdsListToString(profileIds);
        var uri = URL_PROFILES + "listIds/" + tids;
        var responseEntity = webClientAuthCall
                .doGetReqParamAll(uri)
                .block();
        return responseEntity.getBody();
    }

    private String parseIdsListToString(List<Integer> list) {
        var builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i));
            if (i < list.size() - 1) {
                builder.append(',');
            }
        }
        return builder.toString();
    }
}
