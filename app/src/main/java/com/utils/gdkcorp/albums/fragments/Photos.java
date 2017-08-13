package com.utils.gdkcorp.albums.fragments;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.activities.ImageViewActivity;
import com.utils.gdkcorp.albums.adapters.AllPhotosAdapter;
import com.utils.gdkcorp.albums.adapters.MediaStoreImageBitmapAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Photos#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Photos extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,MediaStoreImageBitmapAdapter.ImageBitMapAdapterClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
    private static final int MEDIASTORE_LOADER_ID = -2;
    private AllPhotosAdapter mRecyclerViewAdapter;
    private RecyclerView mPhotosRecyclerView;
    private GridLayoutManager gridLayoutManager;

    public Photos() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Photos.
     */
    // TODO: Rename and change types and number of parameters
    public static Photos newInstance() {
        Photos fragment = new Photos();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photos, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPhotosRecyclerView = (RecyclerView) view.findViewById(R.id.all_photos_rview);
        mRecyclerViewAdapter = new AllPhotosAdapter(null,getActivity(),this);
        gridLayoutManager = new GridLayoutManager(getActivity(),4,LinearLayoutManager.VERTICAL,false);
        mPhotosRecyclerView.setLayoutManager(gridLayoutManager);
        mPhotosRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(MEDIASTORE_LOADER_ID,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] PROJECTION = { MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.ORIENTATION,
                MediaStore.Images.Media.DATE_TAKEN
        };
        String SELECTION = MediaStore.Images.Media.DATA + " like ? OR "+MediaStore.Images.Media.DATA + " like ?";
        String[] SELECTION_ARGS = {"%.jpg","%.png"};
        String SORT_ORDER = MediaStore.Images.Media.DATE_TAKEN + " DESC";
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        return new CursorLoader(getActivity(),uri,PROJECTION,SELECTION,SELECTION_ARGS,SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mRecyclerViewAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerViewAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View view, int position) {
        Intent intent = new Intent(view.getContext(),ImageViewActivity.class);
        intent.putExtra(ImageViewActivity.BUNDLE_FOLDER_NAME_EXTRA_KEY,"null");
        intent.putExtra(ImageViewActivity.BUNDLE_IMAGE_POSITION_EXTRA_KEY,position);
        view.getContext().startActivity(intent);
    }
}
