package tw.openedu.www.core;


import tw.openedu.www.discussion.DiscussionAPI;

import tw.openedu.www.module.analytics.ISegment;
import tw.openedu.www.module.db.IDatabase;
import tw.openedu.www.module.download.IDownloadManager;
import tw.openedu.www.module.notification.NotificationDelegate;
import tw.openedu.www.module.prefs.UserPrefs;
import tw.openedu.www.module.storage.IStorage;
import tw.openedu.www.services.ServiceManager;
import tw.openedu.www.util.Config;
import tw.openedu.www.util.images.ImageCacheManager;
import tw.openedu.www.view.Router;

/**
 * TODO - we should decompose this class into environment setting and service provider settings.
 */
public interface IEdxEnvironment {

    IDatabase getDatabase();

    IStorage getStorage();

    IDownloadManager getDownloadManager();

    ImageCacheManager getImageCacheManager();

    UserPrefs getUserPrefs();

    ISegment getSegment();

    NotificationDelegate getNotificationDelegate();

    Router getRouter();

    Config getConfig();

    ServiceManager getServiceManager();

    //TODO - it should be part of ServiceManager
    DiscussionAPI getDiscussionAPI();
}
