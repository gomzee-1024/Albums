package com.utils.gdkcorp.albums.adapters;

import android.content.Context;
import android.database.Cursor;
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
 * Created by Gautam Kakadiya on 02-08-2017.
 */

public class AllPhotosAdapter extends RecyclerView.Adapter<AllPhotosAdapter.AllPhotosBitmapHolder> {
    private Cursor mMediaStoreImageBitmapCursor;
    private Context context;
    private MediaStoreImageBitmapAdapter.ImageBitMapAdapterClickListener mAdapterClickListener;
    public  AllPhotosAdapter(Cursor cursor, Context context,MediaStoreImageBitmapAdapter.ImageBitMapAdapterClickListener mClickLsitener){
        mMediaStoreImageBitmapCursor = cursor;
        this.context = context;
        mAdapterClickListener = mClickLsitener;
    }


    @Override
    public AllPhotosBitmapHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_photos_item,parent,false);
        AllPhotosBitmapHolder holder = new AllPhotosBitmapHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(AllPhotosBitmapHolder holder, int position) {
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

    @Override
    public void onViewRecycled(AllPhotosBitmapHolder holder) {
        super.onViewRecycled(holder);
        holder.cleanUp();
    }

    public class AllPhotosBitmapHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView photoBitmapView;

        public AllPhotosBitmapHolder(View itemView) {
            super(itemView);
            photoBitmapView = (ImageView) itemView.findViewById(R.id.bitmap_image_view1);
            photoBitmapView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mAdapterClickListener.onClick(view,getLayoutPosition());
        }

        public void cleanUp(){
            photoBitmapView.setImageDrawable(null);
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
}
