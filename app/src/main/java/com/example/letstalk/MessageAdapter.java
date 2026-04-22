package com.example.letstalk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public MessageAdapter(List<Messages> userMessageList){
        this.userMessageList  = userMessageList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView sendrMessegeText, receiverMsgTxt;
        public CircleImageView rcvProImg;
        public ImageView sendImg, ReceiveImg;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            sendrMessegeText = itemView.findViewById(R.id.Sender_msg);
            receiverMsgTxt = itemView.findViewById(R.id.receiver_msg);
            rcvProImg = itemView.findViewById(R.id.msgProImg);
            sendImg = itemView.findViewById(R.id.imgSender);
            ReceiveImg =itemView.findViewById(R.id.imgReceiver);
        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_message_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return  new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {


        //Validations
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        userRef = FirebaseDatabase.getInstance().getReference().child("LetsTalk Users").child(fromUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("image")){
                    String recPromg = snapshot.child("image").getValue().toString();
                    Picasso.get().load(recPromg).placeholder(R.drawable.baseline_person_24).into(holder.rcvProImg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });






        //Text View
        holder.sendrMessegeText.setVisibility(View.GONE);
        holder.receiverMsgTxt.setVisibility(View.GONE);
        holder.rcvProImg.setVisibility(View.GONE);

        //Image View
        holder.sendImg.setVisibility(View.INVISIBLE);
        holder.ReceiveImg.setVisibility(View.INVISIBLE);

        //Messages View For Both Sides
        if (fromMessageType.equals("text")){



            if (fromUserID.equals(messageSenderId)){

                holder.sendrMessegeText.setVisibility(View.VISIBLE);

                holder.sendrMessegeText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.sendrMessegeText.setTextColor(Color.BLACK);
                holder.sendrMessegeText.setText(messages.getMessage()+"\n \n"+ messages.getTime()+"-"+ messages.getDate());
            }
            else{

                holder.rcvProImg.setVisibility(View.VISIBLE);
                holder.receiverMsgTxt.setVisibility(View.VISIBLE);

                holder.receiverMsgTxt.setBackgroundResource(R.drawable.sender_message_layout);
                holder.receiverMsgTxt.setTextColor(Color.BLACK);
                holder.receiverMsgTxt.setText(messages.getMessage()+"\n \n"+ messages.getTime()+"-"+ messages.getDate());

            }
        }
        //Image View
        else if (fromMessageType.equals("image")) {
            if (fromUserID.equals(messageSenderId)){
                holder.sendImg.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.sendImg);
            }else{
                holder.ReceiveImg.setVisibility(View.VISIBLE);
                holder.rcvProImg.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.ReceiveImg);


            }
        }
        //pdf View
        else if (fromMessageType.equals("pdf")||(fromMessageType.equals("docx"))){
            if(fromUserID.equals(messageSenderId)){

                holder.sendImg.setVisibility(View.VISIBLE);

                holder.sendImg.setBackgroundResource(R.drawable.baseline_document_scanner_24);


            }
            else {

                holder.ReceiveImg.setVisibility(View.VISIBLE);
                holder.rcvProImg.setVisibility(View.VISIBLE);
            }
            holder.sendImg.setBackgroundResource(R.drawable.baseline_document_scanner_24);
        }


        //Send Messages

        if (fromUserID.equals(messageSenderId)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;

                    if (userMessageList.get(position).getType().equals("pdf") ||userMessageList.get(position).getType().equals("pdf")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "Download and View",
                                "Delete for everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message!");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){
                                    deleteSentMessages(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 1){

                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                            holder.itemView.getContext().startActivity(intent);


                                }
                                else if (i == 2){
                                    deleteForEveryone(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 3){

                                }
                            }
                        });
                        builder.show();
                    }


                    else if (userMessageList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "Delete for everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message!");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){
                                    deleteSentMessages(position, holder);
                                }
                                else if (i == 1){
                                    deleteForEveryone(position, holder);
                                }
                                else if (i == 2){

                                }
                            }
                        });
                        builder.show();
                    }



                    else if (userMessageList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "Delete for everyone",
                                "View Image",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message!");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){
                                    deleteSentMessages(position, holder);
                                }
                                else if (i == 1){
                                    deleteForEveryone(position,holder);
                                }
                                else if (i == 2){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("uri",userMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 3){

                                }
                            }
                        });
                        builder.show();
                    }
                }
            });

        }

        //Receive Messages

        else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;

                    if (userMessageList.get(position).getType().equals("pdf") ||userMessageList.get(position).getType().equals("pdf")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "Download and View",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message!");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){
                                    deleteReceivetMessages(position, holder);
                                }
                                else if (i == 1){

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);


                                }
                                else if (i == 2){

                                }

                            }
                        });
                        builder.show();
                    }
                    else if (userMessageList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message!");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){
                                    deleteReceivetMessages(position, holder);
                                }
                                else if (i == 1){

                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessageList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "View Image",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message!");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){
                                    deleteReceivetMessages(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 1){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("uri",userMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 2){

                                }

                            }
                        });
                        builder.show();
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }


    private void deleteSentMessages(final int mawisi, final MessageViewHolder wisani){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(mawisi).getFrom())
                .child(userMessageList.get(mawisi).getTo())
                .child(userMessageList.get(mawisi).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(wisani.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(wisani.itemView.getContext(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void deleteReceivetMessages(final int mawisi, final MessageViewHolder wisani){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(mawisi).getTo())
                .child(userMessageList.get(mawisi).getFrom())
                .child(userMessageList.get(mawisi).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(wisani.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(wisani.itemView.getContext(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void deleteForEveryone(final int mawisi, final MessageViewHolder wisani){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(mawisi).getTo())
                .child(userMessageList.get(mawisi).getFrom())
                .child(userMessageList.get(mawisi).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            rootRef.child("Messages")
                                    .child(userMessageList.get(mawisi).getFrom())
                                    .child(userMessageList.get(mawisi).getTo())
                                    .child(userMessageList.get(mawisi).getMessageID())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(wisani.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        }else{
                            Toast.makeText(wisani.itemView.getContext(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



}
