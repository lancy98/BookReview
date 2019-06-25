package com.lancy.bookreview.userflow;


import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lancy.bookreview.R;
import com.lancy.bookreview.helper.UserInterfaceHelper;
import com.lancy.bookreview.model.User;
import com.roger.catloadinglibrary.CatLoadingView;
import com.squareup.picasso.Picasso;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import de.hdodenhof.circleimageview.CircleImageView;


public class AccountInformationFragment extends Fragment {

    private DatabaseReference userDatabaseReference;
    private User user;
    private TextView usernameTextView;
    private CircleImageView circleImageView;
    private StorageReference mStorageRef;
    private Button saveButton;
    private CatLoadingView catLoadingView = new CatLoadingView();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_information, container, false);
        catLoadingView.setCanceledOnTouchOutside(false);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        circleImageView = view.findViewById(R.id.profileImageView);
        saveButton = view.findViewById(R.id.saveButton);

        addSaveButtonOnClickListener();
        addImageClickListener();

        catLoadingView.show(getActivity().getSupportFragmentManager(), "");
        updateUserInformation();

        return view;
    }

    private void updateUserInformation() {
        if (UserInterfaceHelper.hasUserLoggedIn()) {
            getUserDatabaseReference();
            getUserData();
            getUserImage();
        }
    }

    private void addSaveButtonOnClickListener() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUsername(usernameTextView.getText().toString());
            }
        });
    }

    private void addImageClickListener() {
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
    }

    private void getUserData() {
        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                reloadUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void getUserImage() {
        userDatabaseReference.child("image").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imagePath = dataSnapshot.getValue(String.class);
                if (imagePath != null) {
                    updateImageInImageView(imagePath);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateImageInImageView(String imagePath) {
        Drawable drawable = ResourcesCompat
                .getDrawable(getResources(), R.drawable.ic_person, null);
        Picasso.get().load(imagePath)
                .placeholder(drawable)
                .fit()
                .centerCrop()
                .into(circleImageView);
    }

    private void reloadUI() {
        usernameTextView.setText(user.username);
        catLoadingView.dismiss();
    }

    private String userID() {
        if (UserInterfaceHelper.hasUserLoggedIn()) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        return null;
    }

    private void getUserDatabaseReference() {
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(userID());
    }

    private void pickImage() {
        PickImageDialog.build(new PickSetup())
                .setOnPickResult(new IPickResult() {
                    @Override
                    public void onPickResult(PickResult r) {
                        uploadFile(r.getUri());
                    }
                })
                .setOnPickCancel(new IPickCancel() {
                    @Override
                    public void onCancelClick() {
                        //Do nothing as of now.
                    }
                }).show(getActivity().getSupportFragmentManager());
    }

    private void uploadFile(Uri file) {
        updateImageInImageView(file.toString());
        catLoadingView.show(getActivity().getSupportFragmentManager(), "");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference riversRef = mStorageRef.child("images/" + userID()+".jpg");

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        downloadFileURL(taskSnapshot);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.w("Lancy", "image upload to firebase failed");
                    }
                });
    }

    private void downloadFileURL(UploadTask.TaskSnapshot taskSnapshot) {
        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                updateUserDatabase(uri.toString());
            }
        });
    }

    private void updateUserDatabase(String imageUrl) {
        userDatabaseReference.child("image").setValue(imageUrl)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                catLoadingView.dismiss();
                if (task.isSuccessful()) {
                    Log.i("Lancy", "updated user image");
                } else {
                    Log.i("Lancy", "failed to update user image");
                }
            }
        });
    }

    private void updateUsername(String username) {
        catLoadingView.show(getActivity().getSupportFragmentManager(), "");
        userDatabaseReference.child("username").setValue(username)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                catLoadingView.dismiss();
                if (task.isSuccessful()) {
                    Log.i("Lancy", "updated user name");
                } else {
                    Log.i("Lancy", "failed to update user name");
                }
            }
        });
    }
}
