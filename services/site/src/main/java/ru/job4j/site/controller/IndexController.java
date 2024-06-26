package ru.job4j.site.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.job4j.site.dto.CategoryDTO;
import ru.job4j.site.dto.InterviewDTO;
import ru.job4j.site.service.*;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static ru.job4j.site.controller.RequestResponseTools.getToken;

@Controller
@AllArgsConstructor
@Slf4j
public class IndexController {
    private static final int INTERVIEW_MODE = 1;
    private final CategoriesService categoriesService;
    private final InterviewsService interviewsService;
    private final AuthService authService;
    private final NotificationService notifications;
    private final TopicsService topicsService;
    private final ProfilesService profilesService;

    @GetMapping({"/", "index"})
    public String getIndexPage(Model model, HttpServletRequest req) throws JsonProcessingException {
        RequestResponseTools.addAttrBreadcrumbs(model,
                "Главная", "/"
        );
        List<CategoryDTO> categories = categoriesService.getMostPopular();
        try {
            model.addAttribute("categories", categories);
            var token = getToken(req);
            if (token != null) {
                var userInfo = authService.userInfo(token);
                model.addAttribute("userInfo", userInfo);
                model.addAttribute("userDTO", notifications.findCategoriesByUserId(userInfo.getId()));
                RequestResponseTools.addAttrCanManage(model, userInfo);
            }
        } catch (Exception e) {
            log.error("Remote application not responding. Error: {}. {}, ", e.getCause(), e.getMessage());
        }
        List<InterviewDTO> interviewDTOList = interviewsService.getByType(INTERVIEW_MODE);
        model.addAttribute("new_interviews", interviewDTOList);
        model.addAttribute("number_new_interviews", topicsService.getTopicMap(categories, interviewDTOList));
        model.addAttribute("profiles", profilesService.getProfileMap(interviewDTOList));
        return "index";
    }
}