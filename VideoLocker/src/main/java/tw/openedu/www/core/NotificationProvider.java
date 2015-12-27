package tw.openedu.www.core;

import com.google.inject.Inject;
import com.google.inject.Provider;

import tw.openedu.www.module.notification.DummyNotificationDelegate;
import tw.openedu.www.module.notification.NotificationDelegate;
import tw.openedu.www.module.notification.ParseNotificationDelegate;
import tw.openedu.www.util.Config;

/**
 * Created by hanning on 6/22/15.
 */
public class NotificationProvider  implements Provider<NotificationDelegate> {

    @Inject
    Config config;

    @Override
    public NotificationDelegate get() {
        if ( config.isNotificationEnabled() ) {
            Config.ParseNotificationConfig parseNotificationConfig =
                config.getParseNotificationConfig();
            if (parseNotificationConfig.isEnabled()) {
                return new ParseNotificationDelegate();
            }
            else {
                return new DummyNotificationDelegate();
            }
        }
        else {
            return new DummyNotificationDelegate();
        }
    }
}
