package com.test.compl.photogalley;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;

import com.test.compl.BaseActivity;
import com.test.compl.fragment.PhotoPageFragment;

public class PhotoPageActivity extends BaseActivity implements View.OnTouchListener{

    @Override
    protected Fragment getFragmentInstance() {
        return PhotoPageFragment.newInstance(getIntent().getData());
    }

    private PhotoPageFragment getCurrentFragment() {
        return (PhotoPageFragment) getSupportFragmentManager().findFragmentById(R.id.framelayout);
    }

    public static Intent newIntent(Context context, Uri photoPageUri) {
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.BUTTON_BACK) {
            getCurrentFragment().onDealWithEvent(event);
            return true;
        }
        return false;
    }
}
