package com.kniost.draganddraw;

import android.graphics.PointF;

/**
 * Created by kniost on 2017/3/2.
 */

public class Box {
    private PointF mOrigin;
    private PointF mCurrent;
    private float mOriginAngle;
    private float mRotatedAngle;

    public Box(PointF origin) {
        mOrigin = origin;
        mCurrent = origin;
        mOriginAngle = 0;
        mRotatedAngle = 0;
    }

    public PointF getOrigin() {
        return mOrigin;
    }

    public PointF getCurrent() {
        return mCurrent;
    }

    public void setCurrent(PointF current) {
        mCurrent = current;
    }

    public float getOriginAngle() {
        return mOriginAngle;
    }

    public void setOriginAngle(float originAngle) {
        mOriginAngle = originAngle;
    }

    public float getRotatedAngle() {
        return mRotatedAngle;
    }

    public void setRotatedAngle(float rotatedAngle) {
        mRotatedAngle = rotatedAngle;
    }

    public PointF getCenter() {
        return new PointF((mCurrent.x + mOrigin.x) / 2, (mCurrent.y + mOrigin.y) / 2);
    }
}
