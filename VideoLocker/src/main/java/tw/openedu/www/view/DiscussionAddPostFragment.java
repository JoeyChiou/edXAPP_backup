package tw.openedu.www.view;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import tw.openedu.www.R;
import tw.openedu.www.discussion.CourseTopics;
import tw.openedu.www.discussion.DiscussionThread;
import tw.openedu.www.discussion.DiscussionThreadPostedEvent;
import tw.openedu.www.discussion.DiscussionTopic;
import tw.openedu.www.discussion.DiscussionTopicDepth;
import tw.openedu.www.discussion.ThreadBody;
import tw.openedu.www.logger.Logger;
import tw.openedu.www.model.api.EnrolledCoursesResponse;
import tw.openedu.www.module.analytics.ISegment;
import tw.openedu.www.task.CreateThreadTask;
import tw.openedu.www.task.GetTopicListTask;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class DiscussionAddPostFragment extends RoboFragment {

    static public String TAG = DiscussionAddPostFragment.class.getCanonicalName();
    static public String ENROLLMENT = TAG + ".enrollment";
    static public String TOPIC = TAG + ".topic";

    protected final Logger logger = new Logger(getClass().getName());

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    private EnrolledCoursesResponse courseData;

    @InjectExtra(Router.EXTRA_DISCUSSION_TOPIC)
    private DiscussionTopic discussionTopic;

    @InjectView(R.id.discussion_question_segmented_group)
    private RadioGroup discussionQuestionSegmentedGroup;

    @InjectView(R.id.topics_spinner)
    private Spinner topicsSpinner;

    @InjectView(R.id.title_edit_text)
    private EditText titleEditText;

    @InjectView(R.id.body_edit_text)
    private EditText bodyEditText;

    @InjectView(R.id.add_post_button)
    private Button addPostButton;

    @Inject
    ISegment segIO;


    private ViewGroup container;

    private CourseTopics allCourseTopics;
    private List<DiscussionTopicDepth> allTopicsWithDepth;
    private int selectedTopicIndex;
    private GetTopicListTask getTopicListTask;
    private CreateThreadTask createThreadTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            segIO.screenViewsTracking(courseData.getCourse().getName() +
                    " - AddPost");
        } catch (Exception e) {
            logger.error(e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        this.container = container;
        return inflater.inflate(R.layout.fragment_add_post, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionQuestionSegmentedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                @StringRes final int bodyHint;
                @StringRes final int submitLabel;
                if (discussionQuestionSegmentedGroup.getCheckedRadioButtonId() == R.id.discussion_radio_button) {
                    bodyHint = R.string.discussion_body_hint_discussion;
                    submitLabel = R.string.discussion_add_post;
                } else {
                    bodyHint = R.string.discussion_body_hint_question;
                    submitLabel = R.string.discussion_add_question;
                }
                bodyEditText.setHint(bodyHint);
                addPostButton.setText(submitLabel);
            }
        });
        discussionQuestionSegmentedGroup.check(R.id.discussion_radio_button);

        getTopicList();

        ViewCompat.setBackgroundTintList(topicsSpinner, getResources().getColorStateList(R.color.edx_grayscale_neutral_dark));

        topicsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // if a top-level topic is selected, go back to previous selected position
                DiscussionTopicDepth topic = allTopicsWithDepth.get(position);
                if (topic.getDepth() == 0) {
                    topicsSpinner.setSelection(selectedTopicIndex);
                    Toast.makeText(container.getContext(), "Top level topic cannot be selected.", Toast.LENGTH_SHORT).show();
                } else
                    selectedTopicIndex = position;
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        addPostButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String title = titleEditText.getText().toString();
                final String body = bodyEditText.getText().toString();

                final DiscussionThread.ThreadType discussionQuestion;
                if (discussionQuestionSegmentedGroup.getCheckedRadioButtonId() == R.id.discussion_radio_button) {
                    discussionQuestion = DiscussionThread.ThreadType.DISCUSSION;
                } else {
                    discussionQuestion = DiscussionThread.ThreadType.QUESTION;
                }

                ThreadBody threadBody = new ThreadBody();
                threadBody.setCourseId(courseData.getCourse().getId());
                threadBody.setTitle(title);
                threadBody.setRawBody(body);
                threadBody.setTopicId(allTopicsWithDepth.get(selectedTopicIndex).getDiscussionTopic().getIdentifier());
                threadBody.setType(discussionQuestion);

                addPostButton.setEnabled(false);
                createThread(threadBody);
            }
        });
        addPostButton.setEnabled(false);
        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String title = titleEditText.getText().toString();
                final String body = bodyEditText.getText().toString();
                addPostButton.setEnabled(title.trim().length() > 0 && body.trim().length() > 0);
            }
        };
        titleEditText.addTextChangedListener(textWatcher);
        bodyEditText.addTextChangedListener(textWatcher);
    }

    protected void createThread(ThreadBody threadBody) {
        if (createThreadTask != null) {
            createThreadTask.cancel(true);
        }
        createThreadTask = new CreateThreadTask(getActivity(), threadBody) {
            @Override
            public void onSuccess(DiscussionThread courseTopics) {
                EventBus.getDefault().post(new DiscussionThreadPostedEvent(courseTopics));
                getActivity().finish();
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                //  hideProgress();
                addPostButton.setEnabled(true);
            }
        };
        createThreadTask.execute();
    }

    protected void getTopicList() {
        if (getTopicListTask != null) {
            getTopicListTask.cancel(true);
        }
        getTopicListTask = new GetTopicListTask(getActivity(), courseData.getCourse().getId()) {
            @Override
            public void onSuccess(CourseTopics courseTopics) {
                allCourseTopics = courseTopics;
                ArrayList<DiscussionTopic> allTopics = new ArrayList<>();
                allTopics.addAll(courseTopics.getCoursewareTopics());
                allTopics.addAll(courseTopics.getNonCoursewareTopics());

                allTopicsWithDepth = DiscussionTopicDepth.createFromDiscussionTopics(allTopics);
                ArrayList<String> topicList = new ArrayList<String>();
                int i = 0;
                for (DiscussionTopicDepth topic : allTopicsWithDepth) {
                    topicList.add((topic.getDepth() == 0 ? "" : "  ") + topic.getDiscussionTopic().getName());
                    if (discussionTopic.getName().equalsIgnoreCase(topic.getDiscussionTopic().getName()))
                        selectedTopicIndex = i;
                    i++;
                }

                String[] topics = new String[topicList.size()];
                topics = topicList.toArray(topics);

                final String prefix = getString(R.string.discussion_add_post_topic_label) + ": ";
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(container.getContext(), R.layout.edx_spinner_item, topics) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        final TextView view = (TextView) super.getView(position, convertView, parent);
                        view.setText(prefix + view.getText().toString());
                        return view;
                    }
                };
                adapter.setDropDownViewResource(R.layout.edx_spinner_dropdown_item);
                topicsSpinner.setAdapter(adapter);
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                //  hideProgress();
            }
        };
        getTopicListTask.execute();
    }

}
