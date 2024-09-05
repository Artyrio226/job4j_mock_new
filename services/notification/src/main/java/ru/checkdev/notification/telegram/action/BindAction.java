package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Calendar;

@AllArgsConstructor
@Slf4j
public class BindAction implements Action {

    private static final String ERROR_OBJECT = "error";
    private static final String URL_AUTH_BIND = "/bind";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint tgAuthCallWebClint;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var text = "Введите логин и пароль через пробел для привязки аккаунта telegram к платформе CheckDev";
        return new SendMessage(chatId, text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        var sl = System.lineSeparator();
        var chatId = message.getChatId().toString();
        var text = message.getText();
        var userName = message.getFrom().getUserName();
        var userChatId = message.getFrom().getId();
        var parts = text.split(" ");
        if (parts.length == 2) {
            var login = parts[0];
            var password = parts[1];
            var person = PersonDTO.builder()
                    .username(userName)
                    .email(login)
                    .password(password)
                    .userChatId(userChatId)
                    .privacy(true)
                    .created(Calendar.getInstance())
                    .build();
            Object result;
            try {
                result = tgAuthCallWebClint.doPost(URL_AUTH_BIND, person).block();
            } catch (Exception e) {
                log.error("WebClient doPost error: {}", e.getMessage());
                text = "Сервис авторизации не доступен попробуйте позже" + sl
                       + "/start";
                return new SendMessage(chatId, text);
            }
            var mapObject = tgConfig.getObjectToMap(result);
            if (mapObject.containsKey(ERROR_OBJECT)) {
                text = "Ошибка привязки: " + mapObject.get(ERROR_OBJECT);
                return new SendMessage(chatId, text);
            }
            text = "Аккаунт telegram привязан к платформе CheckDev";
            return new SendMessage(chatId, text);
        } else {
            text = "Неправильный формат ввода";
            return new SendMessage(chatId, text);
        }
    }
}
