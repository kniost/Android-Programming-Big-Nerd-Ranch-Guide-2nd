package com.kniost.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kniost on 2017/3/2.
 */

public class BoxDrawingView extends View {

    private static final String
            TAG = "BoxDrawingView",
            KEY_SUPER_DATA = "key_super_data",
            KEY_BOXEN = "key_boxen";

    private Box mCurrentBox;
    private List<Box> mBoxen = new ArrayList<>();
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    // Used when creating the view in code
    public BoxDrawingView(Context context) {
        this(context, null);
    }

    // Used when inflating the view from xml
    public BoxDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Paint the boxes a nice semitransparent red (ARGB)
        mBoxPaint = new Paint();
        mBoxPaint.setColor(0x22ff0000);

        // Paint the background off-white
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fill the background
        canvas.drawPaint(mBackgroundPaint);

        for (Box box : mBoxen) {
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            canvas.save();
            canvas.rotate(box.getRotatedAngle(), box.getCenter().x, box.getCenter().y);
            canvas.drawRect(left, top, right, bottom, mBoxPaint);
            canvas.restore();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());
        String action = "";

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                // Reset drawing state
                mCurrentBox = new Box(current);
                mBoxen.add(mCurrentBox);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                action = "POINTER_DOWN";
                if (event.getPointerCount() == 2) {
                    float angle = (float) (Math.atan((event.getY(1) - event.getY(0)) /
                        (event.getX(1) - event.getX(0))) * 180 / Math.PI);
                    mCurrentBox.setOriginAngle(angle);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                if (mCurrentBox != null) {
                    if (event.getPointerCount() == 1 && mCurrentBox.getRotatedAngle() == 0) {
                        mCurrentBox.setCurrent(current);
                    }
                    if (event.getPointerCount() == 2) {
                        float angle = (float) (Math.atan((event.getY(1) - event.getY(0)) /
                                (event.getX(1) - event.getX(0))) * 180 / Math.PI);
                        Log.i(TAG, "onTouchEvent: angle:" + (angle - mCurrentBox.getOriginAngle()));
                        mCurrentBox.setRotatedAngle(mCurrentBox.getRotatedAngle() + angle
                                - mCurrentBox.getOriginAngle());
                        mCurrentBox.setOriginAngle(angle);
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                mCurrentBox = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                mCurrentBox = null;
                break;
        }
//
//        Log.i(TAG, action + " at x=" + current.x +
//                ", y=" + current.y);

        return true;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        Parcelable superData = super.onSaveInstanceState();
        bundle.putParcelable(KEY_SUPER_DATA, superData);
        bundle.putSerializable(KEY_BOXEN, (ArrayList) mBoxen);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        Parcelable superData = bundle.getParcelable(KEY_SUPER_DATA);
        mBoxen = (List<Box>) bundle.getSerializable(KEY_BOXEN);
        super.onRestoreInstanceState(superData);
        invalidate();
    }
}
