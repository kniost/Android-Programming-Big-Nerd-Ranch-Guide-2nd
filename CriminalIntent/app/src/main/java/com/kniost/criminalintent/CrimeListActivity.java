package com.kniost.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by kniost on 2016/11/7.
 */

public class CrimeListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }
}
