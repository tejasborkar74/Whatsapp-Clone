package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class FriendRequestActivity extends AppCompatActivity {

    DatabaseReference rootRef;//for menu
    FirebaseAuth userAuth;//for menu

    DatabaseReference chatRequestRef,usersRef,contactRef;
    String currentUserID;
    RecyclerView myRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        ActionBar actionBar =getSupportActionBar();
        actionBar.setTitle("Friend Requests");

        myRecyclerView=(RecyclerView)findViewById(R.id.recyclerView);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        rootRef= FirebaseDatabase.getInstance().getReference();//for menu
        userAuth=FirebaseAuth.getInstance();//for menu

        chatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        usersRef=FirebaseDatabase.getInstance().getReference().child("User");
        contactRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        currentUserID=userAuth.getCurrentUser().getUid();



    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestRef.child(currentUserID),Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts,RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model)
                    {
                        holder.itemView.findViewById(R.id.accept_request_button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.decline_request_button).setVisibility(View.VISIBLE);

                        //key of users listed under current user
                        final String list_userID=getRef(position).getKey();

                        //sent or received
                        DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.exists())
                                {
                                    String type=dataSnapshot.getValue().toString();

                                    if(type.equals("received"))
                                    {
                                        usersRef.child(list_userID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {
                                                if(dataSnapshot.hasChild("image"))
                                                {
                                                  String requestProfileImage=dataSnapshot.child("image").getValue().toString();

                                                  Picasso.get().load(requestProfileImage).into(holder.profileImage);

                                                }

                                                    String requestuserName=dataSnapshot.child("name").getValue().toString();
                                                    String requestuserStatus=dataSnapshot.child("status").getValue().toString();

                                                    holder.userName.setText(requestuserName);
                                                    holder.userStatus.setText("Whats to connect with you");



                                                //if click Accept

                                                holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view)
                                                    {
                                                        contactRef.child(currentUserID).child(list_userID).child("Contacts").setValue("saved")
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        if(task.isSuccessful())
                                                                        {
                                                                            contactRef.child(list_userID).child(currentUserID).child("Contacts").setValue("saved")
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                        {
                                                                                            if(task.isSuccessful())
                                                                                            {
                                                                                                //Till now contacts has been saved ...but we have to remove this request to display

                                                                                                chatRequestRef.child(currentUserID).child(list_userID).removeValue()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                                            {
                                                                                                                if(task.isSuccessful())
                                                                                                                {
                                                                                                                    chatRequestRef.child(list_userID).child(currentUserID).removeValue()
                                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                @Override
                                                                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                                                                {
                                                                                                                                    if(task.isSuccessful())
                                                                                                                                    {
                                                                                                                                        Toast.makeText(FriendRequestActivity.this, "Chat Request Accepted... New Contact added", Toast.LENGTH_SHORT).show();

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


                                                                    }
                                                                });


                                                    }
                                                });


                                                //if clicked on Decline button

                                                holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view)
                                                    {
                                                        //remove from chat request

                                                        chatRequestRef.child(currentUserID).child(list_userID).removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        if(task.isSuccessful())
                                                                        {
                                                                            chatRequestRef.child(list_userID).child(currentUserID).removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                        {
                                                                                            if(task.isSuccessful())
                                                                                            {
                                                                                                Toast.makeText(FriendRequestActivity.this, "Chat Request Decline", Toast.LENGTH_SHORT).show();

                                                                                            }

                                                                                        }
                                                                                    });

                                                                        }

                                                                    }
                                                                });


                                                    }
                                                });


                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        //1.
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        RequestViewHolder holder =new RequestViewHolder(view);
                        return holder;

                    }
                };

        myRecyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button acceptButton,cancelButton;
        public RequestViewHolder(@NonNull View itemView)
        {
            super(itemView);


            userName=itemView.findViewById(R.id.users_profile_name);
            userStatus=itemView.findViewById(R.id.users_profile_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            acceptButton=itemView.findViewById(R.id.accept_request_button);
            cancelButton=itemView.findViewById(R.id.decline_request_button);


        }
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
                    Toast.makeText(FriendRequestActivity.this, "Please enter group name", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(FriendRequestActivity.this, setGroupName+" is created successfully", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            String error=task.getException().toString();
                            Toast.makeText(FriendRequestActivity.this, "ERROR: " +error, Toast.LENGTH_SHORT).show();
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
