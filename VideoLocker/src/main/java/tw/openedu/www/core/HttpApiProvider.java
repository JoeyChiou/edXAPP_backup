package tw.openedu.www.core;

import android.app.Application;

import com.google.inject.Inject;
import com.google.inject.Provider;

import tw.openedu.www.base.MainApplication;
import tw.openedu.www.http.Api;
import tw.openedu.www.http.IApi;
import tw.openedu.www.http.RestApiManager;
import tw.openedu.www.util.Config;

/**
 * Created by hanning on 6/22/15.
 */
public class HttpApiProvider implements Provider<IApi> {

    @Inject
    Application application;
    @Inject
    Config config;

    @Override
    public IApi get() {
        if (MainApplication.RETROFIT_ENABLED ){
            return new RestApiManager(application);
        } else {
            return new Api(application);
        }
    }
}
