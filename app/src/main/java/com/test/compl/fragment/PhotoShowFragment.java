package com.test.compl.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.test.compl.galley.ImageLoader;
import com.test.compl.photogalley.R;

public class PhotoShowFragment extends DialogFragment {

    public static PhotoShowFragment newInstance(String url) {
        PhotoShowFragment fragment = new PhotoShowFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", url);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_show_photo, null);

        if (getArguments() != null) {
            String url = getArguments().getString("URL");
            ImageView image = view.findViewById(R.id.show_photo);
            int width = getContext().getResources().getDisplayMetrics().widthPixels;
            int height = getContext().getResources().getDisplayMetrics().heightPixels;
            Bitmap bm = ImageLoader.getInstance().loadBitmap(url, width, height, true);
            image.setImageDrawable(new BitmapDrawable(bm));
        }

        return new AlertDialog.Builder(getContext())
                .setView(view)
                .setCancelable(true)
                .show();
    }
}
