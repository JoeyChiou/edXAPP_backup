package tw.openedu.www.task;

import android.content.Context;

import tw.openedu.www.discussion.CommentBody;
import tw.openedu.www.discussion.DiscussionComment;

public abstract class CreateCommentTask extends
Task<DiscussionComment> {

    CommentBody thread;

    public CreateCommentTask(Context context, CommentBody thread) {
        super(context);
        this.thread = thread;
    }



    public DiscussionComment call( ) throws Exception{
        try {

            if(thread!=null){

                return environment.getDiscussionAPI().createComment(thread);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
