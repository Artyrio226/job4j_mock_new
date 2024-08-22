package ru.checkdev.notification.domain;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "telegram_profile")
public class TelegramProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true)
    private String chatId;

    @Column(unique = true)
    private String email;

    private boolean subscribed;
}
