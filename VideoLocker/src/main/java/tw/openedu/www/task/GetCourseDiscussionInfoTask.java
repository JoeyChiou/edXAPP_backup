package tw.openedu.www.task;

import android.content.Context;

import tw.openedu.www.discussion.CourseDiscussionInfo;

public abstract class GetCourseDiscussionInfoTask extends
Task<CourseDiscussionInfo> {

    String courseId;
    boolean preferCache;

    public GetCourseDiscussionInfoTask(Context context, String courseId, boolean preferCache) {
        super(context);
        this.courseId = courseId;
        this.preferCache = preferCache;
    }



    public CourseDiscussionInfo call( ) throws Exception{
        try {

            if(courseId!=null){

                return environment.getDiscussionAPI().getCourseDiscussionInfo(courseId, preferCache);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
