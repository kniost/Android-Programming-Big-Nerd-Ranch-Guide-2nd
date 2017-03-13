package com.kniost.criminalintent;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ImageView;


/**
 * Created by kniost on 17/1/11.
 */

public class GlancePictureFragment extends DialogFragment {

    private static final String ARG_PATH = "path";

    private ImageView mImage;

    public static GlancePictureFragment newInstance(String path) {
        Bundle args = new Bundle();
        args.putString(ARG_PATH, path);
        GlancePictureFragment fragment = new GlancePictureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String path = getArguments().getString(ARG_PATH);

        final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_image_glance);

        mImage = (ImageView) dialog.findViewById(R.id.glance_image);
        mImage.setImageBitmap(
                PictureUtils.getScaledBitmap(path, getActivity()));
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }
}
