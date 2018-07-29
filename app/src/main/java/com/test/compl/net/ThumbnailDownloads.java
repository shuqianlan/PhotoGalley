package com.test.compl.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.test.compl.galley.ImageLoader;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ThumbnailDownloads<T> extends HandlerThread {

    public static final String TAG = "ThumbnailDownloads";
    private static final int MESSAGE_DOWNLOAD = 0;
    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ImageLoader mImageLoader;
    private ThumbnailDownLoadListener<T> mThumbnailDownLoadListener;
    private ConcurrentHashMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    public ThumbnailDownloads(String name) {

        super(name);
    }

    public ThumbnailDownloads(Handler handler) {
        this(TAG);
        mImageLoader = ImageLoader.getInstance();
        mResponseHandler = handler;
    }

    public interface ThumbnailDownLoadListener<T> {
        void onThumbnailDown(T target, Bitmap bitmap);
    }

    public void setThumbnailDownLoadListener(ThumbnailDownLoadListener<T> thumbnailDownLoadListener) {
        mThumbnailDownLoadListener = thumbnailDownLoadListener;
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    // 当前线程的Handler. sendTarget后，Looper然后dispatchMessage到此handleMessage，随后发生请求，请求成功后发生通过mResponseHandler在UI线程中更新UI.
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    int arg1 = msg.arg1;
                    int arg2 = msg.arg2;

                    Log.d(TAG, "handleMessage: arg1 " + arg1);
                    Log.d(TAG, "handleMessage: arg2 " + arg2);
                    if (arg1 != 0 && arg2 != 0) {
                        handleRequest(target, arg1, arg2);
                    } else {
                        handleRequest(target);
                    }
                }
            }
        };
    }

    public void queueThumbnail(T target, String url) {
        if (TextUtils.isEmpty(url)) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    public void queueThumbnail(T target, String url, int reqWidth, int reqHeight) {
        if (TextUtils.isEmpty(url)) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, reqWidth, reqHeight, target).sendToTarget();
        }
    }

    private void handleRequest(final T target) {
        final String url = mRequestMap.get(target);
        if (TextUtils.isEmpty(url)) {
            return;
        }

        try {
            byte[] bytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url || mHasQuit) {
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownLoadListener.onThumbnailDown(target, bitmap);
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleRequest(final T target, final int reqWidth, final int reqHeight) {
        final String url = mRequestMap.get(target);
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Log.d(TAG, "handleRequest: url " + url);
        Log.d(TAG, "handleRequest: reqWidth " + reqWidth);
        Log.d(TAG, "handleRequest: reqHeight " + reqHeight);
        final Bitmap bitmap = mImageLoader.loadBitmap(url, reqWidth, reqHeight);
        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mRequestMap.get(target) != url || mHasQuit) {
                    return;
                }

                mRequestMap.remove(target);
                mThumbnailDownLoadListener.onThumbnailDown(target, bitmap);
            }
        });
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }


}
