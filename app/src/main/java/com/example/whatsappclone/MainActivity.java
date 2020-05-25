package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    Button chatButton,groupChatButton,contactButton;



    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userAuth=FirebaseAuth.getInstance();
        currentUser=userAuth.getCurrentUser();
        rootRef= FirebaseDatabase.getInstance().getReference();
        chatButton=(Button)findViewById(R.id.chatButton);
        groupChatButton=(Button)findViewById(R.id.groupChatImage);
        contactButton=(Button)findViewById(R.id.contactButton);

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToChat();
            }
        });


    }

    private void sendUserToChat()
    {
        Intent chatIntent= new Intent(getApplicationContext(),ChatsMainActivity.class);
        startActivity(chatIntent);
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
        if(item.getItemId()==R.id.Create_Group)
        {
            RequestNewGroup();
        }
    return true;
    }

    private void RequestNewGroup()
    {
        AlertDialog.Builder alertDialog= new AlertDialog.Builder(this);
        alertDialog.setTitle("Enter Group Name:");

        final EditText groupName=new EditText(this);
        groupName.setHint("Group Name");

        alertDialog.setView(groupName);
        alertDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String setGroupName=groupName.getText().toString();
                if(TextUtils.isEmpty(setGroupName))
                {
                    Toast.makeText(MainActivity.this, "Please enter group name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //store inside the fire base data base
                    CreateNewGroup(setGroupName);

                }
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertDialog.show();

    }

    private void CreateNewGroup(final String setGroupName)
    {
        //Here Groups key is created as well!!
        //just created new group in database
        rootRef.child("Groups").child(setGroupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, setGroupName+" is created successfully", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            String error=task.getException().toString();
                            Toast.makeText(MainActivity.this, "ERROR: " +error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void sendUserToSettings()
    {
        Intent i= new Intent(getApplicationContext(),SettingsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

}
