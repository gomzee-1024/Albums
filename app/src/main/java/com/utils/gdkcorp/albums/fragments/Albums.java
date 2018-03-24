package com.utils.gdkcorp.albums.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.adapters.MediaStoreImageFolderAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Albums.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Albums#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Albums extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
    private static final int MEDIASTORE_LOADER_ID = -1;
    private RecyclerView mAlbumsRecyclerView;
    private MediaStoreImageFolderAdapter mRecyclerViewAdapter;
    private LinearLayoutManager layoutManager;
    private Parcelable mainRecyclerScrollOffset;
    private Parcelable[] childScrollState;
    private int x;
    public Albums() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Albums.
     */
    // TODO: Rename and change types and number of parameters
    public static Albums newInstance(Parcelable mainRViewState,Parcelable[] childRViewState) {
        Albums fragment = new Albums();
        Bundle args = new Bundle();
        args.putParcelable(Constants.SHARE_DATA_KEYS.MAIN_RVIEW_OFFSET, mainRViewState);
        args.putParcelableArray(Constants.SHARE_DATA_KEYS.CHILD_RVIEW_OFFSET, childRViewState);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Albums","onCreate");
        if (getArguments() != null) {
            Log.i("Albums","ArgumentsNotNull");
            mainRecyclerScrollOffset = getArguments().getParcelable(Constants.SHARE_DATA_KEYS.MAIN_RVIEW_OFFSET);
            childScrollState = getArguments().getParcelableArray(Constants.SHARE_DATA_KEYS.CHILD_RVIEW_OFFSET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_albums,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("Albums","onViewCreated");
        if(savedInstanceState!=null) {
            Log.i("Albums","savedInstanceStateNotNUll");
            childScrollState = savedInstanceState.getParcelableArray(Constants.SHARE_DATA_KEYS.CHILD_RVIEW_OFFSET);
            mainRecyclerScrollOffset = savedInstanceState.getParcelable(Constants.SHARE_DATA_KEYS.MAIN_RVIEW_OFFSET);
        }
        mAlbumsRecyclerView = (RecyclerView) view.findViewById(R.id.albums_rview);
        mRecyclerViewAdapter = new MediaStoreImageFolderAdapter(null,getActivity(), childScrollState);
        layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        mAlbumsRecyclerView.setLayoutManager(layoutManager);
        mAlbumsRecyclerView.setAdapter(mRecyclerViewAdapter);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("Albums","onActivityCreated");
        getActivity().getSupportLoaderManager().initLoader(MEDIASTORE_LOADER_ID,null,this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { "DISTINCT "+MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.DATE_ADDED
        };
        String GROUP_BY = "1) GROUP BY (1";
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String sortOrder = MediaStore.Images.Media.BUCKET_DISPLAY_NAME ;
        return new CursorLoader(getActivity(),uri,projection,GROUP_BY,null,sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(mRecyclerViewAdapter!=null) {
            mRecyclerViewAdapter.swapCursor(data);
        }
        if(mainRecyclerScrollOffset!=null) {
            layoutManager.onRestoreInstanceState(mainRecyclerScrollOffset);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(mRecyclerViewAdapter!=null) {
            mRecyclerViewAdapter.swapCursor(null);
        }
    }

    @Override
    public void onDestroyView() {
        Log.i("Albums","onDestroyView");
        if(mAlbumsRecyclerView!=null) {
            mAlbumsRecyclerView.setAdapter(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.i("Albums","onDestroy");
        super.onDestroy();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public Parcelable[] getChildScrollState(){
        return mRecyclerViewAdapter.getScrollOffsetList();
    }

    public Parcelable getMainRecyclerScrollOffset(){
        return layoutManager.onSaveInstanceState();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i("Albums","OnSaveInstanceState");
        outState.putParcelableArray(Constants.SHARE_DATA_KEYS.CHILD_RVIEW_OFFSET,mRecyclerViewAdapter.getScrollOffsetList());
        outState.putParcelable(Constants.SHARE_DATA_KEYS.MAIN_RVIEW_OFFSET,layoutManager.onSaveInstanceState());
        super.onSaveInstanceState(outState);
//        outState.putInt(Constants.SHARE_DATA_KEYS.MAIN_RVIEW_OFFSET,mainRecyclerScrollOffset);
//        outState.putIntArray(Constants.SHARE_DATA_KEYS.CHILD_RVIEW_OFFSET,mRecyclerViewAdapter.getScrollOffsetList());
    }
}
