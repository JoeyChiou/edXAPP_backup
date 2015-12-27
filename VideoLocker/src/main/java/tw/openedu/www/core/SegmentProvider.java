package tw.openedu.www.core;

import android.app.Application;

import com.google.inject.Inject;
import com.google.inject.Provider;

import tw.openedu.www.module.analytics.ISegment;
import tw.openedu.www.module.analytics.ISegmentEmptyImpl;
import tw.openedu.www.module.analytics.ISegmentImpl;
import tw.openedu.www.util.Config;

/**
 * Created by hanning on 6/22/15.
 */
public class SegmentProvider implements Provider<ISegment> {

    @Inject
    Application application;
    @Inject
    Config config;

    @Override
    public ISegment get() {
        if (config.getSegmentConfig().isEnabled()) {
            return new ISegmentImpl( );
        }
        else {
           return new ISegmentEmptyImpl();
        }
    }
}
