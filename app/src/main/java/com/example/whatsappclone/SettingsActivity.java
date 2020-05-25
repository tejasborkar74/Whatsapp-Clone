package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    EditText UserName,UserStatus;
    ImageView ProfilePic;
    Button UpdateButton;
    String CurrentUserID;
    FirebaseAuth userAuth;
    DatabaseReference rootRef;

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

        UpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings();
            }

        });

        RetrieveUserData();
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
