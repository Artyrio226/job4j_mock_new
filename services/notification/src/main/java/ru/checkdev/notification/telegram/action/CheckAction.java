package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.service.TelegramProfileService;

@AllArgsConstructor
@Slf4j
public class CheckAction implements Action {
    private final TelegramProfileService telegramProfileService;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var text = "";
        var sl = System.lineSeparator();

        var telegramProfile = telegramProfileService.findByChatId(chatId);
        if (telegramProfile.isEmpty()) {
            log.error("Не найден TelegramProfile по userId");
            text = "Вы не зарегистрированы в системе." + sl
                   + "/start";
            return new SendMessage(chatId, text);
        }

        text = "Ваше имя: " + message.getFrom().getFirstName() + sl
               + "Ваша почта: " + telegramProfile.get().getEmail();
        return new SendMessage(chatId, text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }
}
