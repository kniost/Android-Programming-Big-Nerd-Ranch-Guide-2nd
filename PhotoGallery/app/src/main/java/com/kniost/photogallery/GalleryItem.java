package com.kniost.photogallery;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kniost on 17/2/9.
 */

public class GalleryItem {
    @SerializedName("title")
    private String mCaption;
    @SerializedName("id")
    private String mId;
    @SerializedName("url_s")
    private String mUrl;
    @SerializedName("owner")
    private String mOwner;

    @Override
    public String toString() {
        return mCaption;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }

    public Uri getPhotoPageUri() {
        return Uri.parse("http://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }
}
