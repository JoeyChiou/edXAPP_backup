package tw.openedu.www.view.common;

import tw.openedu.www.model.course.CourseComponent;

/**
 * Created by hanning on 6/9/15.
 */
public interface RunnableCourseComponent extends Runnable{
    CourseComponent getCourseComponent();
}
