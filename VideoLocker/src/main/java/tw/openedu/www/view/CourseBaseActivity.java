package tw.openedu.www.view;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;

import tw.openedu.www.R;
import tw.openedu.www.base.BaseFragmentActivity;
import tw.openedu.www.event.DownloadEvent;
import tw.openedu.www.model.api.EnrolledCoursesResponse;
import tw.openedu.www.model.course.CourseComponent;
import tw.openedu.www.module.prefs.PrefManager;
import tw.openedu.www.services.CourseManager;
import tw.openedu.www.third_party.iconify.IconDrawable;
import tw.openedu.www.third_party.iconify.IconView;
import tw.openedu.www.third_party.iconify.Iconify;
import tw.openedu.www.util.AppConstants;
import tw.openedu.www.util.BrowserUtil;
import tw.openedu.www.util.NetworkUtil;
import tw.openedu.www.view.common.MessageType;
import tw.openedu.www.view.common.TaskProcessCallback;
import tw.openedu.www.view.custom.popup.menu.PopupMenu;

import de.greenrobot.event.EventBus;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 *  A base class to handle some common task
 *  NOTE- in the layout file,  these should be defined
 *  1. offlineBar
 *  2. progress_spinner
 *  3. offline_mode_message
 */
@ContentView(R.layout.activity_course_base)
public abstract  class CourseBaseActivity  extends BaseFragmentActivity implements TaskProcessCallback {

    @InjectView(R.id.offline_bar)
    View offlineBar;

    @InjectView(R.id.last_access_bar)
    View lastAccessBar;

    @InjectView(R.id.download_in_progress_bar)
    View downloadProgressBar;

    @InjectView(R.id.video_download_indicator)
    IconView downloadIndicator;

    @InjectView(R.id.progress_spinner)
    ProgressBar progressWheel;

    @Inject
    CourseManager courseManager;

    protected EnrolledCoursesResponse courseData;
    protected String courseComponentId;

    protected abstract String getUrlForWebView();

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Bundle bundle = arg0;
        if ( bundle == null ) {
            if ( getIntent() != null )
                bundle = getIntent().getBundleExtra(Router.EXTRA_BUNDLE);
        }
        restore(bundle);

        initialize(arg0);
        blockDrawerFromOpening();
    }

    protected void initialize(Bundle arg){

        setApplyPrevTransitionOnRestart(true);
        ((IconView)findViewById(tw.openedu.www.R.id.video_download_indicator)).setIconColor(getResources().getColor(tw.openedu.www.R.color.edx_brand_primary_light));
        findViewById(tw.openedu.www.R.id.download_in_progress_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getRouter().showDownloads(CourseBaseActivity.this);
            }
        });


        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
            invalidateOptionsMenu();
            showOfflineMessage();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        if ( !EventBus.getDefault().isRegistered(this) )
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ( EventBus.getDefault().isRegistered(this) )
            EventBus.getDefault().unregister(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if ( courseData != null)
            outState.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
        if ( courseComponentId != null )
            outState.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
        super.onSaveInstanceState(outState);
    }

    protected void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            courseData = (EnrolledCoursesResponse) savedInstanceState.getSerializable(Router.EXTRA_ENROLLMENT);
            courseComponentId = savedInstanceState.getString(Router.EXTRA_COURSE_COMPONENT_ID);
        }
    }

    /**
     * callback for EventBus
     * https://github.com/greenrobot/EventBus
     */
    public void onEvent(DownloadEvent event) {
        setVisibilityForDownloadProgressView(true);
    }

    @Override
    protected void onOnline() {
        offlineBar.setVisibility(View.GONE);
        hideOfflineMessage();
    }

    @Override
    protected void onOffline() {
        offlineBar.setVisibility(View.VISIBLE);

        hideLoadingProgress();
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected boolean createOptionMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(tw.openedu.www.R.menu.course_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if( menu.findItem(tw.openedu.www.R.id.action_share_on_web) != null)
            menu.findItem(tw.openedu.www.R.id.action_share_on_web).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_share_square_o)
                    .actionBarSize().colorRes(tw.openedu.www.R.color.edx_white));
        PrefManager.UserPrefManager userPrefManager = new PrefManager.UserPrefManager(this);

        if (  menu.findItem(tw.openedu.www.R.id.action_change_mode) != null ) {
            if (userPrefManager.isUserPrefVideoModel()) {
                menu.findItem(tw.openedu.www.R.id.action_change_mode).setIcon(
                    new IconDrawable(this, Iconify.IconValue.fa_film)
                        .actionBarSize().colorRes(tw.openedu.www.R.color.edx_white));
            } else {
                menu.findItem(tw.openedu.www.R.id.action_change_mode).setIcon(
                    new IconDrawable(this, Iconify.IconValue.fa_list)
                        .actionBarSize().colorRes(tw.openedu.www.R.color.edx_white));
            }
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case tw.openedu.www.R.id.action_share_on_web:
                shareOnWeb();
                return true;
            case tw.openedu.www.R.id.action_change_mode:
                changeMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void changeMode(){
        //Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(this,
            findViewById(tw.openedu.www.R.id.action_change_mode), Gravity.END);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
            .inflate(tw.openedu.www.R.menu.change_mode, popup.getMenu());
        final PrefManager.UserPrefManager userPrefManager =
            new PrefManager.UserPrefManager(this);
        final MenuItem videoOnlyItem = popup.getMenu().findItem(tw.openedu.www.R.id.change_mode_video_only);
        MenuItem fullCourseItem = popup.getMenu().findItem(tw.openedu.www.R.id.change_mode_full_mode);
        // Initializing the font awesome icons
        IconDrawable videoOnlyIcon = new IconDrawable(this, Iconify.IconValue.fa_film);
        IconDrawable fullCourseIcon = new IconDrawable(this, Iconify.IconValue.fa_list);
        videoOnlyItem.setIcon(videoOnlyIcon);
        fullCourseItem.setIcon(fullCourseIcon);
        // Setting checked states
        if (userPrefManager.isUserPrefVideoModel()) {
            videoOnlyItem.setChecked(true);
            videoOnlyIcon.colorRes(tw.openedu.www.R.color.cyan_4);
            fullCourseIcon.colorRes(tw.openedu.www.R.color.black);
        } else {
            fullCourseItem.setChecked(true);
            fullCourseIcon.colorRes(tw.openedu.www.R.color.cyan_4);
            videoOnlyIcon.colorRes(tw.openedu.www.R.color.black);
        }

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                PrefManager.UserPrefManager userPrefManager =
                    new PrefManager.UserPrefManager(CourseBaseActivity.this);
                boolean currentVideoMode = userPrefManager.isUserPrefVideoModel();
                boolean selectedVideoMode = videoOnlyItem == item;
                if ( currentVideoMode == selectedVideoMode )
                    return true;

                userPrefManager.setUserPrefVideoModel(selectedVideoMode);
                modeChanged();
                invalidateOptionsMenu();

                environment.getSegment().trackCourseOutlineMode(selectedVideoMode);
                return true;
            }
        });

        popup.show(); //showing popup menu

    }

    protected void modeChanged(){};


    public void shareOnWeb() {
        //Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(this, findViewById(tw.openedu.www.R.id.action_share_on_web),
                Gravity.END, tw.openedu.www.R.attr.edgePopupMenuStyle, tw.openedu.www.R.style.CustomEdgePopupMenu);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(tw.openedu.www.R.menu.share_on_web, popup.getMenu());


        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                new BrowserUtil().open(CourseBaseActivity.this, getUrlForWebView());
                CourseComponent courseComponent = courseManager.getComponentById(courseData.getCourse().getId(), courseComponentId);
                environment.getSegment().trackOpenInBrowser(courseComponentId
                        , courseData.getCourse().getId(), courseComponent.isMultiDevice());
                return true;
            }
        });

        popup.show(); //showing popup menu
    }

    /**
     * This function shows the offline mode message
     */
    private void showOfflineMessage(){
        if(offlineBar!=null){
            offlineBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function hides the offline mode message
     */
    private void hideOfflineMessage() {
        if(offlineBar!=null){
            offlineBar.setVisibility(View.GONE);
        }
    }

    /**
     * This function shows the loading progress wheel
     * Show progress wheel while loading the web page
     */
    private void showLoadingProgress(){
        if(progressWheel!=null){
            progressWheel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function hides the loading progress wheel
     * Hide progress wheel after the web page completes loading
     */
    private void hideLoadingProgress(){
        if(progressWheel!=null){
            progressWheel.setVisibility(View.GONE);
        }
    }


    protected void setVisibilityForDownloadProgressView(boolean show){
        if ( downloadProgressBar != null )
            downloadProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    protected void hideLastAccessedView(View v) {
        try{
            lastAccessBar.setVisibility(View.GONE);
        }catch(Exception e){
            logger.error(e);
        }
    }

    protected void showLastAccessedView(View v, String title, View.OnClickListener listener) {
        lastAccessBar.setVisibility(View.VISIBLE);
        View lastAccessTextView = v == null ? findViewById(tw.openedu.www.R.id.last_access_text) :
            v.findViewById(tw.openedu.www.R.id.last_access_text);
        ((TextView)lastAccessTextView).setText(title);
        View detailButton = v == null ? findViewById(tw.openedu.www.R.id.last_access_button) :
            v.findViewById(tw.openedu.www.R.id.last_access_button);
        detailButton.setOnClickListener(listener);
    }


    /**
     * Call this function if you do not want to allow
     * opening/showing the drawer(Navigation Fragment) on swiping left to right
     */
    protected void blockDrawerFromOpening(){
        DrawerLayout drawerLayout = (DrawerLayout)
            findViewById(tw.openedu.www.R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    /**
     * implements TaskProcessCallback
     */
    public void startProcess(){
        showLoadingProgress();
    }
    /**
     * implements TaskProcessCallback
     */
    public void finishProcess(){
        hideLoadingProgress();
    }

    public void onMessage(MessageType messageType, String message){
        showErrorMessage("", message);
    }
}

