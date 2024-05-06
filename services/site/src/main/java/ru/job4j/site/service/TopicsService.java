package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TopicsService {

    public List<TopicDTO> getByCategory(int id) throws JsonProcessingException {
        var text = new RestAuthCall("http://localhost:9902/topics/" + id).get();
        var mapper = new ObjectMapper();
        return mapper.readValue(text, new TypeReference<>() {
        });
    }

    public TopicDTO getById(int id) throws JsonProcessingException {
        var text = new RestAuthCall("http://localhost:9902/topic/" + id).get();
        var mapper = new ObjectMapper();
        return mapper.readValue(text, new TypeReference<>() {
        });
    }

    /**
     * Метод принимает список CategoryDTO и возвращает Map с ключом id из CategoryDTO
     * и значением количество новых собеседований.
     *
     * @param interviewDTOList List<InterviewDTO>
     * @return Map<Integer, Integer>
     */
    public Map<Integer, Integer> getTopicMap(
            List<CategoryDTO> categories,
            List<InterviewDTO> interviewDTOList) throws JsonProcessingException {
        Map<Integer, Integer> map = categories.stream().collect(Collectors.toMap(CategoryDTO::getId, dto -> 0));
            for (InterviewDTO dto: interviewDTOList) {
                map.computeIfPresent(getById(dto.getTopicId()).getCategory().getId(), (k, v) -> v + 1);
            }
        return map;
    }

    public TopicDTO create(String token, TopicLiteDTO topicLite) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var topic = new TopicDTO();
        topic.setName(topicLite.getName());
        topic.setPosition(topicLite.getPosition());
        topic.setText(topicLite.getText());
        var category = new CategoryDTO();
        category.setId(topicLite.getCategoryId());
        topic.setCategory(category);
        var out = new RestAuthCall("http://localhost:9902/topic/").post(
                token,
                mapper.writeValueAsString(topic)
        );
        return mapper.readValue(out, TopicDTO.class);
    }

    public void update(String token, TopicDTO topic) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        topic.setUpdated(Calendar.getInstance());
        var json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(topic);
        new RestAuthCall("http://localhost:9902/topic/").update(
                token,
                json
        );
    }

    public void delete(String token, int id) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var topic = new TopicDTO();
        topic.setId(id);
        new RestAuthCall("http://localhost:9902/topic/").delete(
                token,
                mapper.writeValueAsString(topic)
        );
    }

    public String getNameById(int id) {
        return new RestAuthCall(String.format("http://localhost:9902/topic/name/%d", id)).get();
    }

    public List<TopicIdNameDTO> getTopicIdNameDtoByCategory(int categoryId)
            throws JsonProcessingException {
        var text = new
                RestAuthCall(String.format("http://localhost:9902/topics/getByCategoryId/%d",
                categoryId)).get();
        var mapper = new ObjectMapper();
        return mapper.readValue(text, new TypeReference<>() {
        });
    }
}
