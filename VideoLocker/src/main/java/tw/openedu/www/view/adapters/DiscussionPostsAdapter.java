package tw.openedu.www.view.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.TextViewCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;

import tw.openedu.www.core.IEdxEnvironment;
import tw.openedu.www.discussion.DiscussionThread;
import tw.openedu.www.third_party.iconify.IconDrawable;
import tw.openedu.www.third_party.iconify.IconView;
import tw.openedu.www.third_party.iconify.Iconify;

public class DiscussionPostsAdapter extends BaseListAdapter<DiscussionThread> {

    @Inject
    public DiscussionPostsAdapter(Context context, IEdxEnvironment environment) {
        super(context, tw.openedu.www.R.layout.row_discussion_thread, environment);
    }

    @Override
    public void render(BaseViewHolder tag, DiscussionThread discussionThread) {
        ViewHolder holder = (ViewHolder) tag;

        Iconify.IconValue iconValue = Iconify.IconValue.fa_comments;
        if (discussionThread.getType() == DiscussionThread.ThreadType.QUESTION) {
            iconValue = discussionThread.isHasEndorsed() ?
                    Iconify.IconValue.fa_check_square_o : Iconify.IconValue.fa_question;
        }
        holder.discussionPostTypeIcon.setIcon(iconValue);

        String threadTitle = discussionThread.getTitle();
        holder.discussionPostTitle.setText(threadTitle);

        if (discussionThread.getAuthorLabel() != null) {
            holder.discussionPostPinTextView.setVisibility(View.VISIBLE);
            final String pinFollowTextLabel;
            switch (discussionThread.getAuthorLabel()) {
                case STAFF: {
                    pinFollowTextLabel = getContext().getString(tw.openedu.www.R.string.discussion_priviledged_author_label_staff);
                    break;
                }
                case COMMUNITY_TA: {
                    pinFollowTextLabel = getContext().getString(tw.openedu.www.R.string.discussion_priviledged_author_label_ta);
                    break;
                }
                default: {
                    pinFollowTextLabel = "";
                }
            }
            final Drawable icon;
            if (discussionThread.isPinned()) {
                icon = new IconDrawable(getContext(), Iconify.IconValue.fa_thumb_tack).colorRes(tw.openedu.www.R.color.edx_grayscale_neutral_base).sizeRes(tw.openedu.www.R.dimen.edx_x_small);
            } else {
                icon = null;
            }
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.discussionPostPinTextView, icon, null, null, null);
            holder.discussionPostPinTextView.setText(" " + pinFollowTextLabel);

        } else {
            holder.discussionPostPinTextView.setVisibility(View.GONE);
        }

        if (discussionThread.isFollowing()) {
            holder.discussionPostFollowTextView.setVisibility(View.VISIBLE);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.discussionPostFollowTextView,
                    new IconDrawable(getContext(), Iconify.IconValue.fa_star).colorRes(tw.openedu.www.R.color.edx_grayscale_neutral_base).sizeRes(tw.openedu.www.R.dimen.edx_x_small), null, null, null);
        } else {
            holder.discussionPostFollowTextView.setVisibility(View.GONE);
        }

        final int commentColor = discussionThread.getUnreadCommentCount() == 0 ?
                tw.openedu.www.R.color.edx_grayscale_neutral_light : tw.openedu.www.R.color.edx_brand_primary_base;
        holder.discussionPostNumCommentsTextView.setText(Integer.toString(discussionThread.getCommentCount()));
        holder.discussionPostNumCommentsTextView.setTextColor(getContext().getResources().getColor(commentColor));
        holder.discussionPostCommentIcon.setIconColor(getContext().getResources().getColor(commentColor));
        holder.discussionPostRow.setBackgroundColor(getContext().getResources().getColor(
                discussionThread.isRead() ? tw.openedu.www.R.color.edx_grayscale_neutral_xx_light : tw.openedu.www.R.color.edx_grayscale_neutral_white));
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        final ViewHolder holder = new ViewHolder();

        holder.discussionPostRow = (RelativeLayout) convertView.findViewById(tw.openedu.www.R.id.row_discussion_post_relative_layout);
        holder.discussionPostTypeIcon = (IconView) convertView.findViewById(tw.openedu.www.R.id.discussion_post_type_icon);
        holder.discussionPostTitle = (TextView) convertView.findViewById(tw.openedu.www.R.id.discussion_post_title);
        holder.discussionPostPinTextView = (TextView) convertView.findViewById(tw.openedu.www.R.id.discussion_post_pin_text_view);
        holder.discussionPostFollowTextView = (TextView) convertView.findViewById(tw.openedu.www.R.id.discussion_post_following_text_view);
        holder.discussionPostNumCommentsTextView = (TextView) convertView.findViewById(tw.openedu.www.R.id.discussion_post_num_comments_text_view);
        holder.discussionPostCommentIcon = (IconView) convertView.findViewById(tw.openedu.www.R.id.discussion_post_comment_icon);

        return holder;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    private static class ViewHolder extends BaseViewHolder {
        RelativeLayout discussionPostRow;
        IconView discussionPostTypeIcon;
        TextView discussionPostTitle;
        TextView discussionPostPinTextView;
        TextView discussionPostFollowTextView;
        TextView discussionPostNumCommentsTextView;
        IconView discussionPostCommentIcon;

    }
}
