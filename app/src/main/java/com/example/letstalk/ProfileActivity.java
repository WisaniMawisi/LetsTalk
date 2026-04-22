package com.example.letstalk;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, currentState, sendUserID;

    private CircleImageView userProfile;
    private TextView username, userAbout;
    private Button sendRequest, AcceptRequest, DeclineRequest;
    private DatabaseReference userRef, chatRequest, contRef, Notificattion;
    private FirebaseAuth mAuth;

    private LinearLayout links;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("LetsTalk Users");
        chatRequest = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        Notificattion = FirebaseDatabase.getInstance().getReference().child("Notification");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        sendUserID = mAuth.getCurrentUser().getUid();


        userProfile = findViewById(R.id.visit_prof);
        username = findViewById(R.id.visit_username);
        userAbout = findViewById(R.id.visit_About);
        sendRequest = findViewById(R.id.sendRequst);
        DeclineRequest = findViewById(R.id.Decline);
        AcceptRequest = findViewById(R.id.Accept);
        links = findViewById(R.id.requrequ);


        currentState = "new";

        RetriveUserInfo();
    }

    private void RetriveUserInfo() {

        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("image"))){
                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userabout = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.baseline_person_24).into(userProfile);
                    username.setText(userName);
                    userAbout.setText(userabout);

                    ManageChatRequst();
                }else{
                    String userName = snapshot.child("name").getValue().toString();
                    String userabout = snapshot.child("status").getValue().toString();

                    username.setText(userName);
                    userAbout.setText(userabout);
                    ManageChatRequst();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequst() {

        chatRequest.child(sendUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild(receiverUserID)){
                            String request_type = snapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent")){
                                currentState = "request_sent";
                                sendRequest.setText("Cancel Request");

                            }else if(request_type.equals("received")){

                                sendRequest.setVisibility(View.VISIBLE);
                                links.setVisibility(View.VISIBLE);

                                currentState = "request_recieved";

                                DeclineRequest.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }
                        else {
                           contRef.child(sendUserID)
                                   .addValueEventListener(new ValueEventListener() {
                                       @Override
                                       public void onDataChange(@NonNull DataSnapshot snapshot) {
                                           if(snapshot.hasChild(receiverUserID)){
                                               currentState = "friends";
                                               AcceptRequest.setText("Remove");
                                           }
                                       }

                                       @Override
                                       public void onCancelled(@NonNull DatabaseError error) {

                                       }
                                   });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if(sendUserID.equals(receiverUserID)){

        }else{
            sendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendRequest.setEnabled(false);

                    if(currentState.equals("new")){
                        sendChatRequst();
                    }
                    if (currentState.equals("request_sent")){
                        cancelChatRequest();
                    }
                    if (currentState.equals("request_received")){
                       AcceptRequestRequst();
                    }
                    if (currentState.equals("friends")){
                        RemoveSpecificContact();
                    }
                }
            });
        }

    }

    private void RemoveSpecificContact() {
        contRef.child(sendUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contRef.child(receiverUserID).child(sendUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendRequest.setEnabled(true);
                                                currentState = "new";
                                                sendRequest.setText("Send request");

                                                links.setVisibility(View.INVISIBLE);
                                                links.setEnabled(false);
                                                sendRequest.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptRequestRequst() {

        contRef.child(sendUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            // check
                            contRef.child(receiverUserID).child(sendUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                chatRequest.child(sendUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    chatRequest.child(receiverUserID).child(sendUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                   AcceptRequest.setEnabled(true);
                                                                                   currentState = "frends";
                                                                                   AcceptRequest.setText("Remove");


                                                                                   DeclineRequest.setVisibility(View.INVISIBLE);
                                                                                   DeclineRequest.setEnabled(false);
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

    private void cancelChatRequest() {
        chatRequest.child(sendUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatRequest.child(receiverUserID).child(sendUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendRequest.setEnabled(true);
                                                currentState = "new";
                                                sendRequest.setText("Send zrequest");

                                                links.setVisibility(View.INVISIBLE);
                                                links.setEnabled(false);
                                                sendRequest.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequst() {

        chatRequest.child(sendUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            chatRequest.child(receiverUserID).child(sendUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                HashMap<String, String> chatNotification = new HashMap<>();
                                                chatNotification.put("from", sendUserID);
                                                chatNotification.put("from", "request");

                                                Notificattion.child(receiverUserID).push()
                                                                .setValue(chatNotification)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()) {
                                                                                    sendRequest.setEnabled(true);
                                                                                    currentState = "request_sent";
                                                                                    sendRequest.setText("Cancel Request");
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