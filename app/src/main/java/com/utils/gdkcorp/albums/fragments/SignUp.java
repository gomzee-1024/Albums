package com.utils.gdkcorp.albums.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.activities.MainActivity;
import com.utils.gdkcorp.albums.models.User;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUp#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUp extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseAuth mAuth;
    private Button mSignUpButton;
    private ProgressDialog mProgress;
    private DatabaseReference mDatabase;
    private EditText mFirstNameEditText,mLastNameEditText,mEmailEditText,mPasswordeditText;
    private ImageView addProfilePicButton,profilePicImage;
    private Uri mProfilePicUri;
    private StorageReference mStoreRef;

    public SignUp() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUp.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUp newInstance(String param1, String param2) {
        SignUp fragment = new SignUp();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mStoreRef = FirebaseStorage.getInstance().getReference().child("photos");
        mProgress = new ProgressDialog(getActivity());
        mEmailEditText = (EditText) view.findViewById(R.id.sign_up_email);
        mPasswordeditText = (EditText) view.findViewById(R.id.sign_up_password);
        mFirstNameEditText = (EditText) view.findViewById(R.id.first_name);
        mLastNameEditText = (EditText) view.findViewById(R.id.last_name);
        profilePicImage = (ImageView) view.findViewById(R.id.profile_picture);
        addProfilePicButton = (ImageView) view.findViewById(R.id.add_profle_pic_button);
        addProfilePicButton.setOnClickListener(this);
        mSignUpButton = (Button) view.findViewById(R.id.sign_up_button);
        mSignUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Log.i("SignUp","OnClicked");
        switch (view.getId()){
            case R.id.sign_up_button : startSignUp();
                break;
            case R.id.add_profle_pic_button :
                CropImage.activity()
                        .setRequestedSize(720,720)
                        .setAspectRatio(500,500)
                        .setFixAspectRatio(true)
                        .start(getContext(), this);
                break;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProfilePicUri = result.getUri();
                profilePicImage.setImageURI(mProfilePicUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void startSignUp() {
        Log.i("SignUp","Signing up");
        final String first_name = mFirstNameEditText.getText().toString();
        final String last_name = mLastNameEditText.getText().toString();
        String email = mEmailEditText.getText().toString();
        String password = mPasswordeditText.getText().toString();
        if(!TextUtils.isEmpty(first_name)
                &&!TextUtils.isEmpty(email)
                &&!TextUtils.isEmpty(password)){
            Log.i("SignUp","Signing up");
            mProgress.setMessage("Signing Up...");
            mProgress.show();
            final User user = new User();
            user.setName(first_name+" "+last_name);
            user.setName_lowercase(first_name.toLowerCase()+" "+last_name.toLowerCase());
            user.setRegistration_token(FirebaseInstanceId.getInstance().getToken());
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String userId = mAuth.getCurrentUser().getUid();
                        user.setUser_id(userId);
                        SharedPreferences sharedPref = getContext().getSharedPreferences(Constants.PREFERENCES.PREFERENCE_KEY, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(Constants.USER.USER_ID_PREFERENCE_KEY,userId);
                        editor.commit();
                        final DatabaseReference ref = mDatabase.child(userId);
                        ref.child("user_id").setValue(user.getUser_id());
                        ref.child("name_lowercase").setValue(user.getName_lowercase());
                        ref.child("name").setValue(user.getName());
                        ref.child("registration_token").setValue(user.getRegistration_token());
                        if(mProfilePicUri!=null) {
                            mStoreRef.child(mProfilePicUri.getLastPathSegment()).putFile(mProfilePicUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    user.setProfile_pic_url(taskSnapshot.getDownloadUrl().toString());
                                    ref.child("profile_pic_url").setValue(user.getProfile_pic_url());
                                    mProgress.dismiss();
                                    Intent intent = new Intent(getActivity(),MainActivity.class);
                                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            });
                        }else{
                            ref.child("profile_pic_url").setValue(Constants.AVATAR.DEFAULT_AVATAR_URL);
                            mProgress.dismiss();
                            Intent intent = new Intent(getActivity(),MainActivity.class);
                            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }

                    }else{
                        mProgress.dismiss();
                        Toast.makeText(getActivity(),"SignUp Failed",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
