package com.utils.gdkcorp.albums.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.squareup.picasso.Picasso;
import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.GlideApp;
import com.utils.gdkcorp.albums.R;

/**
 * Created by Gautam Kakadiya on 02-08-2017.
 */

public class AllPhotosAdapter extends CursorRecyclerViewAdapter<AllPhotosAdapter.AllPhotosBitmapHolder> {

    private int width;
    private MediaStoreImageBitmapAdapter.ImageBitMapAdapterClickListener mAdapterClickListener;

    public  AllPhotosAdapter(Cursor cursor,MediaStoreImageBitmapAdapter.ImageBitMapAdapterClickListener mClickLsitener){
        super(cursor,MediaStore.Images.Media._ID);
        mAdapterClickListener = mClickLsitener;
        width = Constants.DISPLAY_WIDTH;
    }


    @Override
    public AllPhotosBitmapHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_photos_item,parent,false);
        AllPhotosBitmapHolder holder = new AllPhotosBitmapHolder(v);
        return holder;
    }


    @Override
    public void onBindViewHolder(AllPhotosBitmapHolder viewHolder, Cursor cursor, int position) {
        Log.i("ImageBitmapAdapter:","onBind");
        cursor.moveToPosition(position);
        int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        int foldercolunIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        Uri imageURI = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(cursor.getInt(columnIndex)));
//        Picasso
//                .with(context)
//                .load(imageURI)
//                .placeholder(ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_default_image,null))
//                .error(ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_default_image,null))
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

    @Override
    public void onViewRecycled(AllPhotosBitmapHolder holder) {
        super.onViewRecycled(holder);
        holder.cleanUp();
    }

    public class AllPhotosBitmapHolder extends RecyclerView.ViewHolder{

        private ImageView photoBitmapView;
        private View.OnClickListener mClickListener;
        public AllPhotosBitmapHolder(View itemView) {
            super(itemView);
            photoBitmapView = (ImageView) itemView.findViewById(R.id.bitmap_image_view1);
            setUpmClisckListener();
            photoBitmapView.setOnClickListener(mClickListener);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(((width-8)/4),((width-8)/4));
            params.setMargins(1,1,1,1);
            photoBitmapView.setLayoutParams(params);
        }

        private void setUpmClisckListener() {
            mClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAdapterClickListener.onClick(view,getLayoutPosition());
                }
            };
        }

        public void cleanUp(){
            photoBitmapView.setImageDrawable(null);
        }
    }

    public void cleanUp(){
        mAdapterClickListener=null;
    }

}
