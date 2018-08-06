package com.test.compl.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.compl.galley.GalleyItem;
import com.test.compl.galley.ImageLoader;
import com.test.compl.net.FlickrFetchr;
import com.test.compl.net.ThumbnailDownloads;
import com.test.compl.photogalley.PhotoGalleyActivity;
import com.test.compl.photogalley.PhotoPageActivity;
import com.test.compl.photogalley.R;
import com.test.compl.service.PollService;

import java.io.IOException;
import java.util.List;

public class PhotoGalleyFragment extends VisibleFragment implements ThumbnailDownloads.ThumbnailDownLoadListener<PhotoGalleyFragment.PhotoHolder> {

    public static final String TAG = "Photo_Galley";
    private ThumbnailDownloads<PhotoHolder> mHolderThumbnailDownloads;
    private RecyclerView mRecyclerView;
    public static PhotoGalleyFragment newInstance() {
        return new PhotoGalleyFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        Handler handler = new Handler();
        mHolderThumbnailDownloads = new ThumbnailDownloads<PhotoHolder>(handler);
        mHolderThumbnailDownloads.start();
        mHolderThumbnailDownloads.getLooper();
        mHolderThumbnailDownloads.setThumbnailDownLoadListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_galley, container, false);
        mRecyclerView = v.findViewById(R.id.fragment_galley_recyclerview);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        setAdapter();

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHolderThumbnailDownloads.quit();
    }

    public class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitle;
        private ImageView mDrawable;
        private GalleyItem galley;

        public PhotoHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.photo_title);
            mDrawable = itemView.findViewById(R.id.photo_src);
            this.itemView.setOnClickListener(this);
        }

        public void bindData(GalleyItem item) {
            mTitle.setText(item.getTitle());
            galley = item;
        }

        public void bindDrawable(Drawable drawable) {
            mDrawable.setImageDrawable(drawable);
        }

        @Override
        public void onClick(View v) {
            Intent i = PhotoPageActivity.newIntent(getContext(), galley.getPhotoUri());
            startActivity(i);
        }
    }

    private class GalleyAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleyItem> mLists;
        public GalleyAdapter() {

        }

        public void setLists(List<GalleyItem> lists) {
            mLists = lists;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.galley_photo_item, parent, false);

            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleyItem item = mLists.get(position);
            holder.bindData(item);

            mHolderThumbnailDownloads.queueThumbnail(holder, item.getUrl(), holder.mDrawable.getDrawable().getIntrinsicWidth(), holder.mDrawable.getDrawable().getIntrinsicHeight()); // 通过HandlerThread实现获取.
//            Picasso.get().load(Uri.parse(item.getUrl()))
//                    .into(holder.mDrawable);
        }

        @Override
        public int getItemCount() {
            if (mLists == null) {
                return 0;
            }
            return mLists.size();
        }

        @Override
        public void onViewRecycled(@NonNull PhotoHolder holder) {
            super.onViewRecycled(holder);
        }
    }

    private class GalleyAsync extends AsyncTask<String, Void, List<GalleyItem>> {
        @Override
        protected List<GalleyItem> doInBackground(String... voids) {
            Log.d(TAG, "doInBackground: voids");
            if (voids[0].equals("recent")) {
                return new FlickrFetchr().fetchRecentItems();
            } else {
                return new FlickrFetchr().fetchSearchItems(voids[0]);
            }
        }

        @Override
        protected void onPostExecute(List<GalleyItem> galleyItems) {
            super.onPostExecute(galleyItems);

            ((GalleyAdapter)mRecyclerView.getAdapter()).setLists(galleyItems);
        }
    }

    private class GalleyAsyncLoader extends AsyncTaskLoader<List<GalleyItem>> implements Loader.OnLoadCompleteListener {

        public GalleyAsyncLoader(@NonNull Context context) {
            super(context);
        }

        @Nullable
        @Override
        public List<GalleyItem> loadInBackground() {
            String state = "test";
//            if (state != null) {
//                return new FlickrFetchr().fetchSearchItems();
//            }
            return new FlickrFetchr().fetchRecentItems();
        }

        @Override
        public void onCanceled(@Nullable List<GalleyItem> data) {
            super.onCanceled(data);
        }

        @Override
        public void onLoadComplete(@NonNull Loader loader, @Nullable Object data) {
            List<GalleyItem> galleyItems = (List<GalleyItem>)data;
            ((GalleyAdapter)mRecyclerView.getAdapter()).setLists(galleyItems);
        }
    }

    private void setAdapter() {
        if (isAdded()) {
            GalleyAdapter adapter = new GalleyAdapter();
            mRecyclerView.setAdapter(adapter);
            new GalleyAsync().execute("recent");
        }
    }

    @Override
    public void onThumbnailDown(PhotoHolder target, Bitmap bitmap) {
        target.bindDrawable(new BitmapDrawable(bitmap));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mHolderThumbnailDownloads.clearQueue();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                refreshItems(query);
                searchView.onActionViewCollapsed();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        MenuItem menuItem = menu.findItem(R.id.action_runService);
        if (PollService.isServiceAlarmOn(getContext())) {
            menuItem.setTitle("Running");
        } else {
            menuItem.setTitle("Idle");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_delete:
                try {
                    new ImageLoader().clearCache();
                    setAdapter();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return true;
            case R.id.action_runService:
                boolean isRunning = PollService.isServiceAlarmOn(getContext());
                Log.d(TAG, "onOptionsItemSelected: isRunning " + isRunning);
                PollService.setServiceAlarm(getContext(), !isRunning);
                getActivity().invalidateOptionsMenu(); // 声明过时，稍后会重建.
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshItems(String query) {
        new GalleyAsync().execute(query);
    }

}
