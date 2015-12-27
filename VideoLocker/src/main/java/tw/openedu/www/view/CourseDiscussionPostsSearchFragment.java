package tw.openedu.www.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tw.openedu.www.R;
import tw.openedu.www.discussion.TopicThreads;

import tw.openedu.www.task.SearchThreadListTask;
import tw.openedu.www.view.common.MessageType;
import tw.openedu.www.view.common.TaskProcessCallback;

import roboguice.inject.InjectExtra;

public class CourseDiscussionPostsSearchFragment extends CourseDiscussionPostsBaseFragment {

    @InjectExtra(value = Router.EXTRA_SEARCH_QUERY, optional = true)
    private String searchQuery;
    private SearchThreadListTask searchThreadListTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_search_posts, container, false);
    }


    @Override
    protected void populateThreadList(final boolean refreshView) {

        if ( searchThreadListTask != null ){
            searchThreadListTask.cancel(true);
        }
        searchThreadListTask = new SearchThreadListTask(getActivity(), courseData.getCourse().getId(), searchQuery, discussionPostsAdapter.getPagination()) {
            @Override
            public void onSuccess(TopicThreads topicThreads) {
                if ( refreshView ){
                    discussionPostsAdapter.setItems(null);
                }
                boolean hasMore = topicThreads.next != null && topicThreads.next.length() > 0;
                discussionPostsAdapter.addPage(topicThreads.getResults(), hasMore);
                refreshListViewOnDataChange();
               // discussionPostsAdapter.notifyDataSetChanged();
                if ( discussionPostsAdapter.getCount() == 0 ){
                    Activity activity = getActivity();
                    if ( activity instanceof TaskProcessCallback){
                        String escapedTitle = TextUtils.htmlEncode(searchQuery);
                        String resultsTextFormat = getContext().getResources().getString(R.string.forum_query_no_result);
                        String resultsText = String.format(resultsTextFormat, escapedTitle);
                       // CharSequence styledResults = Html.fromHtml(resultsText);
                        ((TaskProcessCallback)activity).onMessage(MessageType.ERROR, resultsText);
                    }
                }
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                //  hideProgress();

            }
        };

        searchThreadListTask.execute();

    }
}
