package com.utils.gdkcorp.albums.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.squareup.picasso.Picasso;
import com.utils.gdkcorp.albums.GlideApp;
import com.utils.gdkcorp.albums.R;

/**
 * Created by Gautam Kakadiya on 24-07-2017.
 */

public class MediaStoreImageBitmapAdapter extends CursorRecyclerViewAdapter<MediaStoreImageBitmapAdapter.BitmapHolder> {

    private ImageBitMapAdapterClickListener mAdapterClickListener;
    public MediaStoreImageBitmapAdapter(Cursor cursor,ImageBitMapAdapterClickListener mClickLsitener){
        super(cursor,MediaStore.Images.Media._ID);
        mAdapterClickListener = mClickLsitener;
    }

    @Override
    public BitmapHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_item,parent,false);
        BitmapHolder holder = new BitmapHolder(v);
        return holder;
    }


    @Override
    public void onViewRecycled(BitmapHolder holder) {
        super.onViewRecycled(holder);
        holder.photoBitmapView.setImageDrawable(null);
    }


    @Override
    public void onBindViewHolder(BitmapHolder viewHolder, Cursor cursor, int position) {
        //Log.i("ImageBitmapAdapter:","onBind");
        cursor.moveToPosition(position);
        int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        int foldercolunIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        Uri imageURI = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(cursor.getInt(columnIndex)));
//        Picasso
//                .with(viewHolder.itemView.getContext().getApplicationContext())
//                .load(imageURI)
//                .placeholder(ResourcesCompat.getDrawable(viewHolder.itemView.getContext().getResources(),R.drawable.ic_default_image,null))
//                .error(ResourcesCompat.getDrawable(viewHolder.itemView.getContext().getResources(),R.drawable.ic_default_image,null))
//                .fit()
//                .centerCrop()
//                .into(viewHolder.photoBitmapView);

        GlideApp
                .with(viewHolder.itemView.getContext().getApplicationContext())
                .load(imageURI)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.ic_default_image)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(viewHolder.photoBitmapView);
    }

    public class BitmapHolder extends RecyclerView.ViewHolder{

        private ImageView photoBitmapView;
        private View.OnClickListener mClicklistener;

        public BitmapHolder(View itemView) {
            super(itemView);
            photoBitmapView = (ImageView) itemView.findViewById(R.id.bitmap_image_view);
            setUpmClisckListener();
            photoBitmapView.setOnClickListener(mClicklistener);
        }

        private void setUpmClisckListener() {
            mClicklistener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAdapterClickListener.onClick(view,getLayoutPosition());
                }
            };
        }

    }



//    private Bitmap getBitmapFromCursor(int position){
//        int idIndex = mMediaStoreImageBitmapCursor.getColumnIndex(MediaStore.Images.Media._ID);
//        mMediaStoreImageBitmapCursor.moveToPosition(position);
//        return MediaStore.Images.Thumbnails.getThumbnail(
//                context.getContentResolver(),
//                mMediaStoreImageBitmapCursor.getLong(idIndex),
//                MediaStore.Images.Thumbnails.MICRO_KIND,
//                null);
//    }

    public interface ImageBitMapAdapterClickListener {
        public void onClick(View view,int position);
    }

    public void cleanUp(){
        mAdapterClickListener=null;
    }

}
