package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseUser currentUser;
    FirebaseAuth userAuth;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userAuth=FirebaseAuth.getInstance();
        currentUser=userAuth.getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser==null)//not loged in
        {
            sendUserTologin();
        }
    }

    public void sendUserTologin()
    {
        Intent intent= new Intent(getApplicationContext(),loginActivity.class);
        startActivity(intent);
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

         }
        if(item.getItemId()==R.id.Log_out)
        {
            userAuth.signOut();
            sendUserTologin();
        }
        if(item.getItemId()==R.id.Find_Friends)
        {

        }
    return true;
    }

}
