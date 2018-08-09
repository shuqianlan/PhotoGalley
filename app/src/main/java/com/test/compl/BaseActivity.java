package com.test.compl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.test.compl.photogalley.PhotoGalleyActivity;
import com.test.compl.photogalley.R;

import okhttp3.internal.cache.DiskLruCache;

public abstract class BaseActivity extends AppCompatActivity {

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        mFragmentManager = getSupportFragmentManager();
        Fragment fragment = mFragmentManager.findFragmentById(R.id.framelayout);
        if (fragment == null) {
            fragment = getFragmentInstance();
            if (fragment != null) {
                mFragmentManager.beginTransaction().add(R.id.framelayout, fragment).commit();
            }
        }
    }

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }
    protected abstract Fragment getFragmentInstance();

    public static Intent newIntent(Context context) {
        return new Intent(context, PhotoGalleyActivity.class);
    }

}
