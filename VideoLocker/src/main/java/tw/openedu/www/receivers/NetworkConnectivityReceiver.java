package tw.openedu.www.receivers;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import tw.openedu.www.core.IEdxEnvironment;
import tw.openedu.www.event.NetworkConnectivityChangeEvent;
import tw.openedu.www.logger.Logger;
import tw.openedu.www.model.DownloadDescriptor;
import tw.openedu.www.module.analytics.ISegment;
import tw.openedu.www.services.DownloadSpeedService;
import tw.openedu.www.util.NetworkUtil;

import de.greenrobot.event.EventBus;
import roboguice.receiver.RoboBroadcastReceiver;

/**
 * Created by yervant on 1/15/15.
 */
@Singleton
public class NetworkConnectivityReceiver extends RoboBroadcastReceiver {

    private static final Logger logger = new Logger(NetworkConnectivityReceiver.class);
    private static boolean isFirstStart = false;

    @Inject
    IEdxEnvironment environment;

    @Override
    public void handleReceive(Context context, Intent intent) {
        // speed-test is moved behind a flag in the configuration
        if(environment.getConfig().isSpeedTestEnabled()) {
            ConnectivityManager cm =
                    (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isAvailable();

            if (isConnected) {
                logger.debug("Have reconnected, testing download speed.");
                //start an instance of the download speed service so it can run in the background
                Intent speedTestIntent = new Intent(context, DownloadSpeedService.class);
                String downloadEndpoint = context.getString(tw.openedu.www.R.string.speed_test_url);
                speedTestIntent.putExtra(DownloadSpeedService.EXTRA_FILE_DESC,
                        new DownloadDescriptor(downloadEndpoint, !isFirstStart));
                context.startService(speedTestIntent);
                isFirstStart = true;
            }
        }

        //Track the connection change. Record if the user is on a cell network.
        if (NetworkUtil.isConnectedMobile(context)){

            ISegment segIO = environment.getSegment();

            TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            String carrierName = manager.getNetworkOperatorName();

            segIO.trackUserCellConnection(carrierName, NetworkUtil.isOnZeroRatedNetwork(context, environment.getConfig()));
        }
        NetworkConnectivityChangeEvent event = new NetworkConnectivityChangeEvent();
        EventBus.getDefault().post(event);

    }
}
