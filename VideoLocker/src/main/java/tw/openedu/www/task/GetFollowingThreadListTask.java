package tw.openedu.www.task;

import android.content.Context;

import tw.openedu.www.discussion.DiscussionPostsFilter;
import tw.openedu.www.discussion.DiscussionPostsSort;
import tw.openedu.www.discussion.TopicThreads;

import tw.openedu.www.view.adapters.IPagination;

public abstract class GetFollowingThreadListTask extends
Task<TopicThreads> {

    String courseId;
    DiscussionPostsSort orderBy;
    DiscussionPostsFilter filter;
    IPagination pagination;

    public GetFollowingThreadListTask(Context context, String courseId,
                                      DiscussionPostsFilter filter,
                                      DiscussionPostsSort orderBy,
                                      IPagination pagination) {
        super(context);
        this.courseId = courseId;
        this.orderBy = orderBy;
        this.filter = filter;
        this.pagination = pagination;
    }



    public TopicThreads call( ) throws Exception{
        try {
            if(courseId!=null){

                String view;
                if (filter == DiscussionPostsFilter.Unread) view = "unread";
                else if (filter == DiscussionPostsFilter.Unanswered) view = "unanswered";
                else view = "";

                String order;
                if (orderBy == DiscussionPostsSort.LastActivityAt) order = "last_activity_at";
                else if (orderBy == DiscussionPostsSort.VoteCount) order = "vote_count";
                else order = "";

                int pageSize = pagination.pageSize();
                int page = pagination.numOfPagesLoaded() + 1;
                return environment.getDiscussionAPI().getFollowingThreadList(courseId, view, order, pageSize, page);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
