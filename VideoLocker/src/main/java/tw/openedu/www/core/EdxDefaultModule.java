package tw.openedu.www.core;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

import com.google.inject.AbstractModule;

import tw.openedu.www.base.MainApplication;
import tw.openedu.www.http.Api;
import tw.openedu.www.http.IApi;
import tw.openedu.www.http.RestApiManager;
import tw.openedu.www.module.analytics.ISegment;
import tw.openedu.www.module.analytics.ISegmentEmptyImpl;
import tw.openedu.www.module.analytics.ISegmentImpl;
import tw.openedu.www.module.analytics.ISegmentTracker;
import tw.openedu.www.module.analytics.ISegmentTrackerImpl;
import tw.openedu.www.module.db.IDatabase;
import tw.openedu.www.module.db.impl.IDatabaseImpl;
import tw.openedu.www.module.download.IDownloadManager;
import tw.openedu.www.module.download.IDownloadManagerImpl;
import tw.openedu.www.module.notification.DummyNotificationDelegate;
import tw.openedu.www.module.notification.NotificationDelegate;
import tw.openedu.www.module.notification.ParseNotificationDelegate;
import tw.openedu.www.module.storage.IStorage;
import tw.openedu.www.module.storage.Storage;
import tw.openedu.www.util.Config;

public class EdxDefaultModule extends AbstractModule {
    //if your module requires a context, add a constructor that will be passed a context.
    private Context context;

    //with RoboGuice 3.0, the constructor for AbstractModule will use an `Application`, not a `Context`
    public EdxDefaultModule(Context context) {
        this.context = context;
    }

    @Override
    public void configure() {
        Config config = new Config(context);

        bind(IDatabase.class).to(IDatabaseImpl.class);
        bind(IStorage.class).to(Storage.class);
        bind(ISegmentTracker.class).to(ISegmentTrackerImpl.class);
        if (config.getSegmentConfig().isEnabled()) {
            bind(ISegment.class).to(ISegmentImpl.class);
        }
        else {
            bind(ISegment.class).to(ISegmentEmptyImpl.class);
        }

        bind(IDownloadManager.class).to(IDownloadManagerImpl.class);

        if (MainApplication.RETROFIT_ENABLED ){
            bind(IApi.class).to(RestApiManager.class);
        } else {
            bind(IApi.class).to(Api.class);
        }

        if ( config.isNotificationEnabled() ) {
            Config.ParseNotificationConfig parseNotificationConfig =
                config.getParseNotificationConfig();
            if (parseNotificationConfig.isEnabled()) {
                bind(NotificationDelegate.class).to(ParseNotificationDelegate.class);
            }
            else {
                bind(NotificationDelegate.class).to(DummyNotificationDelegate.class);
            }
        }
        else {
            bind(NotificationDelegate.class).to(DummyNotificationDelegate.class);
        }

        bind(IEdxEnvironment.class).to(EdxEnvironment.class);

        bind(LinearLayoutManager.class).toProvider(LinearLayoutManagerProvider.class);

    }
}
