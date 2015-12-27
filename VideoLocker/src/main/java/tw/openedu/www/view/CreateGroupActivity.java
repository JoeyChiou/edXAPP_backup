package tw.openedu.www.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import tw.openedu.www.base.BaseSingleFragmentActivity;

public class CreateGroupActivity extends BaseSingleFragmentActivity {

    public static String TAG = CreateGroupActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(tw.openedu.www.R.anim.slide_in_bottom, tw.openedu.www.R.anim.stay_put);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(tw.openedu.www.R.string.label_new_group));
    }

    @Override
    public Fragment getFirstFragment() {
        return new CreateGroupFragment();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(tw.openedu.www.R.anim.stay_put, tw.openedu.www.R.anim.slide_out_bottom);
    }

}
