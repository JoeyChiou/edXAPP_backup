package tw.openedu.www.task;

import android.content.Context;

import tw.openedu.www.discussion.DiscussionThread;

public abstract class FollowThreadTask extends
Task<DiscussionThread> {

    DiscussionThread thread;
    Boolean voted;

    public FollowThreadTask(Context context, DiscussionThread thread, Boolean voted) {
        super(context);
        this.thread = thread;
        this.voted = voted;
    }



    public DiscussionThread call( ) throws Exception{
        try {

            if(thread!=null){

                return environment.getDiscussionAPI().followThread(thread, voted);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
