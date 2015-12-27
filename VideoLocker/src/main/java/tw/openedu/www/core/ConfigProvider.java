package tw.openedu.www.core;

import android.app.Application;

import com.google.inject.Inject;
import com.google.inject.Provider;

import tw.openedu.www.util.Config;

/**
 * Created by hanning on 6/22/15.
 */
public class ConfigProvider implements Provider<Config> {

    @Inject
    Application application;


    @Override
    public Config get() {
        return new Config(application);
    }
}
