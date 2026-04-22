package com.example.letstalk;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String MessageRecieverID, ReeiverName, profileeImage, messageSenderID;
    private TextView username, userlastsen;
    private CircleImageView profile;
    private ImageButton sendMessage, Attached;
    private EditText messgeInput;
    private FirebaseAuth mAuth;
    private DatabaseReference roofRef;
    private ProgressDialog loading;
    private String saveCurrentTime, saveCjurrentDate;
    private Toolbar chatToolbar;
    private  final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessages;

    private StorageTask uploadTask;
    private Uri fileUri;

    private String checker="", myUri="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);


        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        roofRef = FirebaseDatabase.getInstance().getReference();

        MessageRecieverID = getIntent().getExtras().get("visit_user_id").toString();
        ReeiverName = getIntent().getExtras().get("visit_name_id").toString();
        profileeImage = getIntent().getExtras().get("visit_image").toString();

        Toast.makeText(this, MessageRecieverID, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, ReeiverName, Toast.LENGTH_SHORT).show();

        Inintialize();
        DisplayLastSeen();

        username.setText(ReeiverName);
        Picasso.get().load(profileeImage).placeholder(R.drawable.baseline_person_24).into(profile);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        Attached.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF files",
                                "Ms Word Docs",
                                "Music"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0){

                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select image"), 438);
                        }
                        if(i == 1){

                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("appliaction/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF"), 438);
                        }
                        if(i == 2){

                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select Docx"), 438);

                        }
                        if(i == 3){

                            checker = "Audio";

                        }
                    }
                });
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode==438 && requestCode==RESULT_OK && data!=null && data.getData()!= null){
            fileUri = data.getData();

            if (!checker.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                String messageSenderRef = "Messages/" + messageSenderID + "/" + MessageRecieverID;
                String messageReceiveRef = "Messages/" + MessageRecieverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = roofRef.child("Messages")
                        .child(messageSenderRef).child(messageReceiveRef).push();


                final String messagePushID = userMessageKeyRef.getKey();

                StorageReference filepath = storageReference.child(messagePushID +"."+ checker);
                filepath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){

                            filepath.getDownloadUrl().addOnSuccessListener(uri -> {

                                Map messageTextBody = new HashMap();
                                messageTextBody.put("message", uri.toString());
                                messageTextBody.put("name", fileUri.getLastPathSegment());
                                messageTextBody.put("type", checker);
                                messageTextBody.put("from", messageSenderID);
                                messageTextBody.put("to", MessageRecieverID);
                                messageTextBody.put("messageID", messagePushID);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCjurrentDate);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                messageBodyDetails.put(messageReceiveRef + "/" + messagePushID, messageTextBody);

                                roofRef.updateChildren(messageBodyDetails);

                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double p = (100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        loading.setMessage((int) p +"% Uploading");
                    }
                });


              //For Images
            }
            else if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("image Files");

                String messageSenderRef = "Messages/" + messageSenderID + "/" + MessageRecieverID;
                String messageReceiveRef = "Messages/" + MessageRecieverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = roofRef.child("Messages")
                        .child(messageSenderRef).child(messageReceiveRef).push();


                final String messagePushID = userMessageKeyRef.getKey();

                StorageReference filepath = storageReference.child(messagePushID +"."+ "jpg");

                uploadTask = filepath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            throw  task.getException();
                        }

                        return filepath.getDownloadUrl() ;
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>(){
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            myUri = downloadUri.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUri);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", MessageRecieverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCjurrentDate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReceiveRef + "/" + messagePushID, messageTextBody);

                            roofRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(ChatActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    messgeInput.setText("");
                                }
                            });


                        }
                    }
                });


            }else{
                Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void Inintialize() {
        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        username = findViewById(R.id.useRname);
        userlastsen = findViewById(R.id.Lastseen);
        profile = findViewById(R.id.proImage);

        sendMessage = findViewById(R.id.sendMessage);
        Attached = findViewById(R.id.sendAttach);
        messgeInput = findViewById(R.id.msgInput);


        messageAdapter = new MessageAdapter(messagesList);
        userMessages = findViewById(R.id.private_msg_List);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessages.setLayoutManager(linearLayoutManager);
        userMessages.setAdapter(messageAdapter);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat curentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCjurrentDate = curentDate.format(calendar.getTime());

        SimpleDateFormat curentTime = new SimpleDateFormat("hh:mm");
        saveCurrentTime = curentTime.format(calendar.getTime());

        loading  = new ProgressDialog(this);
        
    }

    private void DisplayLastSeen(){
        roofRef.child("LetsTalk Users").child(MessageRecieverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("userState").hasChild("state")){

                            String state = snapshot.child("userState").child("state").getValue().toString();
                            String date = snapshot.child("userState").child("date").getValue().toString();
                            String time = snapshot.child("userState").child("time").getValue().toString();
                            if(state.equals("online")){
                                userlastsen.setText("online");
                            } else if (state.equals("offline")) {
                                userlastsen.setText("Last Seen:"+date+" "+time);
                            }
                        }
                        else{
                            userlastsen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        roofRef.child("Messages").child(messageSenderID).child(MessageRecieverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Messages messages = snapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessages.smoothScrollToPosition(userMessages.getAdapter().getItemCount());
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

    private void SendMessage() {
        String messageText = messgeInput.getText().toString();

        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "write your message"  , Toast.LENGTH_SHORT).show();
        }else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + MessageRecieverID;
            String messageReceiveRef = "Messages/" + MessageRecieverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = roofRef.child("Messages")
                    .child(messageSenderRef).child(messageReceiveRef).push();


            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", MessageRecieverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCjurrentDate);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiveRef + "/" + messagePushID, messageTextBody);

            roofRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    messgeInput.setText("");
                }
            });
        }
    }
}