package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.Edits;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupChatMainActivity extends AppCompatActivity {

    ListView listView ;
    ArrayList<String> arrayList=new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    DatabaseReference groupRef;
    FirebaseAuth userAuth;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_main);

        ActionBar actionBar= getSupportActionBar();
        actionBar.setTitle("Public Group Chats");
        userAuth=FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();

        groupRef= FirebaseDatabase.getInstance().getReference().child("Groups");

        Initialise();

        RetriveAndDisplayGroups();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String groupName= adapterView.getItemAtPosition(i).toString();
                Intent toGroupChatting=new Intent(getApplicationContext(),GroupChattingActivity.class);
                toGroupChatting.putExtra("GroupName",groupName);
                startActivity(toGroupChatting);
            }
        });




    }

    private void RetriveAndDisplayGroups()
    {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //Iterate through whole group in data base
                Set<String> set = new HashSet<>();

                Iterator iterator=dataSnapshot.getChildren().iterator();
                while(iterator.hasNext())
                {
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                arrayList.clear();//clear current list
                arrayList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void Initialise() {
        listView=(ListView) findViewById(R.id.listView);
        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,arrayList);

        listView.setAdapter(arrayAdapter);
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
                    Toast.makeText(GroupChatMainActivity.this, "Please enter group name", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(GroupChatMainActivity.this, setGroupName+" is created successfully", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            String error=task.getException().toString();
                            Toast.makeText(GroupChatMainActivity.this, "ERROR: " +error, Toast.LENGTH_SHORT).show();
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
