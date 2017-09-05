package com.utils.gdkcorp.albums.services;

/**
 * Created by Gautam Kakadiya on 08-08-2017.
 */
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.utils.gdkcorp.albums.AsynkTasks.CompressUploadImageTask;
import com.utils.gdkcorp.albums.Constants;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Example stub job to monitor when there is a change to photos in the media provider.
 */
public class PhotosContentJob extends JobService {
    // The root URI of the media provider, to monitor for generic changes to its content.
    private DatabaseReference mDataRef;
    private StorageReference mStoreRef;
    private FirebaseAuth mAuth;
    static final Uri MEDIA_URI = Uri.parse("content://" + MediaStore.AUTHORITY + "/");

    // Path segments for image-specific URIs in the provider.
    static final List<String> EXTERNAL_PATH_SEGMENTS
            = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPathSegments();

    // The columns we want to retrieve about a particular image.
    static final String[] PROJECTION = new String[] {
            MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA
    };
    static final int PROJECTION_ID = 0;
    static final int PROJECTION_DATA = 1;

    // This is the external storage directory where cameras place pictures.
    static final String DCIM_DIR = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM).getPath();

    // A pre-built JobInfo we use for scheduling our job.
    static final JobInfo JOB_INFO;

    static {
        JobInfo.Builder builder = new JobInfo.Builder(0,
                new ComponentName("com.utils.gdkcorp.albums", PhotosContentJob.class.getName()));
        // Look for specific changes to images in the provider.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.addTriggerContentUri(new JobInfo.TriggerContentUri(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));
            builder.addTriggerContentUri(new JobInfo.TriggerContentUri(MEDIA_URI, 0));
        }
        // Also look for general reports of changes in the overall provider.
        JOB_INFO = builder.build();
    }

    // Fake job work.  A real implementation would do some work on a separate thread.
    final Handler mHandler = new Handler();
    final Runnable mWorker = new Runnable() {
        @Override public void run() {
            scheduleJob(PhotosContentJob.this);
            jobFinished(mRunningParams, false);
        }
    };

    JobParameters mRunningParams;

    // Schedule this job, replace any existing one.
    public static void scheduleJob(Context context) {
        JobScheduler js = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            js = context.getSystemService(JobScheduler.class);
        }
        js.schedule(JOB_INFO);
        Log.i("PhotosContentJob", "JOB SCHEDULED!");
    }

    // Check whether this job is currently scheduled.
    public static boolean isScheduled(Context context) {
        JobScheduler js = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            js = context.getSystemService(JobScheduler.class);
        }
        List<JobInfo> jobs = js.getAllPendingJobs();
        if (jobs == null) {
            return false;
        }
        for (int i=0; i<jobs.size(); i++) {
            if (jobs.get(i).getId() == 0) {
                return true;
            }
        }
        return false;
    }

    // Cancel this job, if currently scheduled.
    public static void cancelJob(Context context) {
        JobScheduler js = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            js = context.getSystemService(JobScheduler.class);
        }
        js.cancel(0);
    }


    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i("PhotosContentJob", "JOB STARTED!");
        String tripId=null;
        mRunningParams = params;
        if(mStoreRef==null && mDataRef==null){
            SharedPreferences preferences = this.getSharedPreferences(Constants.PREFERENCES.PREFERENCE_KEY,MODE_PRIVATE);
            String userId = preferences.getString(Constants.USER.USER_ID_PREFERENCE_KEY,null);
            tripId = preferences.getString(Constants.TRIP.TRIP_ID_PREFERENCE_KEY,null);
            mDataRef = FirebaseDatabase.getInstance().getReference().child("trips").child(tripId).child("pictures");
            mStoreRef = FirebaseStorage.getInstance().getReference().child("pictures");
            mAuth = FirebaseAuth.getInstance();
        }
        // Instead of real work, we are going to build a string to show to the user.
        StringBuilder sb = new StringBuilder();

        // Did we trigger due to a content change?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (params.getTriggeredContentAuthorities() != null) {
                boolean rescanNeeded = false;

                if (params.getTriggeredContentUris() != null) {
                    // If we have details about which URIs changed, then iterate through them
                    // and collect either the ids that were impacted or note that a generic
                    // change has happened.
                    ArrayList<String> ids = new ArrayList<>();
                    for (Uri uri : params.getTriggeredContentUris()) {
                        List<String> path = uri.getPathSegments();
                        if (path != null && path.size() == EXTERNAL_PATH_SEGMENTS.size()+1) {
                            // This is a specific file.
                            ids.add(path.get(path.size()-1));
                        } else {
                            // Oops, there is some general change!
                            rescanNeeded = true;
                        }
                    }

                    if (ids.size() > 0) {
                        // If we found some ids that changed, we want to determine what they are.
                        // First, we do a query with content provider to ask about all of them.
                        StringBuilder selection = new StringBuilder();
                        for (int i=0; i<ids.size(); i++) {
                            if (selection.length() > 0) {
                                selection.append(" OR ");
                            }
                            selection.append(MediaStore.Images.ImageColumns._ID);
                            selection.append("='");
                            selection.append(ids.get(i));
                            selection.append("'");
                        }

                        // Now we iterate through the query, looking at the filenames of
                        // the items to determine if they are ones we are interested in.
                        Cursor cursor = null;
                        boolean haveFiles = false;
                        try {
                            cursor = getContentResolver().query(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    PROJECTION, selection.toString(), null, null);
                            while (cursor.moveToNext()) {
                                // We only care about files in the DCIM directory.
                                final String dir = cursor.getString(PROJECTION_DATA);
                                if (dir.startsWith(DCIM_DIR)) {
                                    if (!haveFiles) {
                                        haveFiles = true;
                                        sb.append("New photos:\n");
                                    }
                                    sb.append(cursor.getInt(PROJECTION_ID));
                                    sb.append(": ");
                                    sb.append(dir);
                                    sb.append("\n");
                                    String file_name_with_extension = Uri.parse(dir).getLastPathSegment();
                                    final String file_name = file_name_with_extension.substring(0,file_name_with_extension.length()-4);
                                    final Uri imageURI = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(cursor.getInt(PROJECTION_ID)));
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageURI);
                                    int original_height = bitmap.getHeight();
                                    int original_width = bitmap.getWidth();
                                    double aspect_ratio;
                                    int bitmap_type=0;
                                    if(original_width>=original_height) {
                                        aspect_ratio = ((double) original_width / original_height);
                                        bitmap_type = Constants.IMAGES_TYPE.LANSCAPE_TYPE;
                                    }else{
                                        aspect_ratio = ((double) original_height / original_width);
                                        bitmap_type = Constants.IMAGES_TYPE.PORTRAIT_TYPE;
                                    }
                                    CompressUploadImageTask task = new CompressUploadImageTask(bitmap,file_name,dir,bitmap_type,tripId);
                                    Log.i("PhotoContentJob","aspect_ratio "+aspect_ratio);
                                    if(aspect_ratio==Constants.IMAGES_ASPECT_RATIO.FOUR_TO_THREE_RATIO){
                                        Log.i("PhotoContentJob","case 0");
                                        task.execute(0);
                                    }else if(aspect_ratio==Constants.IMAGES_ASPECT_RATIO.SIXTEEN_TO_NINE_RATIO){
                                        Log.i("PhotoContentJob","case 1");
                                        task.execute(1);
                                    }else if(aspect_ratio==Constants.IMAGES_ASPECT_RATIO.ONE_TO_ONE_RATIO){
                                        Log.i("PhotoContentJob","case 2");
                                        task.execute(2);
                                    }else{
                                        Log.i("PhotoContentJob","case 3");
                                        task.execute(3);
                                    }
//                                    StorageReference ref = mStoreRef.child(file_name);
//                                    ref.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                        @Override
//                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                            Uri uri = taskSnapshot.getDownloadUrl();
//                                            DatabaseReference dataref = mDataRef.child(file_name);
//                                            dataref.child("picture_id").setValue(dataref.getKey());
//                                            dataref.child("picture_url").setValue(uri.toString());
//                                            dataref.child("taken_by").setValue(mAuth.getCurrentUser().getUid());
//                                            dataref.child("original_uri").setValue(dir.toString());
//                                            Toast.makeText(PhotosContentJob.this,"Image Uploaded",Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
                                }
                            }
                        } catch (SecurityException e) {
                            sb.append("Error: no access to media!");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }

                } else {
                    // We don't have any details about URIs (because too many changed at once),
                    // so just note that we need to do a full rescan.
                    rescanNeeded = true;
                }

                if (rescanNeeded) {
                    sb.append("Photos rescan needed!");
                }
            } else {
                sb.append("(No photos content)");
            }
        }
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();

        // We will emulate taking some time to do this work, so we can see batching happen.
        scheduleJob(PhotosContentJob.this);
        jobFinished(mRunningParams, false);
        return true;
    }



    @Override
    public boolean onStopJob(JobParameters params) {
        mHandler.removeCallbacks(mWorker);
        return false;
    }
}