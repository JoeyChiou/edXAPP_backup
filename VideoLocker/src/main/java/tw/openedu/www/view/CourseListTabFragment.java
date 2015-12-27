package tw.openedu.www.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.facebook.Session;
import com.facebook.SessionState;
import com.google.inject.Inject;

import tw.openedu.www.base.BaseFragmentActivity;
import tw.openedu.www.core.IEdxEnvironment;
import tw.openedu.www.interfaces.NetworkObserver;
import tw.openedu.www.interfaces.NetworkSubject;
import tw.openedu.www.loader.AsyncTaskResult;
import tw.openedu.www.logger.Logger;
import tw.openedu.www.model.api.EnrolledCoursesResponse;
import tw.openedu.www.module.facebook.FacebookSessionUtil;
import tw.openedu.www.module.facebook.IUiLifecycleHelper;
import tw.openedu.www.module.prefs.PrefManager;
import tw.openedu.www.services.FetchCourseFriendsService;
import tw.openedu.www.social.SocialMember;
import tw.openedu.www.social.SocialProvider;
import tw.openedu.www.social.facebook.FacebookProvider;
import tw.openedu.www.util.ViewAnimationUtil;
import tw.openedu.www.util.NetworkUtil;
import tw.openedu.www.view.adapters.MyCourseAdapter;
import tw.openedu.www.view.custom.ETextView;
import tw.openedu.www.view.dialog.FindCoursesDialogFragment;

import java.util.List;

import roboguice.fragment.RoboFragment;

public abstract class CourseListTabFragment extends RoboFragment implements NetworkObserver, MyCourseAdapter.CourseFriendsListener, LoaderManager.LoaderCallbacks<AsyncTaskResult<List<EnrolledCoursesResponse>>> {

    protected MyCourseAdapter adapter;

    protected SwipeRefreshLayout swipeLayout;
    protected LinearLayout offlinePanel;
    protected View offlineBar;
    protected ProgressBar progressBar;

    protected PrefManager pmFeatures;

    @Inject
    protected IEdxEnvironment environment;


    protected IUiLifecycleHelper uiHelper;
    protected ListView myCourseList;

    FetchFriendsReceiver fetchFriendsObserver;

    protected Logger logger = new Logger(getClass().getSimpleName());

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof NetworkSubject) {
            ((NetworkSubject)activity).registerNetworkObserver(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (getActivity() instanceof NetworkSubject) {
            ((NetworkSubject)getActivity()).unregisterNetworkObserver(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fetchFriendsObserver = new FetchFriendsReceiver();

        SocialProvider fbProvider = new FacebookProvider();
        pmFeatures = new PrefManager(getActivity(), PrefManager.Pref.FEATURES);
        boolean showSocialFeatures = fbProvider.isLoggedIn() && pmFeatures.getBoolean(PrefManager.Key.ALLOW_SOCIAL_FEATURES, true);
        adapter = new MyCourseAdapter(getActivity(), showSocialFeatures, this, environment) {

            @Override
            public void onItemClicked(EnrolledCoursesResponse model) {
                handleCourseClick(model);
            }

            @Override
            public void onAnnouncementClicked(EnrolledCoursesResponse model) {
                environment.getRouter().showCourseDetailTabs(getActivity(), environment.getConfig(), model, true);
            }
        };
        adapter.setImageCacheManager(environment.getImageCacheManager());

        uiHelper = IUiLifecycleHelper.Factory.getInstance(getActivity(), new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {

                if (state.isOpened()) {
                    adapter.notifyDataSetChanged();
                }


            }
        });
        uiHelper.onCreate(savedInstanceState);
        loadData(false,false);
    }

    public abstract void handleCourseClick( EnrolledCoursesResponse model);

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        uiHelper.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        uiHelper.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onPause() {
        uiHelper.onPause();
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getViewResourceID(), container, false);

        offlineBar = view.findViewById(tw.openedu.www.R.id.offline_bar);
        offlinePanel = (LinearLayout) view.findViewById(tw.openedu.www.R.id.offline_panel);
        progressBar = (ProgressBar) view.findViewById(tw.openedu.www.R.id.api_spinner);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(tw.openedu.www.R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Hide the progress bar as swipe functionality has its own Progress indicator
                if(progressBar!=null){
                    progressBar.setVisibility(View.GONE);
                }
                loadData(true,false);
            }
        });

        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                tw.openedu.www.R.color.grey_act_background , tw.openedu.www.R.color.grey_act_background ,
                tw.openedu.www.R.color.grey_act_background);

        myCourseList = (ListView) view.findViewById(tw.openedu.www.R.id.my_course_list);
        //As per docs, the footer needs to be added before adapter is set to the ListView
        setupFooter(myCourseList);
        myCourseList.setAdapter(adapter);
        myCourseList.setOnItemClickListener(adapter);

        if (!(NetworkUtil.isConnected(getActivity()))) {
            onOffline();
        } else {
            onOnline();
        }

        return view;
    }

    protected abstract int getViewResourceID();

    protected abstract void loadData(boolean forceRefresh, boolean showProgress);

    protected void invalidateSwipeFunctionality(){
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void onOnline() {
        if (offlineBar != null && swipeLayout != null) {
            offlineBar.setVisibility(View.GONE);
            hideOfflinePanel();
            swipeLayout.setEnabled(true);
        }
    }

    public void hideOfflinePanel() {
        ViewAnimationUtil.stopAnimation(offlinePanel);
        if(offlinePanel.getVisibility()==View.VISIBLE){
            offlinePanel.setVisibility(View.GONE);
        }
    }

    public void showOfflinePanel() {
        ViewAnimationUtil.showMessageBar(offlinePanel);
    }

    @Override
    public void onOffline() {
        offlineBar.setVisibility(View.VISIBLE);
        showOfflinePanel();
        //Disable swipe functionality and hide the loading view
        swipeLayout.setEnabled(false);
        invalidateSwipeFunctionality();
    }

    @Override
    public void onStop() {
        super.onStop();
        hideOfflinePanel();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(fetchFriendsObserver);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter fetchFriendsFilter = new IntentFilter(FetchCourseFriendsService.NOTIFY_FILTER);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(fetchFriendsObserver, fetchFriendsFilter);
    }

    @Override
    public void onResume() {
        super.onResume();

        uiHelper.onResume();

        //Let the adapter know if it's connection status to facebook has changed.
        boolean socialConnected = new FacebookProvider().isLoggedIn() && pmFeatures.getBoolean(PrefManager.Key.ALLOW_SOCIAL_FEATURES, true);
        adapter.setShowSocial(socialConnected);

    }

    /**
     * Adds a footer view to the list, which has "FIND A COURSE" button.
     * @param myCourseList - ListView
     */
    private void setupFooter(ListView myCourseList) {
        try {
            View footer = LayoutInflater.from(getActivity()).inflate(tw.openedu.www.R.layout.panel_find_course, null);
            myCourseList.addFooterView(footer, null, false);
            Button course_btn = (Button) footer.findViewById(tw.openedu.www.R.id.course_btn);
            course_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        environment.getSegment().trackUserFindsCourses();
                    } catch (Exception e) {
                        logger.error(e);
                    }

                    try {
                        if (environment.getConfig().getEnrollmentConfig().isEnabled()) {
                            //Call the Find courses activity
                            environment.getRouter().showFindCourses(getActivity());
                        } else {
                            //Show the dialog only if the activity is started. This is to avoid Illegal state
                            //exceptions if the dialog fragment tries to show even if the application is not in foreground
                            if (isAdded() && isVisible()) {
                                FindCoursesDialogFragment findCoursesFragment = new FindCoursesDialogFragment();
                                findCoursesFragment.setStyle(DialogFragment.STYLE_NORMAL,
                                        android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                                findCoursesFragment.setCancelable(false);
                                findCoursesFragment.show(getFragmentManager(), "dialog-find-courses");
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });

            ETextView courseNotListedTv = (ETextView) footer.findViewById(tw.openedu.www.R.id.course_not_listed_tv);
            courseNotListedTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCourseNotListedDialog();
                }
            });
        }catch(Exception e){
            logger.error(e);
        }
    }

    public void showCourseNotListedDialog() {
        ((BaseFragmentActivity)getActivity()).showWebDialog(getString(tw.openedu.www.R.string.course_not_listed_file_name), false,
                null);
    }

    private class FetchFriendsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String courseId = intent.getStringExtra(FetchCourseFriendsService.EXTRA_BROADCAST_COURSE_ID);

            AsyncTaskResult<List<SocialMember>> result = FetchCourseFriendsService.fetchResult(courseId);

            if (result !=null && result.getResult() != null) {
                int listPos = adapter.getPositionForCourseId(courseId);
                if(listPos < 0)
                    return;
                adapter.getItem(listPos).getCourse().setMembers_list(result.getResult());
                adapter.notifyDataSetChanged();

            }

        }
    }

    @Override
    public void fetchCourseFriends(EnrolledCoursesResponse course) {

        boolean loggedInSocial = new FacebookProvider().isLoggedIn();

        if (!loggedInSocial){
            return;
        }

        Intent fetchFriends = new Intent(getActivity(), FetchCourseFriendsService.class);

        fetchFriends.putExtra(FetchCourseFriendsService.TAG_COURSE_ID, course.getCourse().getId());
        fetchFriends.putExtra(FetchCourseFriendsService.TAG_COURSE_OAUTH, FacebookSessionUtil.getAccessToken());

        getActivity().startService(fetchFriends);
    }

}
