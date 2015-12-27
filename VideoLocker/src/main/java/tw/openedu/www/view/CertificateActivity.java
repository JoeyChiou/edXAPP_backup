package tw.openedu.www.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import tw.openedu.www.base.BaseSingleFragmentActivity;
import tw.openedu.www.model.api.EnrolledCoursesResponse;

public class CertificateActivity extends BaseSingleFragmentActivity {

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(tw.openedu.www.R.string.tab_label_certificate));
    }

    @Override
    public Fragment getFirstFragment() {

        Fragment frag = new CertificateFragment();

        EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) getIntent().getSerializableExtra(CertificateFragment.ENROLLMENT);
        if (courseData != null) {

            Bundle bundle = new Bundle();
            bundle.putSerializable(CertificateFragment.ENROLLMENT, courseData);
            frag.setArguments(bundle);

        }

        return frag;
    }

}
