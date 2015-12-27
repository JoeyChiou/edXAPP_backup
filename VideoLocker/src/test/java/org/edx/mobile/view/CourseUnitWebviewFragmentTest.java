package org.openedu.www.view;

import android.view.View;
import android.webkit.WebView;

import tw.openedu.www.R;
import tw.openedu.www.http.OkHttpUtil;
import tw.openedu.www.model.api.EnrolledCoursesResponse;
import tw.openedu.www.model.course.BlockType;
import tw.openedu.www.model.course.CourseComponent;
import tw.openedu.www.model.course.HtmlBlockModel;
import tw.openedu.www.view.CourseUnitWebviewFragment;

import org.junit.Test;
import org.robolectric.util.SupportFragmentTestUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

// There is currently a Robolectric issue with initializing EdxWebView:
// https://github.com/robolectric/robolectric/issues/793
// We should add mock web server and test the handling later
public class CourseUnitWebviewFragmentTest extends UiTest {
    /**
     * Method for iterating through the mock course response data, and
     * returning the first video block leaf.
     *
     * @return The first {@link HtmlBlockModel} leaf in the mock course data
     */
    private HtmlBlockModel getHtmlUnit() {
        EnrolledCoursesResponse courseData;
        CourseComponent courseComponent;
        try {
            courseData = api.getEnrolledCourses().get(0);
            courseComponent = serviceManager.getCourseStructure(
                    courseData.getCourse().getId(),
                    OkHttpUtil.REQUEST_CACHE_TYPE.IGNORE_CACHE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<CourseComponent> htmlBlockUnits = new ArrayList<>();
        courseComponent.fetchAllLeafComponents(htmlBlockUnits,
                EnumSet.of(BlockType.HTML));
        return (HtmlBlockModel) htmlBlockUnits.get(0);
    }

    /**
     * Testing initialization
     */
    @Test
    public void initializeTest() {
        CourseUnitWebviewFragment fragment = CourseUnitWebviewFragment.newInstance(getHtmlUnit());
        SupportFragmentTestUtil.startVisibleFragment(fragment);
        View view = fragment.getView();
        assertNotNull(view);

        View courseUnitWebView = view.findViewById(R.id.course_unit_webView);
        assertNotNull(courseUnitWebView);
        assertThat(courseUnitWebView).isInstanceOf(WebView.class);
        WebView webView = (WebView) courseUnitWebView;
        assertTrue(webView.getSettings().getJavaScriptEnabled());
    }
}
