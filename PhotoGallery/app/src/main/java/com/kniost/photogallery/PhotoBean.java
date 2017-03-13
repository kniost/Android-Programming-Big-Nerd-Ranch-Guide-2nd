package com.kniost.photogallery;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by kniost on 17/2/10.
 */

public class PhotoBean {

    @SerializedName("photos")
    private PhotosInfo mPhotoInfo;

    public class PhotosInfo {
        @SerializedName("photo")
        List<GalleryItem> mPhoto;

        public List<GalleryItem> getPhoto() {
            return mPhoto;
        }

        public void setPhoto(List<GalleryItem> photo) {
            mPhoto = photo;
        }
    }

    public PhotosInfo getPhotoInfo() {
        return mPhotoInfo;
    }

    public void setPhotoInfo(PhotosInfo photoInfo) {
        mPhotoInfo = photoInfo;
    }
}
