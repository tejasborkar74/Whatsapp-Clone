package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsActivity extends AppCompatActivity {

    RecyclerView myContactList;
    DatabaseReference contactRef,usersRef;
    FirebaseAuth userAuth;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        ActionBar actionBar =getSupportActionBar();
        actionBar.setTitle("Contact list");

        myContactList=(RecyclerView)findViewById(R.id.contactList);
        myContactList.setLayoutManager(new LinearLayoutManager(this));

        userAuth=FirebaseAuth.getInstance();
        currentUserId= userAuth.getCurrentUser().getUid();
        contactRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef= FirebaseDatabase.getInstance().getReference().child("User");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                                                .setQuery(contactRef,Contacts.class)
                                                .build();

        FirebaseRecyclerAdapter<Contacts, ContactViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder holder, int position, @NonNull Contacts model)
              {

                //2. retrieve name status and image and set and display
                final String friendIDs = getRef(position).getKey();

                usersRef.child(friendIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("image"))
                        {
                            String userImage = dataSnapshot.child("image").getValue().toString();
                            String profileName = dataSnapshot.child("name").getValue().toString();
                            String profileStatus = dataSnapshot.child("status").getValue().toString();


                            holder.userName.setText(profileName);
                            holder.userStatus.setText(profileStatus);

                            Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                        }
                       else
                        {
                                String profileName = (String) dataSnapshot.child("name").getValue();
                                String profileStatus = (String) dataSnapshot.child("status").getValue();


                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                //1.in this we access user_display_layout

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);

                ContactViewHolder viewHolder = new ContactViewHolder(view);
                return viewHolder;

            }
        };

        myContactList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class ContactViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.users_profile_name);
            userStatus=itemView.findViewById(R.id.users_profile_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);



        }
    }

}
