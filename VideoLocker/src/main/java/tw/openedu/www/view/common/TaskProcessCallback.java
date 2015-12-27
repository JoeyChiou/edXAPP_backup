package tw.openedu.www.view.common;

/**
 * Created by hanning on 4/30/15.
 */
public interface TaskProcessCallback {
    void startProcess();
    void finishProcess();
    void onMessage(MessageType messageType, String message);
}
