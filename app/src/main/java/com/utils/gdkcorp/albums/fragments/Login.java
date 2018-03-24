package com.utils.gdkcorp.albums.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.utils.gdkcorp.albums.Constants;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.activities.MainActivity;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Login#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Login extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseAuth mAuth;
    private Button mSignIpButton;
    private ProgressDialog mProgress;
    private DatabaseReference mDatabase;
    private EditText mEmailEditText,mPasswordEditText;

    public Login() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Login.
     */
    // TODO: Rename and change types and number of parameters
    public static Login newInstance(String param1, String param2) {
        Login fragment = new Login();
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
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(getActivity());
        mSignIpButton = (Button) view.findViewById(R.id.sign_in_button);
        mEmailEditText = (EditText) view.findViewById(R.id.sign_in_email);
        mPasswordEditText = (EditText) view.findViewById(R.id.sign_in_password);
        mSignIpButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sign_in_button : startSignIn();
                break;
        }
    }

    private void startSignIn() {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        if(!TextUtils.isEmpty(email)
                &&!TextUtils.isEmpty(password)){
            mProgress.setMessage("Signing In ....");
            mProgress.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    mProgress.dismiss();
                    if(task.isSuccessful()){
                        SharedPreferences preferences = getContext().getSharedPreferences(Constants.PREFERENCES.PREFERENCE_KEY, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(Constants.USER.USER_ID_PREFERENCE_KEY,mAuth.getCurrentUser().getUid());
                        editor.commit();
                        Intent intent = new Intent(getActivity(),MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getActivity(),"Sign In Failed",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
