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
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.customviews.CenterLayoutManager;
import com.utils.gdkcorp.albums.adapters.MediaStoreImageBitmapAdapter;
import com.utils.gdkcorp.albums.customviews.MyViewPager;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.customviews.TouchImageView;
import com.utils.gdkcorp.albums.models.Photo;
import com.utils.gdkcorp.albums.models.Trip;

import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

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
    private RecyclerView mRecyclerView;
    private CenterLayoutManager layoutManager;
    private RecyclerView mRecyclerViewPager;
    private Trip mTrip;
    private int mInitialPosition;
    private String mTripId;
    private DatabaseReference mTripDataRef;
    private DatabaseReference mTripPhotoDataRef;
    private FirebaseRecyclerAdapter<Photo,TripThumbnailImageHolder> firebaseRecyclerAdapter;
    private FirebaseRecyclerAdapter<Photo,TripFullImageHolder> firebaseRecyclerAdapterFull;
    private FirebaseAuth mAuth;
    private List<Photo> mPhotoList;
    private int width,height;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        showSystemUI();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
        viewPager = (MyViewPager) findViewById(R.id.image_view_pager);
        mRecyclerViewPager = (RecyclerView) findViewById(R.id.image_recycler_view_pager);
        mRecyclerView = (RecyclerView) findViewById(R.id.image_view_activity_rview);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerViewPager);
        Intent intent = getIntent();
        if(intent.getAction().equals(Constants.ACTION.OFFLINE_IMAGE_ACTION)) {
            mRecyclerViewPager.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
            folder = intent.getStringExtra(BUNDLE_FOLDER_NAME_EXTRA_KEY);
            folderPosition = intent.getIntExtra(BUNDLE_FOLDER_POSITION_EXTRA_KEY, -2);
            imagePosition = intent.getIntExtra(BUNDLE_IMAGE_POSITION_EXTRA_KEY, 0);
            pagerAdapter = new TouchImageViewPagerAdapter(this, null);
            viewPager.setMyClickListener(new MyViewPager.MyClickListener() {
                @Override
                public void onClick() {
                    if (mRecyclerView.getVisibility() == View.VISIBLE) {
                        mRecyclerView.setVisibility(View.INVISIBLE);
                        viewPager.setBackgroundColor(Color.BLACK);
                        hideSystemUI();
                    } else {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        viewPager.setBackgroundColor(Color.WHITE);
                        showSystemUI();
                    }
                    Log.i("ImageViewActivity", "singleTaped");
                }
            });
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    mRecyclerView.smoothScrollToPosition(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            viewPager.setAdapter(pagerAdapter);
            getSupportLoaderManager().initLoader(folderPosition, null, this);
            layoutManager = new CenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            mRecyclerView.setLayoutManager(layoutManager);
            rViewAdapter = new MediaStoreImageBitmapAdapter(null, this, this);
            mRecyclerView.setAdapter(rViewAdapter);
        }else{
            viewPager.setVisibility(View.GONE);
            mRecyclerViewPager.setVisibility(View.VISIBLE);
            mRecyclerViewPager.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
            mTripId = intent.getStringExtra(Constants.SHARE_DATA_KEYS.TRIP_ID_KEY);
            mInitialPosition = intent.getIntExtra(Constants.SHARE_DATA_KEYS.TRIP_IMAGE_POSITION,0);
            mAuth = FirebaseAuth.getInstance();
            mTripDataRef = FirebaseDatabase.getInstance().getReference().child("trips").child(mTripId);
            mTripDataRef.keepSynced(true);
            mTripPhotoDataRef = mTripDataRef.child("pictures");
            mTripDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mTrip = dataSnapshot.getValue(Trip.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            firebaseRecyclerAdapterFull = new FirebaseRecyclerAdapter<Photo, TripFullImageHolder>(
                    Photo.class,
                    R.layout.trip_photo_rview_pager_item,
                    TripFullImageHolder.class,
                    mTripPhotoDataRef
            ) {
                @Override
                protected void populateViewHolder(final TripFullImageHolder viewHolder, final Photo model, int position) {
                    viewHolder.mPhoto = model;
                    viewHolder.mProgressBar.setVisibility(View.VISIBLE);
                    Log.i("ImageViewActivity","populateViewHolder");
                    viewHolder.target = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Log.i("ImageViewActivity","onBitmapLoaded");
                            //viewHolder.mProgressBar.setVisibility(View.INVISIBLE);
                            viewHolder.mTouchImageView.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            Log.i("ImageViewActivity","onBitmapFailed");
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            viewHolder.mTouchImageView.setImageDrawable(placeHolderDrawable);
                        }
                    };
                    viewHolder.mTouchImageView.setTag(viewHolder.target);
                    Picasso
                            .with(ImageViewActivity.this)
                            .load(model.getPicture_url())
                            .resize(width,height)
                            .centerInside()
                            .placeholder(R.drawable.ic_default_image)
                            .into(viewHolder.target);
                    viewHolder.mTouchImageView.setOnResetZoomListener(new TouchImageView.TouchImageViewResetZoom() {
                        @Override
                        public void onZoomReset(String where) {
                            Log.i("ImageViewActivity","onZoomReset "+where);
                            viewHolder.mTouchImageView.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    int action = motionEvent.getAction();
                                    switch (action) {
                                        case MotionEvent.ACTION_DOWN:

                                        case MotionEvent.ACTION_UP:

                                        case MotionEvent.ACTION_MOVE:
                                            mRecyclerViewPager.requestDisallowInterceptTouchEvent(false);
                                            return true;

                                        default:
                                            return true;
                                    }
                                }
                            });
                        }

                        @Override
                        public void onZoom(String where) {
                            Log.i("ImageViewActivity","onZoom " + where);
                            viewHolder.mTouchImageView.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    int action = motionEvent.getAction();
                                    switch (action) {
                                        case MotionEvent.ACTION_DOWN:
                                            // Disallow ScrollView to intercept touch events.
                                            mRecyclerViewPager.requestDisallowInterceptTouchEvent(true);
                                            // Disable touch on transparent view
                                            return false;

                                        case MotionEvent.ACTION_UP:
                                            // Allow ScrollView to intercept touch events.
                                            mRecyclerViewPager.requestDisallowInterceptTouchEvent(false);
                                            return true;

                                        case MotionEvent.ACTION_MOVE:
                                            mRecyclerViewPager.requestDisallowInterceptTouchEvent(true);
                                            return false;

                                        default:
                                            return true;
                                    }
                                }
                            });
                        }
                    });
                    viewHolder.mTouchImageView.setOnSingleTapListener(new TouchImageView.SingleTapListener() {
                        @Override
                        public void onSingleTap() {
                            if (mRecyclerView.getVisibility() == View.VISIBLE) {
                                mRecyclerView.setVisibility(View.INVISIBLE);
                                mRecyclerViewPager.setBackgroundColor(Color.BLACK);
                                hideSystemUI();
                            } else {
                                mRecyclerView.setVisibility(View.VISIBLE);
                                mRecyclerViewPager.setBackgroundColor(Color.WHITE);
                                showSystemUI();
                            }
                        }
                    });
                }

                @Override
                public void onViewRecycled(TripFullImageHolder holder) {
                    super.onViewRecycled(holder);
                    holder.cleanUp();
                }

                @Override
                public void onDataChanged() {
                    super.onDataChanged();
                    mRecyclerViewPager.scrollToPosition(mInitialPosition);
                }
            };
            mRecyclerViewPager.setAdapter(firebaseRecyclerAdapterFull);
            layoutManager = new CenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            mRecyclerView.setLayoutManager(layoutManager);
            firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Photo, TripThumbnailImageHolder>(
                    Photo.class,
                    R.layout.photo_item,
                    TripThumbnailImageHolder.class,
                    mTripPhotoDataRef
            ) {
                @Override
                protected void populateViewHolder(final TripThumbnailImageHolder viewHolder, final Photo model, final int position) {
                    viewHolder.mPhoto = model;
                    Picasso
                            .with(ImageViewActivity.this)
                            .load(model.getPicture_url())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .fit()
                            .centerCrop()
                            .into(viewHolder.photoBitmapView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso
                                            .with(ImageViewActivity.this)
                                            .load(model.getPicture_url())
                                            .fit()
                                            .centerCrop()
                                            .into(viewHolder.photoBitmapView);
                                }
                            });
                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mRecyclerViewPager.smoothScrollToPosition(position);
                            if(position>=2) {
                                mRecyclerView.smoothScrollToPosition(position);
                            }
                        }
                    });
                }

                @Override
                public void onViewRecycled(TripThumbnailImageHolder holder) {
                    super.onViewRecycled(holder);
                    holder.cleanUp();
                }

                @Override
                public void onDataChanged() {
                    super.onDataChanged();
                    mRecyclerView.scrollToPosition(mInitialPosition);
                }
            };
            mRecyclerView.setAdapter(firebaseRecyclerAdapter);
            mRecyclerViewPager.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if(newState==SCROLL_STATE_IDLE) {
                        int pos =((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                        Log.i("ImageViewActivity","new scrool state idle: "+pos);
                        mRecyclerView.smoothScrollToPosition(pos);
                    }
                }
            });
        }
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
            String[] SELECTION_ARGS = {"%.jp%g","%.png"};
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
            String[] SELECTION_ARGS = {"%" + BUCKET_DISPLAY_NAME + "/%.jp%g", "%" + BUCKET_DISPLAY_NAME + "/%.png"};
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
        mRecyclerView.scrollToPosition(imagePosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        pagerAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View view, int position) {
        viewPager.setCurrentItem(position);
        if(position>=2) {
            mRecyclerView.smoothScrollToPosition(position);
        }
    }

    static class TouchImageViewPagerAdapter extends PagerAdapter {

        int imagePosition;
        Context context;
        Cursor cursor;
        int height,width;

        public TouchImageViewPagerAdapter(Context context,Cursor cursor){
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


    private static class TripThumbnailImageHolder extends RecyclerView.ViewHolder{

        private Photo mPhoto;
        private ImageView photoBitmapView;
        public TripThumbnailImageHolder(View itemView) {
            super(itemView);
            photoBitmapView = (ImageView) itemView.findViewById(R.id.bitmap_image_view);
        }

        public void cleanUp(){
            Picasso.with(itemView.getContext())
                    .cancelRequest(photoBitmapView);
            photoBitmapView.setImageDrawable(null);
        }
    }

    private static class TripFullImageHolder extends RecyclerView.ViewHolder{

        private TouchImageView mTouchImageView;
        private Photo mPhoto;
        private Target target;
        private ProgressBar mProgressBar;
        public TripFullImageHolder(View itemView) {
            super(itemView);
            mTouchImageView = (TouchImageView) itemView.findViewById(R.id.touch_image_view);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        }

        public void cleanUp(){
            Picasso.with(itemView.getContext())
                    .cancelRequest(target);
            mTouchImageView.setImageDrawable(null);
        }
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
    private void showSystemUI(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerViewPager.setAdapter(null);
        mRecyclerView.setAdapter(null);
    }
}
