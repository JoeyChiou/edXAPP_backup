package tw.openedu.www.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TabWidget;

import com.facebook.Session;
import com.facebook.SessionState;
import com.google.inject.Inject;

import tw.openedu.www.interfaces.NetworkObserver;
import tw.openedu.www.model.api.EnrolledCoursesResponse;
import tw.openedu.www.module.db.DataCallback;
import tw.openedu.www.module.facebook.IUiLifecycleHelper;
import tw.openedu.www.module.notification.NotificationDelegate;
import tw.openedu.www.module.prefs.PrefManager;
import tw.openedu.www.social.facebook.FacebookProvider;
import tw.openedu.www.util.AppConstants;

import java.util.ArrayList;
import java.util.List;

public class MyCoursesListActivity extends BaseTabActivity implements NetworkObserver {

    private IUiLifecycleHelper uiLifecycleHelper;
    private PrefManager featuresPref;

    @Inject
    NotificationDelegate notificationDelegate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(tw.openedu.www.R.layout.activity_course_list);

        featuresPref = new PrefManager(this, PrefManager.Pref.FEATURES);

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function

        configureDrawer();

        setTitle(getString(tw.openedu.www.R.string.label_my_courses));
        

        try{
            environment.getSegment().screenViewsTracking(getString(tw.openedu.www.R.string.label_my_courses));
        }catch(Exception e){
            logger.error(e);
        }

        Session.StatusCallback statusCallback = new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {

                changeSocialMode(state.isOpened());
            }

        };
        uiLifecycleHelper = IUiLifecycleHelper.Factory.getInstance(this, statusCallback);
        uiLifecycleHelper.onCreate(savedInstanceState);
    }

    private void changeSocialMode(boolean socialEnabled) {

        //Social enabled is always false if social features are disabled
        boolean allowSocialPref = featuresPref.getBoolean(PrefManager.Key.ALLOW_SOCIAL_FEATURES, true);
        if (!allowSocialPref) {
            socialEnabled = false;
        }

        if (tabHost != null) {
            TabWidget widget = tabHost.getTabWidget();
            widget.setVisibility(socialEnabled ? View.VISIBLE : View.GONE);

            if (!socialEnabled) {
                widget.setCurrentTab(0);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public List<TabModel> tabsToAdd() {
        List<TabModel> tabs = new ArrayList<TabModel>();
        tabs.add(new TabModel(getString(tw.openedu.www.R.string.label_my_courses),
                MyCourseListTabFragment.class,
                null, "my_course_tab_fragment"));
        tabs.add(new TabModel(getString(tw.openedu.www.R.string.label_my_friends_courses),
                MyFriendsCoursesTabFragment.class,
                null, "my_friends_course_fragment"));

        return tabs;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // GetEnrolledCoursesTask();
        setTitle(getString(tw.openedu.www.R.string.label_my_courses));
    }

    @Override
    public void onOffline() {
        AppConstants.offline_flag = true;
        invalidateOptionsMenu();
    }

    @Override
    public void onOnline() {
        AppConstants.offline_flag = false;
        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        uiLifecycleHelper.onResume();
        changeSocialMode(new FacebookProvider().isLoggedIn());
        notificationDelegate.checkAppUpgrade();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        overridePendingTransition(tw.openedu.www.R.anim.slide_in_from_start, tw.openedu.www.R.anim.slide_out_to_end);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiLifecycleHelper.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        uiLifecycleHelper.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiLifecycleHelper.onDestroy();
    }

    @Override
    protected int getDefaultTab() {
        return 0;
    }

    public void updateDatabaseAfterDownload(ArrayList<EnrolledCoursesResponse> list) {
        if (list != null && list.size() > 0) {
            //update all videos in the DB as Deactivated
            environment.getDatabase().updateAllVideosAsDeactivated(dataCallback);

            for (int i = 0; i < list.size(); i++) {
                //Check if the flag of isIs_active is marked to true,
                //then activate all videos
                if (list.get(i).isIs_active()) {
                    //update all videos for a course fetched in the API as Activated
                    environment.getDatabase().updateVideosActivatedForCourse(list.get(i).getCourse().getId(),
                            dataCallback);
                } else {
                    list.remove(i);
                }
            }

            //Delete all videos which are marked as Deactivated in the database
            environment.getStorage().deleteAllUnenrolledVideos();
        }
    }

    private DataCallback<Integer> dataCallback = new DataCallback<Integer>() {
        @Override
        public void onResult(Integer result) {
        }
        @Override
        public void onFail(Exception ex) {
            logger.error(ex);
        }
    };

    @Override
    protected void reloadMyCoursesData() {
        CourseListTabFragment fragment = (CourseListTabFragment) getFragmentByTag("my_course_tab_fragment");
        if (fragment != null) {
            fragment.loadData(false, true);
        }
    }
}
