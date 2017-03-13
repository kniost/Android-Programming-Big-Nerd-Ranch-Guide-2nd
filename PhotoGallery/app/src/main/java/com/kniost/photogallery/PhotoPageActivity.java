package com.kniost.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

/**
 * Created by kniost on 17/3/1.
 */

public class PhotoPageActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context, Uri photoPageUri) {
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        return PhotoPageFragment.newInstance(getIntent().getData());
    }

    @Override
    public void onBackPressed() {
        PhotoPageFragment fragment = (PhotoPageFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (fragment.getWebView().canGoBack()) {
            fragment.getWebView().goBack();
        } else {
            super.onBackPressed();
        }
    }
}
