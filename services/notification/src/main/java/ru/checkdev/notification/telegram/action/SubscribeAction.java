package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.service.TelegramProfileService;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

@AllArgsConstructor
@Slf4j
public class SubscribeAction implements Action {

    private static final String ERROR_OBJECT = "error";
    private static final String CHECK_PASSWORD = "/checkPassword";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TelegramProfileService telegramProfileService;
    private final TgAuthCallWebClint tgAuthCallWebClint;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var text = "";
        var sl = System.lineSeparator();

        var telegramProfile = telegramProfileService.findByChatId(chatId);
        if (telegramProfile.isEmpty()) {
            log.error("Не найден TelegramProfile по userId");
            text = "Вы не зарегистрированы" + sl
                   + "/start";
            return new SendMessage(chatId, text);
        }

        text = "Введите пароль для подтверждения:";
        return new SendMessage(chatId, text);
    }

    @Override
    @Transactional
    public BotApiMethod<Message> callback(Message message) {
        var chatId = message.getChatId().toString();
        var password = message.getText();
        var text = "";
        var sl = System.lineSeparator();
        var telegramProfile = telegramProfileService.findByChatId(chatId);

        var email = telegramProfile.get().getEmail();
        var person = PersonDTO.builder()
                .email(email)
                .password(password)
                .build();
        Object result;
        try {
            result = tgAuthCallWebClint.doPost(CHECK_PASSWORD, person).block();
        } catch (Exception e) {
            log.error("WebClient doGet error: {}", e.getMessage());
            text = "Сервис авторизации не доступен попробуйте позже" + sl
                   + "/start";
            return new SendMessage(chatId, text);
        }

        var mapObject = tgConfig.getObjectToMap(result);
        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка: " + mapObject.get(ERROR_OBJECT);
            return new SendMessage(chatId, text);
        }

        telegramProfileService.updateSubscribedByChatId(chatId, true);
        text = "Подписка оформлена.";
        return new SendMessage(chatId, text);
    }
}
