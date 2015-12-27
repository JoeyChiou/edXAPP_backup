package tw.openedu.www.view.view_holders;

import android.view.View;

import tw.openedu.www.R;
import tw.openedu.www.discussion.DiscussionTextUtils;
import tw.openedu.www.discussion.IAuthorData;
import tw.openedu.www.view.custom.ETextView;

public class AuthorLayoutViewHolder {

    private final ETextView discussionAuthorTextView;

    public AuthorLayoutViewHolder(View itemView) {
        discussionAuthorTextView = (ETextView) itemView.
                findViewById(R.id.discussion_author_layout_author_text_view);
    }

    public void setAuthorData(IAuthorData authorData) {
        discussionAuthorTextView.setText(DiscussionTextUtils.getAuthorAttributionText(authorData, discussionAuthorTextView.getResources()));
    }
}
