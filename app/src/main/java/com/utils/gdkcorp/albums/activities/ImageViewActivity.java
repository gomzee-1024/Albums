package com.utils.gdkcorp.albums.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.GlideApp;
import com.utils.gdkcorp.albums.customviews.CenterLayoutManager;
import com.utils.gdkcorp.albums.adapters.MediaStoreImageBitmapAdapter;
import com.utils.gdkcorp.albums.customviews.MyViewPager;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.customviews.TouchImageView;
import com.utils.gdkcorp.albums.models.Photo;
import com.utils.gdkcorp.albums.models.Trip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

public class ImageViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

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
    private FirebaseRecyclerAdapter<Photo, TripThumbnailImageHolder> firebaseRecyclerAdapter;
    private FirebaseRecyclerAdapter<Photo, TripFullImageHolder> firebaseRecyclerAdapterFull;
    private FirebaseAuth mAuth;
    private List<Photo> mPhotoList;
    private int width, height;
    private Toolbar mToolbar;
    private boolean onlineMode;
    private int curPage;
    private ImageView mDeleteButton,mShareButton;
    private View.OnClickListener mGenClickListener;
    private MediaStoreImageBitmapAdapter.ImageBitMapAdapterClickListener mMediaAdapterClickListener;
    private RecyclerView.OnScrollListener mScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_image_view);
        setSupportActionBar(mToolbar);
        showSystemUI();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
        viewPager = (MyViewPager) findViewById(R.id.image_view_pager);
        mRecyclerViewPager = (RecyclerView) findViewById(R.id.image_recycler_view_pager);
        mRecyclerView = (RecyclerView) findViewById(R.id.image_view_activity_rview);
        mDeleteButton = (ImageView) findViewById(R.id.delete_image);
        setUpmGenClickListener();
        mDeleteButton.setOnClickListener(mGenClickListener);
        mShareButton = (ImageView) findViewById(R.id.share_image);
        mShareButton.setOnClickListener(mGenClickListener);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerViewPager);
        Intent intent = getIntent();
        if (intent.getAction().equals(Constants.ACTION.OFFLINE_IMAGE_ACTION)) {
            onlineMode = false;
            mRecyclerViewPager.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
            folder = intent.getStringExtra(BUNDLE_FOLDER_NAME_EXTRA_KEY);
            folderPosition = intent.getIntExtra(BUNDLE_FOLDER_POSITION_EXTRA_KEY, -2);
            imagePosition = intent.getIntExtra(BUNDLE_IMAGE_POSITION_EXTRA_KEY, 0);
            curPage = imagePosition;
            pagerAdapter = new TouchImageViewPagerAdapter(null);
            viewPager.setMyClickListener(new MyViewPager.MyClickListener() {
                @Override
                public void onClick() {
                    if (mRecyclerView.getVisibility() == View.VISIBLE) {
                        mRecyclerView.setVisibility(View.INVISIBLE);
                        viewPager.setBackgroundColor(Color.BLACK);
                        hideSystemUI();
                    } else {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        viewPager.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary));
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
                    curPage = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            viewPager.setAdapter(pagerAdapter);
            getSupportLoaderManager().initLoader(folderPosition, null, this);
            layoutManager = new CenterLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
            mRecyclerView.setLayoutManager(layoutManager);
            setUpmMediaAdapterClickListener();
            rViewAdapter = new MediaStoreImageBitmapAdapter(null, mMediaAdapterClickListener);
            mRecyclerView.setAdapter(rViewAdapter);
        } else {
            onlineMode = true;
            viewPager.setVisibility(View.GONE);
            mRecyclerViewPager.setVisibility(View.VISIBLE);
            mRecyclerViewPager.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
            mTripId = intent.getStringExtra(Constants.SHARE_DATA_KEYS.TRIP_ID_KEY);
            mInitialPosition = intent.getIntExtra(Constants.SHARE_DATA_KEYS.TRIP_IMAGE_POSITION, 0);
            curPage = mInitialPosition;
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
                    viewHolder.mTouchImageView.setImageDrawable(null);
                    viewHolder.mTouchImageView.setVisibility(View.INVISIBLE);
                    viewHolder.mProgressBar.setVisibility(View.VISIBLE);
                    Log.i("ImageViewActivity", "populateViewHolder");
//                    viewHolder.target = new Target() {
//                        @Override
//                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                            Log.i("ImageViewActivity", "onBitmapLoaded");
//                            viewHolder.mTouchImageView.setImageBitmap(bitmap);
//                            viewHolder.mTouchImageView.setVisibility(View.VISIBLE);
//                            viewHolder.mProgressBar.setVisibility(View.INVISIBLE);
//                        }
//
//                        @Override
//                        public void onBitmapFailed(Drawable errorDrawable) {
//                            Log.i("ImageViewActivity", "onBitmapFailed");
//                        }
//
//                        @Override
//                        public void onPrepareLoad(Drawable placeHolderDrawable) {
//                            viewHolder.mTouchImageView.setImageDrawable(placeHolderDrawable);
//                        }
//                    };
                    viewHolder.target = new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            Log.i("ImageViewActivity", "onBitmapLoaded");
                            viewHolder.mTouchImageView.setImageDrawable(resource);
                            viewHolder.mTouchImageView.setVisibility(View.VISIBLE);
                            viewHolder.mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    };
//                    viewHolder.mTouchImageView.setTag(viewHolder.target);
//                    Picasso
//                            .with(getApplicationContext())
//                            .load(model.getPicture_url())
//                            .resize(width, height)
//                            .centerInside()
//                            .placeholder(R.drawable.ic_default_image)
//                            .into(viewHolder.target);
                    GlideApp
                            .with(getApplicationContext())
                            .load(model.getPicture_url())
                            .centerInside()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.ic_default_image)
                            .into(viewHolder.target);
                    viewHolder.mTouchImageView.setOnResetZoomListener(new TouchImageView.TouchImageViewResetZoom() {
                        @Override
                        public void onZoomReset(String where) {
                            Log.i("ImageViewActivity", "onZoomReset " + where);
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
                            Log.i("ImageViewActivity", "onZoom " + where);
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
                                mRecyclerViewPager.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary));
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
            layoutManager = new CenterLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
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
//                    Picasso
//                            .with(getApplicationContext())
//                            .load(model.getPicture_url())
//                            .networkPolicy(NetworkPolicy.OFFLINE)
//                            .fit()
//                            .centerCrop()
//                            .into(viewHolder.photoBitmapView, new Callback() {
//                                @Override
//                                public void onSuccess() {
//
//                                }
//
//                                @Override
//                                public void onError() {
//                                    Picasso
//                                            .with(getApplicationContext())
//                                            .load(model.getPicture_url())
//                                            .fit()
//                                            .centerCrop()
//                                            .into(viewHolder.photoBitmapView);
//                                }
//                            });
                    viewHolder.target =  GlideApp
                            .with(getApplicationContext())
                            .asBitmap()
                            .load(model.getPicture_url())
                            .placeholder(R.drawable.ic_default_image)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .centerCrop()
                            .into(viewHolder.photoBitmapView);
                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mRecyclerViewPager.smoothScrollToPosition(position);
                            if (position >= 2) {
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
            mScrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == SCROLL_STATE_IDLE) {
                        int pos = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                        curPage = pos;
                        Log.i("ImageViewActivity", "new scrool state idle: " + pos);
                        mRecyclerView.smoothScrollToPosition(pos);
                    }
                }
            };
            mRecyclerViewPager.addOnScrollListener(mScrollListener);
        }
    }

    private void setUpmMediaAdapterClickListener() {
        mMediaAdapterClickListener = new MediaStoreImageBitmapAdapter.ImageBitMapAdapterClickListener() {
            @Override
            public void onClick(View view, int position) {
                viewPager.setCurrentItem(position);
                if (position >= 2) {
                    mRecyclerView.smoothScrollToPosition(position);
                }
            }
        };
    }

    private void setUpmGenClickListener() {
        mGenClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.delete_image:
                        deleteImage();
                        break;
                    case R.id.share_image:
                        shareImage();
                        break;
                }
            }
        };
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (folder.compareTo("null") == 0) {
            String[] PROJECTION = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.ORIENTATION,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DATA
            };
            String SELECTION = MediaStore.Images.Media.DATA + " like ? OR " + MediaStore.Images.Media.DATA + " like ?";
            String[] SELECTION_ARGS = {"%.jp%g", "%.png"};
            String SORT_ORDER = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            return new CursorLoader(getApplicationContext(), uri, PROJECTION, SELECTION, SELECTION_ARGS, SORT_ORDER);
        } else {
            String BUCKET_DISPLAY_NAME = folder;
            String[] PROJECTION = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.ORIENTATION,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DATA
            };
            String SELECTION = MediaStore.Images.Media.DATA + " like ? OR " + MediaStore.Images.Media.DATA + " like ?";
            String[] SELECTION_ARGS = {"%" + BUCKET_DISPLAY_NAME + "/%.jp%g", "%" + BUCKET_DISPLAY_NAME + "/%.png"};
            String SORT_ORDER = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            return new CursorLoader(getApplicationContext(), uri, PROJECTION, SELECTION, SELECTION_ARGS, SORT_ORDER);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i("ImageViewActivity", "data " + data.getCount());
        pagerAdapter.swapCursor(data);
        if(curPage<=(data.getCount()-1)) {
            viewPager.setCurrentItem(curPage, false);
        }else{
            if(data.getCount()==0){
                finish();
            }else{
                viewPager.setCurrentItem(curPage-1,false);
            }
        }
        rViewAdapter.swapCursor(data);
        if(imagePosition>=2) {
            if(curPage<=(data.getCount()-1)) {
                mRecyclerView.scrollToPosition(curPage);
            }else{
                mRecyclerView.scrollToPosition(curPage-1);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        pagerAdapter.swapCursor(null);
    }

    private void shareImage() {
        if(!onlineMode){
            Cursor cursor = pagerAdapter.getCursor();
            cursor.moveToPosition(curPage);
            int dataIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String path = cursor.getString(dataIndex);
            File file = new File(path);
            Uri uri = Uri.parse("file://"+file.getAbsolutePath());
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM,uri);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent,Constants.SHARE_DATA_KEYS.SHARE_DATA_KEY));
        }else{
            TripFullImageHolder holder = (TripFullImageHolder) mRecyclerViewPager.findViewHolderForLayoutPosition(curPage);
            BitmapDrawable drawable = (BitmapDrawable) holder.mTouchImageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            File file = new File(getCacheDir(), "temp" + ".png");
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            file.setReadable(true, false);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(intent.createChooser(intent,Constants.SHARE_DATA_KEYS.SHARE_DATA_KEY));
        }
    }

    private void deleteImage() {
        if (!onlineMode) {
            Cursor cursor = pagerAdapter.getCursor();
            cursor.moveToPosition(curPage);
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            final Uri imageURI = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(cursor.getInt(columnIndex)));
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Are you sure, You wanted to delete this Image?");
            alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    //Toast.makeText(ImageViewActivity.this, "You clicked yes button", Toast.LENGTH_LONG).show();
                    getContentResolver().delete(imageURI,null,null);
                }
            });

            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();
        }else{
            Log.i("ImageViewActivity","CurPage = "+curPage);
            final TripFullImageHolder holder = (TripFullImageHolder) mRecyclerViewPager.findViewHolderForLayoutPosition(curPage);
            final StorageReference storeRef = FirebaseStorage.getInstance().getReference().child("pictures");
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("trips").child(mTripId).child("pictures").child(holder.mPhoto.getPicture_id());
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Are you sure, You wanted to delete this Image?");
            alertDialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    storeRef.child(holder.mPhoto.getPicture_id()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            ref.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i("ImageViewActivity","Success delete image from db");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("ImageViewActivity","Failure delete image from db");
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("ImageViewActivity","Failed to delete online Image");
                        }
                    });
                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();
        }
    }

    private class TouchImageViewPagerAdapter extends PagerAdapter {

        int imagePosition;
        Cursor cursor;

        public TouchImageViewPagerAdapter(Cursor cursor) {
            this.cursor = cursor;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.i("ImageViewActivity", "instantiateItem");
            if (cursor == null) {
                Log.i("ImageViewActivity", "cursor null");
            } else {
                Log.i("ImageViewActivity", "cursor not null " + cursor.getCount());
            }
            final TouchImageView touchImageView = new TouchImageView(container.getContext());
            cursor.moveToPosition(position);
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Uri imageURI = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(cursor.getInt(columnIndex)));
            Log.i("ImageViewActivity", imageURI.toString());
            GlideApp
                    .with(getApplicationContext())
                    .load(imageURI)
                    .centerInside()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.ic_default_image)
                    .into(touchImageView);
            container.addView(touchImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return touchImageView;
        }

        @Override
        public int getCount() {
            return cursor == null ? 0 : cursor.getCount();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            TouchImageView view = (TouchImageView) object;
            view.setImageDrawable(null);
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        private Cursor swapCursor(Cursor data) {
            if (cursor == data) {
                return null;
            } else {
                Cursor oldCursor = cursor;
                cursor = data;
                if (data != null) {
                    Log.i("ImageViewActivity", "notifiDataSetChanged");
                    this.notifyDataSetChanged();
                }
                return oldCursor;
            }
        }

        private Cursor getCursor() {
            return cursor;
        }
    }


    private static class TripThumbnailImageHolder extends RecyclerView.ViewHolder {

        private Photo mPhoto;
        private ImageView photoBitmapView;
        private Target<Bitmap> target;
        public TripThumbnailImageHolder(View itemView) {
            super(itemView);
            photoBitmapView = (ImageView) itemView.findViewById(R.id.bitmap_image_view);
        }

        public void cleanUp() {
//            Picasso.with(itemView.getContext())
//                    .cancelRequest(photoBitmapView);
            GlideApp.with(itemView.getContext().getApplicationContext()).clear(target);
            photoBitmapView.setImageDrawable(null);
        }
    }

    private static class TripFullImageHolder extends RecyclerView.ViewHolder {

        private TouchImageView mTouchImageView;
        private Photo mPhoto;
        private SimpleTarget<Drawable> target;
        private ProgressBar mProgressBar;

        public TripFullImageHolder(View itemView) {
            super(itemView);
            mTouchImageView = (TouchImageView) itemView.findViewById(R.id.touch_image_view);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            mProgressBar.getIndeterminateDrawable().setColorFilter(0xFFBDBDBD, PorterDuff.Mode.MULTIPLY);
        }

        public void cleanUp() {
//            Picasso.with(itemView.getContext())
//                    .cancelRequest(target);
//            mTouchImageView.setVisibility(View.INVISIBLE);
//            mTouchImageView.setImageDrawable(null);
//            mProgressBar.setVisibility(View.VISIBLE);
            GlideApp.with(itemView.getContext().getApplicationContext()).clear(target);
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
        mToolbar.setVisibility(View.INVISIBLE);
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mToolbar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        mGenClickListener=null;
        mMediaAdapterClickListener = null;
        if(mRecyclerViewPager!=null) {
            mRecyclerViewPager.setAdapter(null);
            if(mScrollListener!=null)
                mRecyclerViewPager.removeOnScrollListener(mScrollListener);
        }
        mRecyclerView.setAdapter(null);
        viewPager.setAdapter(null);
        if(firebaseRecyclerAdapter!=null)
            firebaseRecyclerAdapter.cleanup();
        if(firebaseRecyclerAdapterFull!=null)
            firebaseRecyclerAdapterFull.cleanup();
        firebaseRecyclerAdapterFull=null;
        firebaseRecyclerAdapter=null;
        super.onDestroy();
    }
}
