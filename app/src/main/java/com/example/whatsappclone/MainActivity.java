package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    FirebaseUser currentUser;
    FirebaseAuth userAuth;
    DatabaseReference rootRef;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userAuth=FirebaseAuth.getInstance();
        currentUser=userAuth.getCurrentUser();
        rootRef= FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser==null)//not loged in
        {
            sendUserToLogin();
        }
        else
        {
            verifyTheUser();
        }
    }

    private void verifyTheUser()
    {
        String currentUserID=userAuth.getCurrentUser().getUid();

        rootRef.child("User").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //old user ...who have already set username and status
                if((dataSnapshot.child("name").exists())) //here name is key of hash map use always same
                {
                    //Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_LONG).show();
                }
                else
                {
                    //just signed up but not updated username
                    sendUserToSettings();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void sendUserToLogin()
    {
        Intent intent= new Intent(getApplicationContext(),loginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_options,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId()==R.id.Settings)
         {
             sendUserToSettings();
         }
        if(item.getItemId()==R.id.Log_out)
        {
            userAuth.signOut();
            sendUserToLogin();
        }
        if(item.getItemId()==R.id.Find_Friends)
        {

        }
    return true;
    }

    public void sendUserToSettings()
    {
        Intent i= new Intent(getApplicationContext(),SettingsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

}
