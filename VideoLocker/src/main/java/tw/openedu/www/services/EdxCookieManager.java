package tw.openedu.www.services;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;

import tw.openedu.www.base.MainApplication;
import tw.openedu.www.event.SessionIdRefreshEvent;
import tw.openedu.www.logger.Logger;
import tw.openedu.www.module.prefs.PrefManager;
import tw.openedu.www.task.GetSessesionExchangeCookieTask;

import java.io.File;
import java.net.HttpCookie;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 *  A central place for course data model transformation
 */
public class EdxCookieManager {

    protected final Logger logger = new Logger(getClass().getName());

    private static EdxCookieManager instance;

    private GetSessesionExchangeCookieTask task;

    public static synchronized EdxCookieManager getSharedInstance(){
        if ( instance == null )
            instance = new EdxCookieManager();
        return instance;
    }

    public void clearWebWiewCookie(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.webkit.CookieManager.getInstance().removeAllCookies(new ValueCallback() {
                @Override
                public void onReceiveValue(Object value) {
                    //do nothing?
                }
            });
        } else {
            try {
                CookieSyncManager.createInstance(context);
                android.webkit.CookieManager.getInstance().removeAllCookie();
            }catch (Exception ex){
                logger.debug(ex.getMessage());
            }
        }
        PrefManager pref = new PrefManager(MainApplication.instance(), PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.AUTH_ASSESSMENT_SESSION_ID, "");

    }

    public void clearWebViewCache(Context context){
        try {
            clearWebWiewCookie(context);

            boolean success = context.deleteDatabase("webview.db");
            logger.debug("delete webview.db result = " + success);
            success =  context.deleteDatabase("webviewCache.db");
            logger.debug("delete webviewCache.db result = " + success);
        } catch (Exception e) {
            logger.error(e);
        }


        File webviewCacheDir = new File(context.getCacheDir().getAbsolutePath()+"/webviewCache");
        if(webviewCacheDir.exists()){
            context.deleteFile(webviewCacheDir.getAbsolutePath());
        }

        File appCacheDir = new File(context.getFilesDir().getAbsolutePath()+ "/webcache");
        if(appCacheDir.exists()){
            context.deleteFile(appCacheDir.getAbsolutePath());
        }
    }

    public synchronized  void tryToRefreshSessionCookie( ){
        if ( task == null || task.isCancelled()  ) {
            task =new GetSessesionExchangeCookieTask(MainApplication.instance()) {
                @Override
                public void onSuccess(List<HttpCookie> result) {
                    if (result == null || result.isEmpty()) {
                        logger.debug("result is empty");
                        EventBus.getDefault().post(new SessionIdRefreshEvent(false));
                        return;
                    }
                    
                    for (HttpCookie cookie : result) {
                        if (cookie.getName().equals(PrefManager.Key.SESSION_ID)) {
                            clearWebWiewCookie(context);
                            PrefManager pref = new PrefManager(MainApplication.instance(), PrefManager.Pref.LOGIN);
                            pref.put(PrefManager.Key.AUTH_ASSESSMENT_SESSION_ID, cookie.getValue());
                            EventBus.getDefault().post(new SessionIdRefreshEvent(true));
                            break;
                        }
                    }
                    task = null;
                }

                @Override
                public void onException(Exception ex) {
                    logger.error(ex);
                    EventBus.getDefault().post(new SessionIdRefreshEvent(false));
                    task = null;
                }
            };
            task.execute();
        }
    }
}
