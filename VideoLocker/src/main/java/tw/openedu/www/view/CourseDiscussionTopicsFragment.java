package tw.openedu.www.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.inject.Inject;

import tw.openedu.www.R;
import tw.openedu.www.discussion.CourseTopics;
import tw.openedu.www.discussion.DiscussionTopic;
import tw.openedu.www.discussion.DiscussionTopicDepth;
import tw.openedu.www.logger.Logger;
import tw.openedu.www.model.api.EnrolledCoursesResponse;
import tw.openedu.www.task.GetTopicListTask;
import tw.openedu.www.third_party.iconify.IconView;
import tw.openedu.www.third_party.iconify.Iconify;
import tw.openedu.www.view.adapters.DiscussionTopicsAdapter;
import tw.openedu.www.view.custom.ETextView;

import java.util.ArrayList;
import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionTopicsFragment extends RoboFragment {

    @InjectView(R.id.discussion_topics_searchview)
    SearchView discussionTopicsSearchView;

    @InjectView(R.id.discussion_topics_listview)
    ListView discussionTopicsListView;

    @InjectView(R.id.discussion_following_icon)
    IconView followingIcon;

    @InjectView(R.id.discussion_topic_name_text_view)
    ETextView followingTextView;


    @InjectExtra(Router.EXTRA_COURSE_DATA)
    private EnrolledCoursesResponse courseData;


    @Inject
    DiscussionTopicsAdapter discussionTopicsAdapter;

    @Inject
    Router router;

    GetTopicListTask getTopicListTask;

    private static final Logger logger = new Logger(CourseDiscussionTopicsFragment.class.getName());


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_topics, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Iconify.IconValue iconValue = Iconify.IconValue.fa_star;
        followingIcon.setIcon(iconValue);

        followingTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DiscussionTopic discussionTopic = new DiscussionTopic();
                discussionTopic.setName(DiscussionTopic.FOLLOWING_TOPICS);
                router.showCourseDiscussionPostsForDiscussionTopic(getActivity(), discussionTopic, courseData);
            }
        });

        discussionTopicsListView.setAdapter(discussionTopicsAdapter);

        discussionTopicsSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query == null || query.trim().length() == 0)
                    return false;

                router.showCourseDiscussionPostsForSearchQuery(getActivity(), query, courseData);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        discussionTopicsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DiscussionTopicDepth discussionTopicDepth = discussionTopicsAdapter.getItem(position);
                DiscussionTopic discussionTopic = discussionTopicDepth.getDiscussionTopic();
                router.showCourseDiscussionPostsForDiscussionTopic(getActivity(), discussionTopic, courseData);
            }
        });

        // TODO: Find a better way to hide the keyboard AND take the focus away from the SearchView
        discussionTopicsSearchView.requestFocus();
        discussionTopicsSearchView.clearFocus();


        getTopicList();

    }

    private void getTopicList() {
        if (getTopicListTask != null) {
            getTopicListTask.cancel(true);
        }
        getTopicListTask = new GetTopicListTask(getActivity(), courseData.getCourse().getId()) {
            @Override
            public void onSuccess(CourseTopics courseTopics) {
                if (courseTopics != null) {
                    logger.debug("GetTopicListTask success=" + courseTopics);
                    //  hideProgress();
                    ArrayList<DiscussionTopic> allTopics = new ArrayList<>();
                    allTopics.addAll(courseTopics.getCoursewareTopics());
                    allTopics.addAll(courseTopics.getNonCoursewareTopics());

                    List<DiscussionTopicDepth> allTopicsWithDepth = DiscussionTopicDepth.createFromDiscussionTopics(allTopics);
                    discussionTopicsAdapter.setItems(allTopicsWithDepth);
                    discussionTopicsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                //  hideProgress();
            }
        };
        getTopicListTask.execute();
    }
}