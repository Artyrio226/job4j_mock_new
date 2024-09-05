package ru.checkdev.notification.domain;

import lombok.*;

import java.util.Calendar;
import java.util.List;

/**
 * DTO модель класса Person сервиса Auth.
 *
 * @author parsentev
 * @since 25.09.2016
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonDTO {
    private String username;
    private String email;
    private String password;
    private Long userChatId;
    private boolean privacy;
    private List<RoleDTO> roles;
    private Calendar created;

}
