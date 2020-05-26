package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    EditText UserName,UserStatus;
    ImageView ProfilePic;
    Button UpdateButton;
    String CurrentUserID;
    FirebaseAuth userAuth;
    DatabaseReference rootRef;
    int GalleryPick=1;
    StorageReference UserProfileImagesRef;

    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar= getSupportActionBar();
        actionBar.setTitle("Settings");
        UserName=(EditText)findViewById(R.id.UserNameEditText);
        UserStatus=(EditText)findViewById(R.id.StatusEditText);
        ProfilePic=(ImageView)findViewById(R.id.ProfileImage);
        UpdateButton=(Button)findViewById(R.id.UpdateButton);
        userAuth=FirebaseAuth.getInstance();
        CurrentUserID=userAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
        loadingBar = new ProgressDialog(SettingsActivity.this);

        UpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings();
            }

        });

        RetrieveUserData();

        ProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //select photo from gallery
                Intent toGallery=new Intent();
                toGallery.setAction(Intent.ACTION_GET_CONTENT);

                //now define the type of file which can access
                toGallery.setType("image/*");
                startActivityForResult(toGallery,GalleryPick);
            }
        });
    }

    //for getting result from gallaery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick && resultCode==RESULT_OK &&  data!=null)
        {
            Uri ImageUri=data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK)
            {

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                //this resultURI contain croped image
                Uri resultURI=result.getUri();

                StorageReference filePath=UserProfileImagesRef.child(CurrentUserID + ".jpg");
                filePath.putFile(resultURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this,"Profile Pic Uploaded Successfully",Toast.LENGTH_LONG).show();
                            //link of profile pic in storage ... we have to store it in DB
                            String downloadedUrl = task.getResult().getStorage().getDownloadUrl().toString();

                            //now save in database
                            rootRef.child("User").child(CurrentUserID).child("image")
                                    .setValue(downloadedUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(SettingsActivity.this, "Image saved in Database successfully", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                loadingBar.dismiss();
                                                String error=task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "ERROR: "+error, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        }
                        else
                        {
                            loadingBar.dismiss();
                            String error=task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "ERROR: " + error, Toast.LENGTH_SHORT).show();
                        }

                    }
                });


            }
        }
    }

    private void RetrieveUserData() {

        rootRef.child("User").child(CurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //if exists the show data
                if((dataSnapshot.child("name").exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image")))
                {
                    //retrive
                    String retriveUserName= dataSnapshot.child("name").getValue().toString();
                    String retriveUserStatus= dataSnapshot.child("status").getValue().toString();
                    String retriveDP= dataSnapshot.child("image").getValue().toString();

                    UserName.setText(retriveUserName);
                    UserStatus.setText(retriveUserStatus);
                    Picasso.get().load(retriveDP).into(ProfilePic);

                }
                else if((dataSnapshot.child("name").exists()) && (dataSnapshot.hasChild("name")))//not image
                {
                    //retrive
                    String retriveUserName= dataSnapshot.child("name").getValue().toString();
                    String retriveUserStatus= dataSnapshot.child("status").getValue().toString();

                    UserName.setText(retriveUserName);
                    UserStatus.setText(retriveUserStatus);
                }
                else//none of this exist
                {
                    Toast.makeText(SettingsActivity.this,"Please update your Profile...",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateSettings()
    {
        String setUserName= UserName.getText().toString();
        String setStatus=UserStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(SettingsActivity.this,"Please write Username...",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(SettingsActivity.this,"Please write Status...",Toast.LENGTH_LONG).show();
        }
        else
        {

            //we store the info in database...
            HashMap<String,String> profileMap = new HashMap<>();
            profileMap.put("uid",CurrentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);

            //By using rootRef we are going to save this

            rootRef.child("User").child(CurrentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                sendUserToMain();
                                Toast.makeText(SettingsActivity.this,"Profile Updated Successfully...",Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                String error=task.getException().toString();
                                Toast.makeText(SettingsActivity.this,"ERROR: "+error,Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }
    }

    private void sendUserToMain() {
        Intent toMainInIntent= new Intent(SettingsActivity.this,MainActivity.class);
        toMainInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toMainInIntent);
        finish();
    }
}
