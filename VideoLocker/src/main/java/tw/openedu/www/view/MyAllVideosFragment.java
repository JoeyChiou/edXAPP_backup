package tw.openedu.www.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import tw.openedu.www.base.MyVideosBaseFragment;
import tw.openedu.www.logger.Logger;
import tw.openedu.www.model.api.EnrolledCoursesResponse;
import tw.openedu.www.util.AppConstants;
import tw.openedu.www.view.adapters.MyAllVideoCourseAdapter;

import java.util.ArrayList;

public class MyAllVideosFragment extends MyVideosBaseFragment {
    private MyAllVideoCourseAdapter myCoursesAdaptor;
    protected final Logger logger = new Logger(getClass().getName());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            environment.getSegment().screenViewsTracking("My Videos - All Videos");
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void reloadList() {
        addMyAllVideosData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view       =   inflater.inflate(tw.openedu.www.R.layout.fragment_my_all_videos, container, false);

        ListView myCourseList = (ListView) view.findViewById(tw.openedu.www.R.id.my_video_course_list);
        myCourseList.setEmptyView(view.findViewById(tw.openedu.www.R.id.empty_list_view));

        myCoursesAdaptor = new MyAllVideoCourseAdapter(getActivity(), environment) {
            @Override
            public void onItemClicked(EnrolledCoursesResponse model) {
                AppConstants.myVideosDeleteMode = false;
                
                Intent videoIntent = new Intent(getActivity(), VideoListActivity.class);
                videoIntent.putExtra(Router.EXTRA_ENROLLMENT, model);
                videoIntent.putExtra("FromMyVideos", true);
                //videoIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(videoIntent);
            }
        };

        addMyAllVideosData();
        myCourseList.setAdapter(myCoursesAdaptor);
        myCourseList.setOnItemClickListener(myCoursesAdaptor);
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        addMyAllVideosData();
        myCoursesAdaptor.notifyDataSetChanged();
    }
    
    @Override
    public void onStop() {
        super.onStop();
    }
    
    private void addMyAllVideosData(){
        try{
            if(myCoursesAdaptor!=null){
                myCoursesAdaptor.clear();
                ArrayList<EnrolledCoursesResponse> coursesList = environment.getStorage()
                        .getDownloadedCoursesWithVideoCountAndSize();
                for (EnrolledCoursesResponse m : coursesList) {
                    if(m.isIs_active()){
                        myCoursesAdaptor.add(m);
                    }
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
    }
}