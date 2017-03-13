package com.kniost.photogallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kniost on 17/2/7.
 */

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private ViewTreeObserver mObserver;
    private PhotoAdapter mPhotoAdapter;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDowloader<PhotoHolder> mThumbnailDownloader;
    private FetchItemsTask mFetchItemsTask;
    private int mNextPage = 1, mLastPosition;

    private final int MAX_PAGES = 3;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems(1);

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDowloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloaderListener(
                new ThumbnailDowloader.ThumbnailDowloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        target.bindDrawable(drawable);
                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) v
                .findViewById(R.id.fragment_photo_gallery_recycler_view);
//        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        mPhotoRecyclerView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int columns = mPhotoRecyclerView.getWidth() / 240;
                        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columns));
                        mPhotoRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        if (!(mFetchItemsTask.getStatus() == AsyncTask.Status.FINISHED)) {
                            return;
                        }
                        mPhotoRecyclerView.setAdapter(mPhotoAdapter);
                        mPhotoRecyclerView.addOnScrollListener(onButtomListener);
                        mPhotoRecyclerView.getLayoutManager().scrollToPosition(mLastPosition);
                    }
                });
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);
                QueryPreferences.setStoredQuery(getActivity(), query);
                mNextPage = 1;
                updateItems(mNextPage);
                if (searchView != null) {
                    // 得到输入管理对象
                    InputMethodManager imm = (InputMethodManager) getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        // 这将让键盘在所有的情况下都被隐藏，但是一般我们在点击搜索按钮后，输入法都会乖乖的自动隐藏的。
                        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0); // 输入法如果是显示状态，那么就隐藏输入法
                    }
                    searchView.onActionViewCollapsed(); // 不获取焦点
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                mThumbnailDownloader.clearQueue();
                mNextPage = 1;
                updateItems(mNextPage);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems(int page) {
        String query = QueryPreferences.getStoredQuery(getActivity());
        mFetchItemsTask = new FetchItemsTask(query);
        mFetchItemsTask.execute(page);
    }

    private void setAdapter() {
        if (isAdded()) {
            if (mNextPage == 1) {
                mPhotoAdapter = new PhotoAdapter(mItems);
                mPhotoRecyclerView.setAdapter(mPhotoAdapter);
                mPhotoRecyclerView.addOnScrollListener(onButtomListener);
            } else {
                mPhotoAdapter.addData(mItems);
            }
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView
                    .findViewById(R.id.fragment_photo_gallery_image_view);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflator = LayoutInflater.from(getActivity());
            View view = inflator.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            Drawable placeholder = getResources()
                    .getDrawable(R.drawable.android_robot);
            holder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl());
            for (int i = Math.max(0, position - 10);
                    i < Math.min(mGalleryItems.size() - 1, position + 10);
                    i ++ ) {
//                Log.i(TAG, "Preload position" + i);
                mThumbnailDownloader.queuePreloadThumbnail(mGalleryItems.get(i).getUrl());
            }
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }

        public void addData(List<GalleryItem> newItems) {
            mGalleryItems.addAll(newItems);
            notifyDataSetChanged();
        }
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, List<GalleryItem>> {
        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {

            if (mQuery == null) {
                return new FlickrFetchr().fetchRecentPhotos(params[0]);
            } else {
                return new FlickrFetchr().searchPhotos(mQuery, params[0]);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems = galleryItems;
            setAdapter();
        }
    }

    private RecyclerView.OnScrollListener onButtomListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            mLastPosition = layoutManager.findLastCompletelyVisibleItemPosition();
            if (mPhotoAdapter == null) {
                return;
            }
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && mLastPosition >= mPhotoAdapter.getItemCount() - 1) {
                if (mFetchItemsTask.getStatus() == AsyncTask.Status.FINISHED) {
                    mNextPage++;
                    if (mNextPage <= MAX_PAGES) {
                        Toast.makeText(getActivity(), "waiting to load ……", Toast.LENGTH_SHORT).show();
                        updateItems(mNextPage);
                    } else {
                        Toast.makeText(getActivity(), "This is the end!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };
}
