package tw.openedu.www.view;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.inject.Inject;

import tw.openedu.www.discussion.DiscussionThread;

import tw.openedu.www.model.api.EnrolledCoursesResponse;
import tw.openedu.www.view.adapters.BaseListAdapter;
import tw.openedu.www.view.adapters.DiscussionPostsAdapter;
import tw.openedu.www.view.adapters.IPagination;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;


public abstract class CourseDiscussionPostsBaseFragment extends RoboFragment implements BaseListAdapter.PaginationHandler {

    @InjectView(tw.openedu.www.R.id.discussion_posts_listview)
    ListView discussionPostsListView;

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    EnrolledCoursesResponse courseData;

    @Inject
    DiscussionPostsAdapter discussionPostsAdapter;

    @Inject
    Router router;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        discussionPostsAdapter.setPaginationHandler(this);
        discussionPostsListView.setAdapter(discussionPostsAdapter);

        discussionPostsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DiscussionThread thread = discussionPostsAdapter.getItem(position);
                router.showCourseDiscussionResponses(getActivity(), thread, courseData);
            }
        });

        populateThreadList(true);
    }

    /**
     * setAdapter calls mRecycler.clear();  which invalidate the recycled views
     */
    protected void refreshListViewOnDataChange(){
        discussionPostsListView.setAdapter(discussionPostsAdapter);
    }

    protected abstract void populateThreadList(boolean refreshView);

    public void loadMoreRecord(IPagination pagination){
        populateThreadList(false);
    }

}
