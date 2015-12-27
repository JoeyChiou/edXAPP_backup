package tw.openedu.www.view.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import tw.openedu.www.R;
import tw.openedu.www.view.custom.ETextView;

public class NumberResponsesViewHolder extends RecyclerView.ViewHolder {
    public ETextView numberResponsesOrCommentsLabel;

    public NumberResponsesViewHolder(View itemView) {
        super(itemView);

        numberResponsesOrCommentsLabel = (ETextView) itemView.
                findViewById(R.id.number_responses_or_comments_label);
    }

}
