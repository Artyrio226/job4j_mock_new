/**
 *
 */
package ru.checkdev.notification.service;

import org.junit.Test;
import ru.checkdev.notification.domain.Notify;


/**
 * @author olegbelov
 *
 */
public class NotificationServiceTest {

    @Test
    public void whenReadQueue() {
        TemplateService templates = new TemplateService(null, null) {
            @Override
            public Notify send(Notify user) {
                System.out.println(user.getEmail());
                System.out.println(user.getTemplate());
                return user;
            }
        };
    }


}
