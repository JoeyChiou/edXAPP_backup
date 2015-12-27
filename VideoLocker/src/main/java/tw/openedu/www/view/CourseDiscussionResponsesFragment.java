package tw.openedu.www.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;

import tw.openedu.www.R;
import tw.openedu.www.discussion.DiscussionThread;
import tw.openedu.www.discussion.ThreadComments;

import tw.openedu.www.discussion.DiscussionCommentPostedEvent;
import tw.openedu.www.logger.Logger;
import tw.openedu.www.model.api.EnrolledCoursesResponse;
import tw.openedu.www.task.GetCommentListTask;
import tw.openedu.www.view.adapters.CourseDiscussionResponsesAdapter;
import tw.openedu.www.view.adapters.IPagination;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionResponsesFragment extends RoboFragment implements CourseDiscussionResponsesAdapter.PaginationHandler{

    @Inject
    LinearLayoutManager linearLayoutManager;

    @InjectView(R.id.discussion_responses_recycler_view)
    RecyclerView discussionResponsesRecyclerView;

    @InjectView(R.id.create_new_item_text_view)
    TextView addResponseTextView;

    @InjectView(R.id.create_new_item_relative_layout)
    RelativeLayout addResponseLayout;

    @InjectExtra(Router.EXTRA_DISCUSSION_THREAD)
    DiscussionThread discussionThread;

    @InjectExtra(value = Router.EXTRA_COURSE_DATA, optional = true)
    EnrolledCoursesResponse courseData;

    @Inject
    CourseDiscussionResponsesAdapter courseDiscussionResponsesAdapter;

    @Inject
    Router router;

    private final Logger logger = new Logger(getClass().getName());
    private GetCommentListTask getCommentListTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_responses, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionResponsesRecyclerView.setLayoutManager(linearLayoutManager);

        courseDiscussionResponsesAdapter.setPaginationHandler(this);
        courseDiscussionResponsesAdapter.setDiscussionThread(discussionThread);
        discussionResponsesRecyclerView.setAdapter(courseDiscussionResponsesAdapter);

        getCommentList(true);

        addResponseTextView.setText(R.string.discussion_responses_add_response_button);

        addResponseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                router.showCourseDiscussionAddResponse(getActivity(), discussionThread);
            }
        });
    }

    protected void getCommentList(final boolean refresh) {

        if ( getCommentListTask != null ){
            getCommentListTask.cancel(true);
        }
        getCommentListTask = new GetCommentListTask(getActivity(),
                                                    discussionThread.getIdentifier(),
                                                    courseDiscussionResponsesAdapter.getPagination()) {
            @Override
            public void onSuccess(ThreadComments threadComments) {
                if ( threadComments == null ) {
                    logger.debug("GetCommentListTask returns null onSuccess");
                    return;// should not happen?
                }
                if ( refresh ){
                    //it clear up details and reset pagination
                    courseDiscussionResponsesAdapter.setDiscussionThread(discussionThread);
                }
                boolean hasMore = threadComments.next != null && threadComments.next.length() > 0;
                courseDiscussionResponsesAdapter.addPage(threadComments.getResults(), hasMore);
                courseDiscussionResponsesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                //  hideProgress();
            }
        };
        getCommentListTask.execute();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(DiscussionCommentPostedEvent event) {
        if (discussionThread.containsComment(event.getComment())) {
            discussionThread.incrementCommentCount();
            courseDiscussionResponsesAdapter.notifyDataSetChanged();
            getCommentList(true);
        }
    }

    /**
     *  callback from CourseDiscussionResponsesAdapter
     */
    @Override
    public void loadMoreRecord(IPagination pagination) {
        getCommentList(false);
    }
}
