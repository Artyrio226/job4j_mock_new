package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

@AllArgsConstructor
@Slf4j
public class ForgetAction implements Action {

    private static final String ERROR_OBJECT = "error";
    private static final String URL_AUTH_FORGOT = "/forgot";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint tgAuthCallWebClint;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var userChatId = message.getFrom().getId();
        var sl = System.lineSeparator();
        var text = "";

        Object result;
        try {
            result = tgAuthCallWebClint.doGet("/profiles/chat/" + userChatId).block();
        } catch (Exception e) {
            log.error("WebClient doGet error: {}", e.getMessage());
            text = "Сервис авторизации не доступен попробуйте позже" + sl
                   + "/start";
            return new SendMessage(chatId, text);
        }
        var person = tgConfig.getObjectToPersonDTO(result);
        if (person == null) {
            text = "Вы не зарегистрированы в системе.";
            return new SendMessage(chatId, text);
        }

        var password = tgConfig.getPassword();
        person.setPassword(password);
        try {
            result = tgAuthCallWebClint.doPost(URL_AUTH_FORGOT, person).block();
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                   + "/start";
            return new SendMessage(chatId, text);
        }

        var mapObject = tgConfig.getObjectToMap(result);

        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка восстановления пароля: " + mapObject.get(ERROR_OBJECT);
            return new SendMessage(chatId, text);
        }

        text = "Ваш Логин: " + person.getEmail() + sl
               + "Новый пароль: " + person.getPassword();
        return new SendMessage(chatId, text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }
}
