package com.utils.gdkcorp.albums.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.activities.ImageViewActivity;

import java.util.ArrayList;

import static com.utils.gdkcorp.albums.Constants.FOLDERS.CAMERA;
import static com.utils.gdkcorp.albums.Constants.FOLDERS.DOWNLOADS;
import static com.utils.gdkcorp.albums.Constants.FOLDERS.SCREENSHOTS;
import static com.utils.gdkcorp.albums.Constants.FOLDERS.SNAPSEED;
import static com.utils.gdkcorp.albums.Constants.FOLDERS.WHATSAPP;

/**
 * Created by Gautam Kakadiya on 24-07-2017.
 */

public class MediaStoreImageFolderAdapter extends CursorRecyclerViewAdapter<MediaStoreImageFolderAdapter.AlbumsHolder> {

    public final String BUCKET_DISPLAY_NAME_KEY_BUNDLE = "folder";
    private Parcelable[] mScrollState;
    public MediaStoreImageFolderAdapter(Cursor cursor,Context context,Parcelable[] offsets){
        super(cursor,MediaStore.Images.Media.BUCKET_ID);
        mScrollState = offsets;
    }

    @Override
    public AlbumsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.albums_rview_container_item,parent,false);
        AlbumsHolder holder = new AlbumsHolder(v);
        return holder;
    }


    @Override
    public void onBindViewHolder(AlbumsHolder viewHolder, Cursor cursor,int position) {
        int titleIndex  = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        String folder = cursor.getString(titleIndex);
        viewHolder.albumTitle.setText(folder.toUpperCase());
        switch (folder){
            case WHATSAPP : viewHolder.albumIcon.setImageResource(R.drawable.ic_whatsapp_icon);
                break;
            case CAMERA : viewHolder.albumIcon.setImageResource(R.drawable.ic_camera_black_36dp);
                break;
            case SCREENSHOTS : viewHolder.albumIcon.setImageResource(R.drawable.ic_fullscreen_black_36dp);
                break;
            case SNAPSEED : viewHolder.albumIcon.setImageResource(R.drawable.ic_snap_seed);
                break;
            case DOWNLOADS : viewHolder.albumIcon.setImageResource(R.drawable.ic_file_download_black_36dp);
                break;
            default: viewHolder.albumIcon.setImageResource(R.drawable.ic_image_black_36dp);
                break;
        }
        viewHolder.initLoader(folder,position);
    }

    @Override
    public void onViewRecycled(AlbumsHolder holder) {
        super.onViewRecycled(holder);
        holder.cleanUp();
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        if(newCursor==null){
            mScrollState=null;
        }else{
            if( mScrollState==null) {
                mScrollState = new Parcelable[newCursor.getCount()];
            }else{
                if(mScrollState.length!=newCursor.getCount()){
                    mScrollState = new Parcelable[newCursor.getCount()];
                }
            }
        }
        return super.swapCursor(newCursor);
    }

    public class AlbumsHolder extends RecyclerView.ViewHolder implements LoaderManager.LoaderCallbacks<Cursor>{

        private TextView albumTitle;
        private ImageView albumIcon;
        private RecyclerView mPhotoRecyclerView;
        private MediaStoreImageBitmapAdapter mPhotoBitmapAdapter;
        private GridLayoutManager gridLayoutManager;
        private ProgressBar mProgressBar;
        private MediaStoreImageBitmapAdapter.ImageBitMapAdapterClickListener mClickListener;
        private int position=-1,lastPosition=-1;

        public AlbumsHolder(View itemView) {
            super(itemView);
            albumTitle = (TextView) itemView.findViewById(R.id.album_title);
            albumIcon = (ImageView) itemView.findViewById(R.id.album_icon);
            mPhotoRecyclerView = (RecyclerView) itemView.findViewById(R.id.album_photos_rview);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progess_bar_1);
            gridLayoutManager = new GridLayoutManager(itemView.getContext(),2, LinearLayoutManager.HORIZONTAL,false);
            mPhotoRecyclerView.setLayoutManager(gridLayoutManager);
            setUpmClickListener();
            mPhotoBitmapAdapter = new MediaStoreImageBitmapAdapter(null,mClickListener);
            mPhotoRecyclerView.setAdapter(mPhotoBitmapAdapter);
        }

        private void setUpmClickListener() {
            mClickListener = new MediaStoreImageBitmapAdapter.ImageBitMapAdapterClickListener() {
                @Override
                public void onClick(View view, int position) {
                    Intent intent = new Intent(albumTitle.getContext(),ImageViewActivity.class);
                    intent.setAction(Constants.ACTION.OFFLINE_IMAGE_ACTION);
                    intent.putExtra(ImageViewActivity.BUNDLE_FOLDER_NAME_EXTRA_KEY,albumTitle.getText());
                    intent.putExtra(ImageViewActivity.BUNDLE_IMAGE_POSITION_EXTRA_KEY,position);
                    intent.putExtra(ImageViewActivity.BUNDLE_FOLDER_POSITION_EXTRA_KEY,getLayoutPosition());
                    view.getContext().startActivity(intent);
                }
            };
        }

        public void initLoader(String folder,int position){
            Log.i("ImageFolderAdapter","initLoader");
//            Loader<Cursor> loader = ((FragmentActivity)itemView.getContext()).getSupportLoaderManager().getLoader(position);
//            if(loader==null) {
//                Log.i("ImageFolderAdapter","initLoader null");
                Bundle bundle = new Bundle();
                bundle.putString(BUCKET_DISPLAY_NAME_KEY_BUNDLE,folder);
                lastPosition = this.position;
                this.position = position;
                ((FragmentActivity)itemView.getContext()).getSupportLoaderManager().initLoader(position, bundle, this);
//            }else{
//                Log.i("ImageFolderAdapter","initLoader starting");
//                if(loader.isStarted()){
//                    Log.i("ImageFolderAdapter","initLoader started");
//                    loader.forceLoad();
//                }
//            }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String BUCKET_DISPLAY_NAME = args.getString(BUCKET_DISPLAY_NAME_KEY_BUNDLE);
            String[] PROJECTION = { MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.ORIENTATION,
                    MediaStore.Images.Media.DATE_TAKEN
            };
            String SELECTION = MediaStore.Images.Media.DATA + " like ? OR "+MediaStore.Images.Media.DATA + " like ?";
            String[] SELECTION_ARGS = {"%"+BUCKET_DISPLAY_NAME+"/%.jp%g","%"+BUCKET_DISPLAY_NAME+"/%.png"};
            String SORT_ORDER = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            return new CursorLoader(itemView.getContext(),uri,PROJECTION,SELECTION,SELECTION_ARGS,SORT_ORDER);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.i("ImageFolderAdapter","data "+data.getCount());
            if(getLayoutPosition()==loader.getId()) {
                if(mPhotoBitmapAdapter!=null) {
                    mPhotoBitmapAdapter.swapCursor(data);
                }
                //gridLayoutManager.scrollToPositionWithOffset(0,mScrollOffsetList[getLayoutPosition()]);
                if(mScrollState[getLayoutPosition()]==null){
                    Log.i("ImageFolderAdapter","state null");
                }
                gridLayoutManager.onRestoreInstanceState(mScrollState[getLayoutPosition()]);
                mProgressBar.setVisibility(View.INVISIBLE);
                mPhotoRecyclerView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            Log.i("ImageFolderAdapter","Loader reset called");
            if(getLayoutPosition()==loader.getId()) {
                mPhotoBitmapAdapter.swapCursor(null);
                mPhotoRecyclerView.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        public void cleanUp() {
            Log.i("ImageFolderAdapter","State Save");
            mScrollState[getLayoutPosition()] = gridLayoutManager.onSaveInstanceState();
            mPhotoBitmapAdapter.swapCursor(null);
            albumIcon.setImageDrawable(null);
            mPhotoRecyclerView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public Parcelable[] getScrollOffsetList(){
        return mScrollState;
    }

}
