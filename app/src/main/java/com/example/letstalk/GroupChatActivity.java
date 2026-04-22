package com.example.letstalk;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {


    private Toolbar mToolBar;
    private ImageButton sendMessage;
    private EditText userMessage;
    private ScrollView mScrollview;
    private TextView displayText;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, groupNameRef, groupMsgKeyRef;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_chat);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("LetsTalk Users");



        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(this, currentGroupName, Toast.LENGTH_SHORT).show();

        groupNameRef = FirebaseDatabase.getInstance().getReference().child(currentGroupName);

        Initializefilds();

        GetUserInfo();
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMassageInfoToDatabase();
                userMessage.setText("");
                mScrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
               if(snapshot.exists()){
                   DisplayMessages(snapshot);
               }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void SaveMassageInfoToDatabase() {
        String Message = userMessage.getText().toString();
        String messageKey = groupNameRef.push().getKey();



        if(TextUtils.isEmpty(Message)){
            Toast.makeText(this, "please write message..", Toast.LENGTH_SHORT).show();
        }


        else{
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());


            Calendar calFortime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calFortime.getTime());


            HashMap<String, Object>groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMsgKeyRef = groupNameRef.child(messageKey);


            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", Message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("tme", currentTime);
            groupMsgKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void GetUserInfo() {
        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserName = snapshot.child("name").getKey().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void Initializefilds() {
        mToolBar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(currentGroupName);


        sendMessage = findViewById(R.id.send_message);
        mScrollview = findViewById(R.id.myScroll);
        displayText = findViewById(R.id.input_group_message);
    }



    private void DisplayMessages(DataSnapshot snapshot) {
        Iterator iterator = snapshot.getChildren().iterator();

        while(iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayText.append(chatName+":\n" +chatMessage +"\n" +chatTime + "     "+ chatDate +"\n\n\n");

            mScrollview.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}