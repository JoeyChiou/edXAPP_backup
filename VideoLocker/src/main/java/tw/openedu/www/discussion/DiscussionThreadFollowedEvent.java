package tw.openedu.www.discussion;

import android.support.annotation.NonNull;

public class DiscussionThreadFollowedEvent {

    @NonNull
    private final DiscussionThread discussionThread;

    public DiscussionThreadFollowedEvent(@NonNull DiscussionThread discussionThread) {
        this.discussionThread = discussionThread;
    }

    @NonNull
    public DiscussionThread getDiscussionThread() {
        return discussionThread;
    }
}
