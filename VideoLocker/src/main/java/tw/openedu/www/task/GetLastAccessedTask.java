package tw.openedu.www.task;

import android.content.Context;

import tw.openedu.www.model.api.SyncLastAccessedSubsectionResponse;
import tw.openedu.www.services.ServiceManager;

public abstract class GetLastAccessedTask extends Task<SyncLastAccessedSubsectionResponse> {

    String courseId;
    public GetLastAccessedTask(Context context,  String courseId) {
        super(context);
        this.courseId = courseId;
    }

    @Override
    public SyncLastAccessedSubsectionResponse call() throws Exception{
        try {

            if(courseId!=null){
                ServiceManager api = environment.getServiceManager();
                SyncLastAccessedSubsectionResponse res = api.getLastAccessedSubsection(courseId);
                return res;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
