package com.utils.gdkcorp.albums.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.utils.gdkcorp.albums.customviews.CenterLayoutManager;
import com.utils.gdkcorp.albums.adapters.MediaStoreImageBitmapAdapter;
import com.utils.gdkcorp.albums.customviews.MyViewPager;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.customviews.TouchImageView;

public class ImageViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,MediaStoreImageBitmapAdapter.ImageBitMapAdapterClickListener {

    private MyViewPager viewPager;
    private String folder;
    private int folderPosition;
    private int imagePosition;
    private TouchImageViewPagerAdapter pagerAdapter;
    public static final String BUNDLE_FOLDER_NAME_EXTRA_KEY = "folder_name";
    public static final String BUNDLE_FOLDER_POSITION_EXTRA_KEY = "folder_position";
    public static final String BUNDLE_IMAGE_POSITION_EXTRA_KEY = "image_position";
    private MediaStoreImageBitmapAdapter rViewAdapter;
    private RecyclerView recyclerView;
    private CenterLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        hideSystemUI();
        Intent intent = getIntent();
        folder = intent.getStringExtra(BUNDLE_FOLDER_NAME_EXTRA_KEY);
        folderPosition = intent.getIntExtra(BUNDLE_FOLDER_POSITION_EXTRA_KEY,-2);
        imagePosition = intent.getIntExtra(BUNDLE_IMAGE_POSITION_EXTRA_KEY,0);
        viewPager = (MyViewPager) findViewById(R.id.image_view_pager);
        pagerAdapter = new TouchImageViewPagerAdapter(this,null);
        viewPager.setMyClickListener(new MyViewPager.MyClickListener(){
            @Override
            public void onClick() {
                if(recyclerView.getVisibility()==View.VISIBLE) {
                    recyclerView.setVisibility(View.INVISIBLE);
                    viewPager.setBackgroundColor(Color.BLACK);
                }else{
                    recyclerView.setVisibility(View.VISIBLE);
                    viewPager.setBackgroundColor(Color.WHITE);
                }
                Log.i("ImageViewActivity","singleTaped");
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                recyclerView.smoothScrollToPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setAdapter(pagerAdapter);
        getSupportLoaderManager().initLoader(folderPosition, null, this);
        recyclerView  = (RecyclerView) findViewById(R.id.image_view_activity_rview);
        layoutManager = new CenterLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
        rViewAdapter = new MediaStoreImageBitmapAdapter(null,this,this);
        recyclerView.setAdapter(rViewAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(folder.compareTo("null")==0){
            String[] PROJECTION = { MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.ORIENTATION,
                    MediaStore.Images.Media.DATE_TAKEN
            };
            String SELECTION = MediaStore.Images.Media.DATA + " like ? OR "+MediaStore.Images.Media.DATA + " like ?";
            String[] SELECTION_ARGS = {"%.jpg","%.png"};
            String SORT_ORDER = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            return new CursorLoader(ImageViewActivity.this,uri,PROJECTION,SELECTION,SELECTION_ARGS,SORT_ORDER);
        }else {
            String BUCKET_DISPLAY_NAME = folder;
            String[] PROJECTION = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.ORIENTATION,
                    MediaStore.Images.Media.DATE_TAKEN
            };
            String SELECTION = MediaStore.Images.Media.DATA + " like ? OR " + MediaStore.Images.Media.DATA + " like ?";
            String[] SELECTION_ARGS = {"%" + BUCKET_DISPLAY_NAME + "/%.jpg", "%" + BUCKET_DISPLAY_NAME + "/%.png"};
            String SORT_ORDER = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            return new CursorLoader(ImageViewActivity.this, uri, PROJECTION, SELECTION, SELECTION_ARGS, SORT_ORDER);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i("ImageViewActivity","data "+data.getCount());
        pagerAdapter.swapCursor(data);
        viewPager.setCurrentItem(imagePosition,false);
        rViewAdapter.swapCursor(data);
        recyclerView.scrollToPosition(imagePosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        pagerAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View view, int position) {
        viewPager.setCurrentItem(position);
//        int first = layoutManager.findFirstVisibleItemPosition();
//        int last = layoutManager.findLastVisibleItemPosition();
//        Log.i("ImageViewActivity","firstVisible "+ first);
//        Log.i("ImageViewActivity","lastVisible "+ last);
//        int centrePosition = (first+last)/2;
//        Log.i("ImageViewActivity","center Position "+centrePosition);
        if(position>=2) {
            recyclerView.smoothScrollToPosition(position);
        }
    }

    static class TouchImageViewPagerAdapter extends PagerAdapter {

        int imagePosition;
        Context context;
        Cursor cursor;
        int height,width;

        public TouchImageViewPagerAdapter(Context context,Cursor cursor){
            this.imagePosition = imagePosition;
            this.context = context;
            this.cursor = cursor;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((AppCompatActivity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            height = displayMetrics.heightPixels;
            width = displayMetrics.widthPixels;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.i("ImageViewActivity","instantiateItem");
            if(cursor==null){
                Log.i("ImageViewActivity","cursor null");
            }else{
                Log.i("ImageViewActivity","cursor not null "+cursor.getCount());
            }
            //View imagelayout = ((AppCompatActivity)context).getLayoutInflater().inflate(R.layout.image_view_pager_item,container,false);
            //final TouchImageView touchImageView = (TouchImageView) imagelayout.findViewById(R.id.touch_image_view);
            final TouchImageView touchImageView = new TouchImageView(container.getContext());
            cursor.moveToPosition(position);
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Uri imageURI = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(cursor.getInt(columnIndex)));
            Log.i("ImageViewActivity",imageURI.toString());
//            Drawable myDrawable=null;
//            try {
//                InputStream inputStream = container.getContext().getContentResolver().openInputStream(imageURI);
//                myDrawable = Drawable.createFromStream(inputStream, imageURI.toString() );
//            } catch (FileNotFoundException e) {
//                myDrawable = container.getContext().getDrawable(R.drawable.ic_default_image);
//            }
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d("ImageViewAcivity","onBitmapLoaded");
                    touchImageView.setImageBitmap (bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Log.d("ImageViewAcivity","onBitmapFailed");
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Log.d("ImageViewAcivity","onPrepareLoad");
                    touchImageView.setImageDrawable (placeHolderDrawable);
                }
            };
            touchImageView.setTag(target);
            Picasso
                    .with(context)
                    .load(imageURI)
                    .resize(width,height)
                    .centerInside()
                    .placeholder(R.drawable.ic_default_image)
                    .into(target);
//            touchImageView.setImageDrawable(myDrawable);
            //container.addView(imagelayout,0);
            container.addView(touchImageView,ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            return touchImageView;
        }

        @Override
        public int getCount() {
            return cursor==null?0:cursor.getCount();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        private Cursor swapCursor(Cursor data) {
            if(cursor==data){
                return null;
            }else{
                Cursor oldCursor = cursor;
                cursor = data;
                if(data!=null){
                    Log.i("ImageViewActivity","notifiDataSetChanged");
                    this.notifyDataSetChanged();
                }
                return oldCursor;
            }
        }
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
}
