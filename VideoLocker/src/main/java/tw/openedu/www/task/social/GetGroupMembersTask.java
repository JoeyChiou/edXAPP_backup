package tw.openedu.www.task.social;

import android.content.Context;

import tw.openedu.www.services.ServiceManager;
import tw.openedu.www.social.SocialMember;
import tw.openedu.www.task.Task;

import java.util.List;

/**
 * Created by marcashman on 2014-12-18.
 */
public abstract class GetGroupMembersTask extends Task<List<SocialMember>> {

    private long groupId;

    public GetGroupMembersTask(Context context, long groupId) {
        super(context);
        this.groupId = groupId;
    }

    @Override
    public List<SocialMember> call( ) {
        ServiceManager api = environment.getServiceManager();
        try {
            return api.getGroupMembers(false, groupId);
        } catch (Exception e) {
            logger.error(e);
            onException(e);
        }
        return null;
    }
}
