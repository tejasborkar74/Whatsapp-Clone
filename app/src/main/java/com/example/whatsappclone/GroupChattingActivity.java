package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChattingActivity extends AppCompatActivity {

    DatabaseReference rootRef;//for menu
    FirebaseAuth userAuth;//for menu

    EditText userMessage;
    ImageView sendIcon;
    ScrollView scrollView;
    TextView textChatDisplay;

    String GroupName;
    String CurrentUserID,CurrentUserName;
    DatabaseReference userRef,groupNameRef,groupMessageKeyRef;
    String currentDate,currentTime;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chatting);

        GroupName=getIntent().getExtras().get("GroupName").toString();

        ActionBar actionBar= getSupportActionBar();
        actionBar.setTitle(GroupName);

        //Initialize
        rootRef= FirebaseDatabase.getInstance().getReference();//for menu
        userAuth=FirebaseAuth.getInstance();//for menu
        userMessage=(EditText)findViewById(R.id.editText);
        textChatDisplay=(TextView)findViewById(R.id.chatTextDisplay);
        sendIcon=(ImageView)findViewById(R.id.sendIcon);
        scrollView=(ScrollView)findViewById(R.id.scrollView);
        CurrentUserID=userAuth.getCurrentUser().getUid();


        userRef=FirebaseDatabase.getInstance().getReference().child("User");
        groupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(GroupName);

        GetUserInfo();

        sendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMessageToDataBase();
                //after the message save/send null edittext
                userMessage.setText("");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        //DISPLAY MESSAGES

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void GetUserInfo()
    {
        userRef.child(CurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    CurrentUserName=dataSnapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveMessageToDataBase()
    {
        String message= userMessage.getText().toString();
        String messageKey=groupNameRef.push().getKey();//create a key

        if(!TextUtils.isEmpty(message))
        {
            // find date and time of message

            Calendar calForDate =Calendar.getInstance();
            SimpleDateFormat currentDateFormat= new SimpleDateFormat("MMM dd YYYY");
            currentDate=  currentDateFormat.format(calForDate.getTime());

            Calendar calForTime =Calendar.getInstance();
            SimpleDateFormat currentTimeFormat= new SimpleDateFormat("hh:mm a");
            currentTime=  currentTimeFormat.format(calForTime.getTime());

            //now save;
            HashMap<String, Object> groupMessageKey=new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef=groupNameRef.child(messageKey);

            HashMap<String,Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", CurrentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);

            groupMessageKeyRef.updateChildren(messageInfoMap);


        }
    }

    private void DisplayMessages(DataSnapshot dataSnapshot)
    {
        Iterator iterator =dataSnapshot.getChildren().iterator();
        while(iterator.hasNext())
        {
            //fetch data
            String chatDate=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatName=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String) ((DataSnapshot)iterator.next()).getValue();

            SpannableString ss1=  new SpannableString(chatName);
            int e=chatName.length();
            ss1.setSpan(new RelativeSizeSpan(0.8f), 0, e, 0); // set size
            ss1.setSpan(new ForegroundColorSpan(Color.GREEN), 0, e, 0);
 
            textChatDisplay.append(ss1);

            textChatDisplay.append(" \n" + chatMessage + "\n\n\n" );

//            String dateTime= chatDate + "      " + chatTime;
//
//            SpannableString date=  new SpannableString(dateTime);
//            e=dateTime.length();
//            date.setSpan(new RelativeSizeSpan(0.7f), 0, e, 0); // set size
//            date.setSpan(new ForegroundColorSpan(Color.RED), 0, e, 0);
//
//
//            textChatDisplay.append(date + "\n\n\n");

        }

        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }


    //===========================================================MENU=========================================================
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
            sendUserToFindFriend();
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
                    Toast.makeText(GroupChattingActivity.this, "Please enter group name", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(GroupChattingActivity.this, setGroupName+" is created successfully", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            String error=task.getException().toString();
                            Toast.makeText(GroupChattingActivity.this, "ERROR: " +error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void sendUserToSettings()
    {
        Intent i= new Intent(getApplicationContext(),SettingsActivity.class);

        startActivity(i);

    }
    public void sendUserToLogin()
    {
        Intent intent= new Intent(getApplicationContext(),loginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void sendUserToFindFriend()
    {
        Intent ent= new Intent(getApplicationContext(),FindFriendsActivity.class);
        startActivity(ent);

    }

    //================================================================MENU=======================================================================
}
