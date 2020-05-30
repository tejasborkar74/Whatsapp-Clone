package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

//Here we have all users in contact list...but the difference is that we can chat with them

public class ChatsMainActivity extends AppCompatActivity {
    DatabaseReference rootRef;//for menu
    FirebaseAuth userAuth;//for menu

    DatabaseReference chatsRef,usersRef;

    RecyclerView chatsList;
    String currentUserID;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_main);

        ActionBar actionBar= getSupportActionBar();
        actionBar.setTitle("Chats");

        rootRef= FirebaseDatabase.getInstance().getReference();//for menu
        userAuth=FirebaseAuth.getInstance();//for menu

        chatsList=(RecyclerView)findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(this));

        currentUserID=userAuth.getCurrentUser().getUid();

        chatsRef=FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef=FirebaseDatabase.getInstance().getReference().child("User");

    }


    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model)
                    {
                        final String friendsIDs= getRef(position).getKey();
                        final String[] friendImage = {"default_image"};

                        usersRef.child(friendsIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.exists())
                                {
                                    if(dataSnapshot.hasChild("image"))
                                    {
                                         friendImage[0] =dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(friendImage[0]).into(holder.profileImage);
                                    }

                                    final String friendName=dataSnapshot.child("name").getValue().toString();
                                    String friendStatus=dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(friendName);
                                    holder.userStatus.setText("Last Seen: " + "\n" + "Date " +"Time"  );

                                    //now on click on items

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            Intent privateChatTntent= new Intent(getApplicationContext(),PrivateChattingActivity.class);

                                            privateChatTntent.putExtra("friend_ID",friendsIDs);
                                            privateChatTntent.putExtra("friend_Name",friendName);
                                            privateChatTntent.putExtra("friend_image", friendImage[0]);

                                            startActivity(privateChatTntent);
                                        }
                                    });
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        //Access layout

                        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);

                        return new ChatsViewHolder(view);

                    }
                };

        chatsList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userStatus,userName;

        public ChatsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            profileImage=itemView.findViewById(R.id.users_profile_image);
            userName=itemView.findViewById(R.id.users_profile_name);
            userStatus=itemView.findViewById(R.id.users_profile_status);

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
                    Toast.makeText(ChatsMainActivity.this, "Please enter group name", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ChatsMainActivity.this, setGroupName+" is created successfully", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            String error=task.getException().toString();
                            Toast.makeText(ChatsMainActivity.this, "ERROR: " +error, Toast.LENGTH_SHORT).show();
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
