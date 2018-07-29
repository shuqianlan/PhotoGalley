package com.test.compl.galley;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.LruCache;

import com.test.compl.MyApplication;
import com.test.compl.net.FlickrFetchr;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import libcore.io.DiskLruCache;

public class ImageLoader {

    private Context mContext;
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;
    private ImageResizer mImageResizer;
    private final int DISK_CACHE_INDEX = 0;
    private final int DISK_CACHE_SIZE = 1024 * 1024 * 50;

    private boolean mIsDiskLruCacheCreated = false;
    public static ImageLoader sImageLoader;

    public static ImageLoader getInstance() {
        return SingloneInstance.INSTANCE.getInstance();
    }

    public ImageLoader() {
      mContext = MyApplication.getContext();
      mImageResizer = new ImageResizer();

      int maxMemory = (int)(Runtime.getRuntime().maxMemory())/1024;
      int cacheSize = maxMemory/8;

      mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
          @Override
          protected int sizeOf(String key, Bitmap value) {
              return value.getRowBytes() * value.getHeight() / 1024;
          }
      };

      File diskCacheDir = getDiskCacheDir(mContext, "bitmap");
      if (!diskCacheDir.exists()) {
          diskCacheDir.mkdirs();
      }

      if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
          try {
              mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
              mIsDiskLruCacheCreated = true;
          } catch (IOException ex) {
              ex.printStackTrace();
          }
      }

    }

    public File getDiskCacheDir(Context context, String uniqueName) {
        boolean externalAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;

        if (externalAvailable) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }

        return new File(cachePath + File.separator + uniqueName);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private long getUsableSpace(File path) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
//        }

//        final StatFs ststs = new StatFs(path.getPath());
//        return (long) ststs.getBlockCountLong() * (long) ststs.getAvailableBlocksLong();
    }

    private void addBitmapTpMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    private String hashKeyForUrl(String url) {
        String cacheKey = null;

        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = byteToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException ex) {
            cacheKey = String.valueOf(url.hashCode());
            ex.printStackTrace();
        }

        return cacheKey;
    }

    private String byteToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02x", 0xff & bytes[i]));
        }

        return sb.toString();
    }

    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not visit network from UI Thread.");
        }

        if (mDiskLruCache == null) {
            return null;
        }

        String key = hashKeyForUrl(url);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);

        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if (new FlickrFetchr().downloadUrlToStream(url, outputStream)) {
                editor.commit();
            } else {
                editor.abort();
            }
            mDiskLruCache.flush();
        }

        return loadBitmapFromDiskCache(url, reqWidth, reqHeight);

    }

    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) throws IOException{
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not visit network from UI Thread.");
        }

        if (mDiskLruCache == null) {
            return null;
        }

        Bitmap bitmap = null;
        String key = hashKeyForUrl(url);
        DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);

        if (snapShot != null) {
            FileInputStream fileInputStream = (FileInputStream)snapShot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor descriptor = fileInputStream.getFD();
            bitmap = mImageResizer.decodeSampleBitmapFromFileDescriptor(descriptor, reqWidth, reqHeight);

            if (bitmap != null) {
                addBitmapTpMemoryCache(key, bitmap);
            }
        }

        return bitmap;
    }

    private Bitmap loadBitmapFromMemoryCache(String url) {
        String key = hashKeyForUrl(url);
        return getBitmapFromMemoryCache(key);
    }

    private Bitmap loadBitmapFromUrl(String url) {
        Bitmap bitmap = null;
        try {
            byte[] bytes = new FlickrFetchr().getUrlBytes(url);
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bitmap;
    }

    public Bitmap loadBitmap(String uri, int reqWidth, int reqHeight) {
        Bitmap bitmap = loadBitmapFromMemoryCache(uri);

        if (bitmap != null) {
            return bitmap;
        }

        try {
            bitmap = loadBitmapFromDiskCache(uri, reqWidth, reqHeight);

            if (bitmap != null) {
                return bitmap;
            }

            bitmap = loadBitmapFromHttp(uri, reqWidth, reqHeight);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (bitmap != null && !mIsDiskLruCacheCreated) {
            bitmap = loadBitmapFromUrl(uri);
        }

        return bitmap;
    }

    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight, boolean inMainUI) throws IOException{
        if (mDiskLruCache == null) {
            return null;
        }

        Bitmap bitmap = null;
        String key = hashKeyForUrl(url);
        DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);

        if (snapShot != null) {
            FileInputStream fileInputStream = (FileInputStream)snapShot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor descriptor = fileInputStream.getFD();
            bitmap = mImageResizer.decodeSampleBitmapFromFileDescriptor(descriptor, reqWidth, reqHeight);

            if (bitmap != null) {
                addBitmapTpMemoryCache(key, bitmap);
            }
        }

        return bitmap;
    }

    public Bitmap loadBitmap(String uri, int reqWidth, int reqHeight, boolean inMainUI) {
        Bitmap bitmap = loadBitmapFromMemoryCache(uri);

        if (bitmap != null) {
            return bitmap;
        }

        try {
            bitmap = loadBitmapFromDiskCache(uri, reqWidth, reqHeight, inMainUI);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return bitmap;
    }

    public void clearCache() throws IOException {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }

        if (mDiskLruCache != null) {
            mDiskLruCache.delete();
        }
    }

    private enum SingloneInstance {
        INSTANCE;

        SingloneInstance() {
            sImageLoader = new ImageLoader();
        }

        public static ImageLoader getInstance() {
            return sImageLoader;
        }
    }
}
