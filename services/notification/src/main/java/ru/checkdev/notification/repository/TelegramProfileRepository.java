package ru.checkdev.notification.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.checkdev.notification.domain.TelegramProfile;

public interface TelegramProfileRepository extends CrudRepository<TelegramProfile, Integer> {

    TelegramProfile findByEmail(String email);

    TelegramProfile findByChatId(String id);

    @Modifying
    @Transactional
    @Query("UPDATE telegram_profile tp SET tp.subscribed = ?2 WHERE tp.chatId = ?1")
    int updateSubscribedByChatId(String chatId, boolean choice);
}