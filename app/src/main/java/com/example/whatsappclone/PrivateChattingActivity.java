package com.example.whatsappclone;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrivateChattingActivity extends AppCompatActivity
{
    String messageReceiverID,messageReceiverName,messageReceiverImage;

    TextView userName,userLastSeen;
    CircleImageView userImage;

    Toolbar chatToolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chatting);

        messageReceiverID=getIntent().getExtras().get("friend_ID").toString();
        messageReceiverName=getIntent().getExtras().get("friend_Name").toString();
        messageReceiverImage=getIntent().getExtras().get("friend_image").toString();




        InitialiseController();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);



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

    }
}
