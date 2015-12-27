package tw.openedu.www.http;

import android.os.Bundle;

import tw.openedu.www.interfaces.SectionItemInterface;
import tw.openedu.www.model.api.AnnouncementsModel;
import tw.openedu.www.model.api.AuthResponse;
import tw.openedu.www.model.api.CourseInfoModel;
import tw.openedu.www.model.api.EnrolledCoursesResponse;
import tw.openedu.www.model.api.HandoutModel;
import tw.openedu.www.model.api.LectureModel;
import tw.openedu.www.model.api.ProfileModel;
import tw.openedu.www.model.api.RegisterResponse;
import tw.openedu.www.model.api.ResetPasswordResponse;
import tw.openedu.www.model.api.SectionEntry;
import tw.openedu.www.model.api.SyncLastAccessedSubsectionResponse;
import tw.openedu.www.model.api.VideoResponseModel;
import tw.openedu.www.module.registration.model.RegistrationDescription;
import tw.openedu.www.social.SocialFactory;
import tw.openedu.www.social.SocialMember;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO - we won't need this api when we fully migrate the code to okhttp
 */
public interface IApi {
    ResetPasswordResponse resetPassword(String emailId)
            throws Exception;

    AuthResponse auth(String username, String password)
                    throws Exception;

    ProfileModel getProfile(String username) throws Exception;

    ProfileModel getProfile() throws Exception;

    List<EnrolledCoursesResponse> getEnrolledCourses()
                                                            throws Exception;

    EnrolledCoursesResponse getCourseById(String courseId);

    List<EnrolledCoursesResponse> getEnrolledCourses(boolean fetchFromCache) throws Exception;

    HandoutModel getHandout(String url, boolean fetchFromCache) throws Exception;

    CourseInfoModel getCourseInfo(String url, boolean preferCache) throws Exception;

    List<AnnouncementsModel> getAnnouncement(String url, boolean preferCache)
            throws Exception;


    String downloadTranscript(String url)
                    throws Exception;

    List<EnrolledCoursesResponse> getFriendsCourses(String oauthToken) throws Exception;

    List<EnrolledCoursesResponse> getFriendsCourses(boolean preferCache, String oauthToken) throws Exception;

    List<SocialMember> getFriendsInCourse(String courseId, String oauthToken) throws Exception;

    List<SocialMember> getFriendsInCourse(boolean preferCache, String courseId, String oauthToken) throws Exception;

    boolean inviteFriendsToGroup(long[] toInvite, long groupId, String oauthToken) throws Exception;

    long createGroup(String name, String description, boolean privacy, long adminId, String socialToken) throws Exception;

    boolean setUserCourseShareConsent(boolean consent) throws Exception;

    boolean getUserCourseShareConsent() throws Exception;

    List<SocialMember> getGroupMembers(boolean preferCache, long groupId) throws Exception;


    AuthResponse socialLogin(String accessToken, SocialFactory.SOCIAL_SOURCE_TYPE socialType)
                    throws Exception;

    AuthResponse loginByFacebook(String accessToken) throws Exception;

    AuthResponse loginByGoogle(String accessToken) throws Exception;

    SyncLastAccessedSubsectionResponse syncLastAccessedSubsection(String courseId,
                                                                  String lastVisitedModuleId) throws Exception;

    SyncLastAccessedSubsectionResponse getLastAccessedSubsection(String courseId) throws Exception;

    RegisterResponse register(Bundle parameters)
                                    throws Exception;

    RegistrationDescription getRegistrationDescription() throws Exception;

    Boolean enrollInACourse(String courseId, boolean email_opt_in) throws Exception;

    List<HttpCookie> getSessionExchangeCookie() throws Exception;

    @Deprecated
    String getUnitUrlByVideoById(String courseId, String videoId);
    @Deprecated
    VideoResponseModel getSubsectionById(String courseId, String subsectionId)
        throws Exception;
    @Deprecated
    VideoResponseModel getVideoById(String courseId, String videoId)
        throws Exception;
    @Deprecated
    LectureModel getLecture(String courseId, String chapterName, String lectureName)
        throws Exception;
    @Deprecated
    Map<String, SectionEntry> getCourseHierarchy(String courseId, boolean preferCache)
        throws Exception;
    @Deprecated
    Map<String, SectionEntry> getCourseHierarchy(String courseId)
        throws Exception;
    @Deprecated
    ArrayList<SectionItemInterface> getLiveOrganizedVideosByChapter
        (String courseId, String chapter);

    public HttpManager.HttpResult getCourseStructure(HttpRequestDelegate delegate) throws Exception;
}
