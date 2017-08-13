package com.utils.gdkcorp.albums.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.utils.gdkcorp.albums.R;

/**
 * Created by Gautam Kakadiya on 24-07-2017.
 */

public class MediaStoreImageBitmapAdapter extends RecyclerView.Adapter<MediaStoreImageBitmapAdapter.BitmapHolder> {

    private Cursor mMediaStoreImageBitmapCursor;
    private Context context;
    private ImageBitMapAdapterClickListener mAdapterClickListener;
    public MediaStoreImageBitmapAdapter(Cursor cursor, Context context,ImageBitMapAdapterClickListener mClickLsitener){
        mMediaStoreImageBitmapCursor = cursor;
        this.context = context;
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
    public void onBindViewHolder(BitmapHolder holder, int position) {
//        holder.photoBitmapView.setImageBitmap(getBitmapFromCursor(position));
        Log.i("ImageBitmapAdapter:","onBind");
        mMediaStoreImageBitmapCursor.moveToPosition(position);
        int columnIndex = mMediaStoreImageBitmapCursor.getColumnIndex(MediaStore.Images.Media._ID);
        int foldercolunIndex = mMediaStoreImageBitmapCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        Uri imageURI = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(mMediaStoreImageBitmapCursor.getInt(columnIndex)));
        Picasso
                .with(context)
                .load(imageURI)
                .placeholder(ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_default_image,null))
                .error(ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_default_image,null))
                .fit()
                .centerCrop()
                .into(holder.photoBitmapView);
    }

    @Override
    public int getItemCount() {
        return mMediaStoreImageBitmapCursor==null?0:mMediaStoreImageBitmapCursor.getCount();
    }

    public class BitmapHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView photoBitmapView;

        public BitmapHolder(View itemView) {
            super(itemView);
            photoBitmapView = (ImageView) itemView.findViewById(R.id.bitmap_image_view);
            photoBitmapView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mAdapterClickListener.onClick(view,getLayoutPosition());
        }
    }

    public Cursor swapCursor(Cursor cursor){
        if(mMediaStoreImageBitmapCursor==cursor){
            Log.i("ImageBitmapAdapter:","same cursor");
            return null;
        }else{
            Log.i("ImageBitmapAdapter:","different cursor");
            Cursor oldCursor = mMediaStoreImageBitmapCursor;
            mMediaStoreImageBitmapCursor = cursor;
            if(cursor!=null){
                Log.i("ImageBitmapAdapter:","notify data set changed");
                this.notifyDataSetChanged();
            }
            return oldCursor;
        }
    }


    private Bitmap getBitmapFromCursor(int position){
        int idIndex = mMediaStoreImageBitmapCursor.getColumnIndex(MediaStore.Images.Media._ID);
        mMediaStoreImageBitmapCursor.moveToPosition(position);
        return MediaStore.Images.Thumbnails.getThumbnail(
                context.getContentResolver(),
                mMediaStoreImageBitmapCursor.getLong(idIndex),
                MediaStore.Images.Thumbnails.MICRO_KIND,
                null);
    }

    public interface ImageBitMapAdapterClickListener {
        public void onClick(View view,int position);
    }

}
