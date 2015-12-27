package tw.openedu.www.task;

import android.content.Context;

import tw.openedu.www.model.api.ResetPasswordResponse;
import tw.openedu.www.services.ServiceManager;

public abstract class ResetPasswordTask extends Task<ResetPasswordResponse> {

    String emailId;
    public ResetPasswordTask(Context context,String emailId) {
        super(context);
        this.emailId = emailId;
    }

    @Override
    public ResetPasswordResponse call() throws Exception{
        try {

            if(emailId!=null){
                ServiceManager api = environment.getServiceManager();
                ResetPasswordResponse res = api.resetPassword(emailId);
                return res;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
