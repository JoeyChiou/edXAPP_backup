package tw.openedu.www.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.widget.FacebookDialog;

import tw.openedu.www.R;
import tw.openedu.www.exception.AuthException;
import tw.openedu.www.loader.AsyncTaskResult;
import tw.openedu.www.loader.CoursesAsyncLoader;
import tw.openedu.www.model.api.EnrolledCoursesResponse;
import tw.openedu.www.module.prefs.PrefManager;
import tw.openedu.www.services.FetchCourseFriendsService;
import tw.openedu.www.social.facebook.FacebookProvider;
import tw.openedu.www.module.facebook.FacebookSessionUtil;
import tw.openedu.www.view.dialog.InstallFacebookDialog;

import java.util.ArrayList;
import java.util.List;

public class MyFriendsCoursesTabFragment extends CourseListTabFragment implements View.OnClickListener  {

    private static final String TAG = MyCourseListTabFragment.class.getSimpleName();

    private final int FREINDS_COURSES_LOADER_ID = 0x605000;
    private LinearLayout noFriendsLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            environment.getSegment().screenViewsTracking(getString(R.string.label_my_friends_courses));
        }catch(Exception e){
            logger.error(e);
        }

    }

    @Override
    public void handleCourseClick(EnrolledCoursesResponse model) {

        try {

            Intent friendsInGroupIntent = new Intent(getActivity(),
                    FriendsInCourseActivity.class);
            friendsInGroupIntent.putExtra(FriendsInCourseActivity.EXTRA_COURSE, model.getCourse());
            friendsInGroupIntent.putExtra(FriendsInCourseActivity.EXTRA_FRIENDS_TAB_LINK, true);

            startActivity(friendsInGroupIntent);

        } catch(Exception ex) {
            logger.error(ex);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);
        noFriendsLayout = (LinearLayout) view.findViewById(R.id.friends_course_no_friends_layout);
        TextView shareBtn = (TextView) view.findViewById(R.id.friends_course_no_btn_share_app);
        shareBtn.setOnClickListener(this);

        return view;

    }

    @Override
    protected int getViewResourceID() {
        return R.layout.fragment_my_friends_course_list_tab;
    }

    protected void loadData(boolean forceRefresh,boolean showProgress) {

        if(forceRefresh){
            Intent clearFriends = new Intent(getActivity(), FetchCourseFriendsService.class);

            clearFriends.putExtra(FetchCourseFriendsService.TAG_FORCE_REFRESH, true);

            getActivity().startService(clearFriends);
        }

        Bundle args = new Bundle();
        args.putString(CoursesAsyncLoader.TAG_COURSE_OAUTH, FacebookSessionUtil.getAccessToken());

        getLoaderManager().restartLoader(FREINDS_COURSES_LOADER_ID, args, this);

    }


    @Override
    public Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> onCreateLoader(int i, Bundle bundle) {

        return new CoursesAsyncLoader(getActivity(), bundle, environment, environment.getServiceManager());

    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> asyncTaskResultLoader, AsyncTaskResult<List<EnrolledCoursesResponse>> result) {

        if (progressBar != null) progressBar.setVisibility(View.GONE);

        if(result.getEx() != null)
        {

            if(result.getEx() instanceof AuthException){
                PrefManager prefs = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
                prefs.clearAuth();

                logger.error(result.getEx());
                getActivity().finish();
            } else {
                adapter.clear();
                noFriendsLayout.setVisibility(View.VISIBLE);
                swipeLayout.setVisibility(View.GONE);
            }

        } else if (result.getResult() != null) {
            invalidateSwipeFunctionality();

            ArrayList<EnrolledCoursesResponse> newItems = new ArrayList<EnrolledCoursesResponse>(result.getResult());

            ((MyCoursesListActivity)getActivity()).updateDatabaseAfterDownload(newItems);

            if(result.getResult().size() == 0){
                adapter.clear();
                noFriendsLayout.setVisibility(View.VISIBLE);
                swipeLayout.setVisibility(View.GONE);
            } else {
                noFriendsLayout.setVisibility(View.GONE);
                swipeLayout.setVisibility(View.VISIBLE);
                adapter.setItems(newItems);
                adapter.notifyDataSetChanged();
            }

        } else {
            adapter.clear();
            noFriendsLayout.setVisibility(View.VISIBLE);
            swipeLayout.setVisibility(View.GONE);

        }

    }

    @Override
    public void onLoaderReset(Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> asyncTaskResultLoader) {
        adapter.clear();
        adapter.notifyDataSetChanged();
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.friends_course_no_btn_share_app:
                FacebookProvider fbProvider = new FacebookProvider();
                FacebookDialog dialog = (FacebookDialog) fbProvider.shareApplication(getActivity());
                if (dialog != null) {
                    uiHelper.trackPendingDialogCall(dialog.present());
                } else {
                    new InstallFacebookDialog().show(getFragmentManager(), null);
                }
        }

    }

}
