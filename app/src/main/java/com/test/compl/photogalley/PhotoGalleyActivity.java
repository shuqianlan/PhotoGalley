package com.test.compl.photogalley;

import android.support.v4.app.Fragment;

import com.test.compl.BaseActivity;
import com.test.compl.fragment.PhotoGalleyFragment;

public class PhotoGalleyActivity extends BaseActivity {

    @Override
    protected Fragment getFragmentInstance() {
        return PhotoGalleyFragment.newInstance();
    }

}
