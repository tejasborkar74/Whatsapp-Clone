package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrivateChattingActivity extends AppCompatActivity
{

    String messageReceiverID,messageReceiverName,messageReceiverImage,messageSenderID;

    TextView userName,userLastSeen;
    CircleImageView userImage;

    Toolbar chatToolBar;

    ImageView sendMessageButton;
    EditText MessageInputText;
    FirebaseAuth userAuth;
    DatabaseReference rootRef;

    List<Messages> messagesList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
    RecyclerView userMessagesList;
    


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chatting);

        if(android.os.Build.VERSION.SDK_INT>=21)
        {
            Window window =this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }

        userAuth=FirebaseAuth.getInstance();
        messageSenderID=userAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();

        messageReceiverID=getIntent().getExtras().get("friend_ID").toString();
        messageReceiverName=getIntent().getExtras().get("friend_Name").toString();
        messageReceiverImage=getIntent().getExtras().get("friend_image").toString();




        InitialiseController();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendMessage();

            }
        });

    }
    private void InitialiseController()
    {
        //setting up custom action bar

        chatToolBar = (Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);

        ActionBar actionBar =getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle(null);

        //connecting to view

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userImage=(CircleImageView)findViewById(R.id.custom_profile_image);
        userName=(TextView)findViewById(R.id.custom_profile_name);
        userLastSeen=(TextView)findViewById(R.id.custom_user_seen);

        sendMessageButton=(ImageView)findViewById(R.id.send_message_btn);
        MessageInputText=(EditText)findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList=(RecyclerView)findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


    }




    @Override
    protected void onStart()
    {
        super.onStart();

        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages=dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

    public void SendMessage()
    {
        String messageText= MessageInputText.getText().toString();

        if(!TextUtils.isEmpty(messageText))
        {
            String messageSenderRef="Messages/"+ messageSenderID +"/" +messageReceiverID;
            String messageReceiverRef="Messages/"+ messageReceiverID +"/" +messageSenderID;

            //this create unique key for message
            DatabaseReference userMessageKeyRef=rootRef.child("Messages").child(messageSenderID)
                    .child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);

            //This put the message Body in Database
            Map messageBodyDetails= new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);


            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        //Toast.makeText(PrivateChattingActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(PrivateChattingActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                    }

                    MessageInputText.setText(null);

                }
            });


        }

    }


}
