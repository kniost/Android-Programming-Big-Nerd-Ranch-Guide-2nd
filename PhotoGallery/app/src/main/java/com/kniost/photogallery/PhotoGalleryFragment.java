package com.kniost.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
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
    private FetchItemsTask mFetchItemsTask;
    private int mNextPage = 1, mLastPosition;

    private final int MAX_PAGES = 10;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mFetchItemsTask = new FetchItemsTask();
        mFetchItemsTask.execute(1);
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
                        int columns = mPhotoRecyclerView.getWidth() / 350;
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

    private void setAdapter() {
        if (isAdded()) {
            if (mPhotoAdapter == null) {
                mPhotoAdapter = new PhotoAdapter(mItems);
                mPhotoRecyclerView.setAdapter(mPhotoAdapter);
                mPhotoRecyclerView.addOnScrollListener(onButtomListener);
            } else {
                mPhotoAdapter.addData(mItems);
            }
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTextView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mTitleTextView = (TextView) itemView;
        }

        public void bindGalleryItem(GalleryItem item) {
            mTitleTextView.setText(item.toString());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            holder.bindGalleryItem(galleryItem);
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
        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
            return new FlickrFetchr().fetchItems(params[0]);
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
                        mFetchItemsTask = new FetchItemsTask();
                        mFetchItemsTask.execute(mNextPage);
                    } else {
                        Toast.makeText(getActivity(), "This is the end!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };
}
