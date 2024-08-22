package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.service.TelegramProfileService;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Calendar;

@AllArgsConstructor
@Slf4j
public class ForgetAction implements Action {

    private static final String ERROR_OBJECT = "error";
    private static final String URL_AUTH_FORGOT = "/forgot";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint authCallWebClint;
    private final TelegramProfileService telegramProfileService;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var sl = System.lineSeparator();
        var text = "";

        var telegramProfile = telegramProfileService.findByChatId(chatId);
        if (telegramProfile.isEmpty()) {
            text = "Данный аккаунт Telegram не зарегистрирован";
            return new SendMessage(chatId, text);
        }

        var email = telegramProfile.get().getEmail();
        var password = tgConfig.getPassword();
        var person = PersonDTO.builder()
                .email(email)
                .password(password)
                .privacy(true)
                .created(Calendar.getInstance())
                .build();
        Object result;
        try {
            result = authCallWebClint.doPost(URL_AUTH_FORGOT, person).block();
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                   + "/start";
            return new SendMessage(chatId, text);
        }

        var mapObject = tgConfig.getObjectToMap(result);

        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка восстановления пароля: обратитесь в поддержку ";
            return new SendMessage(chatId, text);
        }

        text = "Ваш Логин: " + email + sl
               + "Новый пароль: " + person.getPassword();
        return new SendMessage(chatId, text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }
}
