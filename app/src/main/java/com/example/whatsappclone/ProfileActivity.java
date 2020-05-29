package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{
    String receiverUserID,current_state,senderUserID;
    CircleImageView userProfileImage;
    TextView  userProfileName, userProfileStatus;
    Button sendChatRequestButton,cancelChatRequestButton;
    DatabaseReference userRef,chatRequestRef,contactsRef;
    FirebaseAuth userAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



        receiverUserID=getIntent().getExtras().get("visit_user_id").toString();

        userProfileImage=(CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName=(TextView)findViewById(R.id.visit_user_name);
        userProfileStatus=(TextView)findViewById(R.id.visit_user_status);
        sendChatRequestButton=(Button)findViewById(R.id.send_chat_request);
        cancelChatRequestButton=(Button)findViewById(R.id.cancel_chat_request);
        current_state="new";

        userRef= FirebaseDatabase.getInstance().getReference().child("User");
        chatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        //for contacts that show all accepted friends
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");


        userAuth=FirebaseAuth.getInstance();
        senderUserID=userAuth.getCurrentUser().getUid();


        retrieveUserInfo();


    }

    private void retrieveUserInfo()
    {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if((dataSnapshot.exists()) && dataSnapshot.hasChild("image"))
                {
                    String userImage=dataSnapshot.child("image").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();
                    String userName=dataSnapshot.child("name").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();

                }
                else
                {
                    String userStatus=dataSnapshot.child("status").getValue().toString();
                    String userName=dataSnapshot.child("name").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequests()
    {

        //2. retrive info when comes from back

        chatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild(receiverUserID))
                        {
                            String request_type=dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if(request_type.equals("sent"))
                            {
                                current_state="request_sent";
                                sendChatRequestButton.setText("Cancel Chat Request");
                            }
                            else if(request_type.equals("received"))
                            {
                                current_state="request_received";
                                sendChatRequestButton.setText("Accept Chat Request");
                                cancelChatRequestButton.setVisibility(View.VISIBLE);
                                cancelChatRequestButton.setEnabled(true);

                                cancelChatRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            contactsRef.child(senderUserID)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            if(dataSnapshot.hasChild(receiverUserID))
                                            {
                                                current_state="friends";
                                                sendChatRequestButton.setText("Remove from Contact");
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //1.
        if(!senderUserID.equals(receiverUserID))
        {
            sendChatRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    sendChatRequestButton.setEnabled(false);

                    if(current_state.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if(current_state.equals("request_sent"))
                    {
                        cancelChatRequest();
                    }
                    if(current_state.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if(current_state.equals("friends"))
                    {
                        removeTheContact();
                    }

                }
            });
        }
        else
        {
            sendChatRequestButton.setVisibility(View.INVISIBLE);
        }
    }


    private void sendChatRequest()
    {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                sendChatRequestButton.setEnabled(true);
                                                current_state="request_sent";

                                                sendChatRequestButton.setText("Cancel Chat Request");

                                            }

                                        }
                                    });
                        }

                    }
                });

    }

    private void cancelChatRequest()
    {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                sendChatRequestButton.setEnabled(true);
                                                current_state="new";
                                                sendChatRequestButton.setText("Send Chat Request");

                                                cancelChatRequestButton.setVisibility(View.INVISIBLE);
                                                cancelChatRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest()
    {
        contactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            contactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                //now contacts id saved the we have to remove records from Chat Requests
                                                chatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    chatRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    sendChatRequestButton.setEnabled(true);
                                                                                    current_state="friends";
                                                                                    sendChatRequestButton.setText("Remove from Contact");

                                                                                    cancelChatRequestButton.setVisibility(View.INVISIBLE);
                                                                                    cancelChatRequestButton.setEnabled(false);

                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });

                        }
                    }
                });
    }


    private void removeTheContact()
    {
        contactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            contactsRef .child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                sendChatRequestButton.setEnabled(true);
                                                current_state="new";
                                                sendChatRequestButton.setText("Send Chat Request");

                                                cancelChatRequestButton.setVisibility(View.INVISIBLE);
                                                cancelChatRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }



}
