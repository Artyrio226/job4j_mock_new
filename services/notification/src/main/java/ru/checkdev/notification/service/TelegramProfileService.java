package ru.checkdev.notification.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.checkdev.notification.domain.TelegramProfile;
import ru.checkdev.notification.repository.TelegramProfileRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TelegramProfileService {
    private final TelegramProfileRepository telegramProfileRepository;

    public Optional<TelegramProfile> findByEmail(String email) {
        return Optional.ofNullable(telegramProfileRepository.findByEmail(email));
    }

    public Optional<TelegramProfile> findByChatId(String id) {
        return Optional.ofNullable(telegramProfileRepository.findByChatId(id));
    }

    public TelegramProfile save(TelegramProfile telegramProfile) {
        return telegramProfileRepository.save(telegramProfile);
    }

    public int updateSubscribedByChatId(String chatId, boolean choice) {
        return telegramProfileRepository.updateSubscribedByChatId(chatId, choice);
    }
}
