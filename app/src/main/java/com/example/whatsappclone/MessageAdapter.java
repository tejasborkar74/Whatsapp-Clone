package com.example.whatsappclone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    List<Messages> userMessageslist;
    FirebaseAuth userAuth;
    DatabaseReference userRef;


    public MessageAdapter (List<Messages> userMessageslist)
    {
        this.userMessageslist = userMessageslist;
    }


    public class  MessageViewHolder  extends RecyclerView.ViewHolder
    {
        public TextView senderMessagestext,receiverMessageText;
        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessagestext=(TextView)itemView.findViewById(R.id.sender_message_text);
            receiverMessageText=(TextView)itemView.findViewById(R.id.receiver_message_text);

        }
    }



    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {

        //connect custom_message_layout
       View view = LayoutInflater.from(viewGroup.getContext())
               .inflate(R.layout.custom_message_layout,viewGroup,false);

       userAuth=FirebaseAuth.getInstance();

       return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position)
    {
        //receive and display the messages

        String messageSenderID=userAuth.getCurrentUser().getUid();
        Messages messages = userMessageslist.get(position);

        String fromUserID=messages.getFrom();//receiver ID
        String fromMessageType=messages.getType();

        userRef= FirebaseDatabase.getInstance().getReference().child("User").child(fromUserID);

      if(fromMessageType.equals("text"))
      {
          holder.receiverMessageText.setVisibility(View.INVISIBLE);
          holder.senderMessagestext.setVisibility(View.INVISIBLE);

          if(fromUserID.equals(messageSenderID))
          {
              holder.senderMessagestext.setVisibility(View.VISIBLE);
              holder.senderMessagestext.setBackgroundResource(R.drawable.sender_message_layout);
              holder.senderMessagestext.setText(messages.getMessage());
          }
          else
          {
              holder.senderMessagestext.setVisibility(View.INVISIBLE);
              holder.receiverMessageText.setVisibility(View.VISIBLE);

              holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
              holder.receiverMessageText.setText( messages.getMessage());



          }
      }



    }



    @Override
    public int getItemCount()
    {
           return userMessageslist.size();
    }




}
