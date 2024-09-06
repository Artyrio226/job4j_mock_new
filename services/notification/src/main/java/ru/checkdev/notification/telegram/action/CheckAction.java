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
public class CheckAction implements Action {
    private final TgAuthCallWebClint tgAuthCallWebClint;
    private final TgConfig tgConfig = new TgConfig("tg/", 8);

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

        text = "Ваше имя: " + person.getUsername() + sl
               + "Ваша почта: " + person.getEmail();
        return new SendMessage(chatId, text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }
}
