package com.example.letstalk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment {

    private  View request;
    private RecyclerView myRequest;

    private DatabaseReference chatRef, userRef, contRef;
    private FirebaseAuth mAuth;
    private  String currentUserID;

    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        request = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        userRef = FirebaseDatabase.getInstance().getReference().child("LetsTalk Users");
        contRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        myRequest = request.findViewById(R.id.requestList);
        myRequest.setLayoutManager(new LinearLayoutManager(getContext()));

        return request;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatRef.child(currentUserID), Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RequestViewHolder requestViewHolder, int i, @NonNull Contacts contacts) {

                       requestViewHolder.itemView.findViewById(R.id.requrequ).setVisibility(View.VISIBLE);

                       final  String listUserId = getRef(i).getKey();
                       DatabaseReference getType = getRef(i).child("request_type").getRef();
                       getType.addValueEventListener(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                               if (snapshot.exists()){
                                   String type = snapshot.getValue().toString();

                                   if (type.equals("received")){

                                       userRef.addValueEventListener(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                                               if (snapshot.hasChild("image")){

                                                   final String requestProfile = snapshot.child("image").getValue().toString();


                                                   Picasso.get().load(requestProfile).placeholder(R.drawable.baseline_person_24).into(requestViewHolder.profile);
                                               }
                                                   final String requestName = snapshot.child("name").getValue().toString();
                                                   final String requestAbout = snapshot.child("status").getValue().toString();

                                                   requestViewHolder.username.setText(requestName);
                                                   requestViewHolder.userabout.setText("Wants to connect with you");



                                               requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View view) {
                                                       CharSequence options[] = new CharSequence[]{
                                                               "Accept",
                                                               "Decline"
                                                       };
                                                       AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                       builder.setTitle(requestName + "Chat Request");

                                                       builder.setItems(options, new DialogInterface.OnClickListener() {
                                                           @Override
                                                           public void onClick(DialogInterface dialogInterface, int i) {


                                                               if (i == 0){
                                                                   contRef.child(currentUserID)
                                                                           .child(listUserId)
                                                                           .child("Contact")
                                                                           .child("Saved")
                                                                           .setValue(true)   // or whatever you're saving
                                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                               @Override
                                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                                   if (task.isSuccessful()) {

                                                                                       contRef.child(listUserId)
                                                                                               .child(currentUserID)
                                                                                               .child("Contact")
                                                                                               .child("Saved")
                                                                                               .setValue(true)   // or whatever you're saving
                                                                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                   @Override
                                                                                                   public void onComplete(@NonNull Task<Void> task) {
                                                                                                       if (task.isSuccessful()) {

                                                                                                           chatRef.child(currentUserID).child(listUserId)
                                                                                                                   .removeValue()
                                                                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                       @Override
                                                                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                                                                           if (task.isSuccessful()){
                                                                                                                               chatRef.child(listUserId).child(currentUserID )
                                                                                                                                       .removeValue()
                                                                                                                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                           @Override
                                                                                                                                           public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                               if (task.isSuccessful()){

                                                                                                                                                   Toast.makeText(getContext(), "Contact Saved", Toast.LENGTH_SHORT).show();
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
                                                               if (i == 1){
                                                                   chatRef.child(currentUserID).child(listUserId)
                                                                           .removeValue()
                                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                               @Override
                                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                                   if (task.isSuccessful()){
                                                                                       chatRef.child(listUserId).child(currentUserID )
                                                                                               .removeValue()
                                                                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                   @Override
                                                                                                   public void onComplete(@NonNull Task<Void> task) {
                                                                                                       if (task.isSuccessful()){

                                                                                                           Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                                                       }
                                                                                                   }
                                                                                               });
                                                                                   }
                                                                               }
                                                                           });
                                                               }



                                                           }
                                                       });
                                                       builder.show();
                                                   }
                                               });
                                           }

                                           @Override
                                           public void onCancelled(@NonNull DatabaseError error) {

                                           }
                                       });
                                   } else if (type.equals("sent")) {
                                       Button requset_sent_btn = requestViewHolder.itemView.findViewById(R.id.Accept);
                                       requset_sent_btn.setText("Sent");

                                       requestViewHolder.itemView.findViewById(R.id.Decline).setVisibility(View.INVISIBLE);

                                       userRef.addValueEventListener(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                                               if (snapshot.hasChild("image")){

                                                   final String requestProfile = snapshot.child("image").getValue().toString();


                                                   Picasso.get().load(requestProfile).placeholder(R.drawable.baseline_person_24).into(requestViewHolder.profile);
                                               }
                                               final String requestName = snapshot.child("name").getValue().toString();
                                               final String requestAbout = snapshot.child("status").getValue().toString();

                                               requestViewHolder.username.setText(requestName);
                                               requestViewHolder.userabout.setText("You have sent request" + requestName);



                                               requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View view) {
                                                       CharSequence options[] = new CharSequence[]{
                                                               "Cancel",
                                                       };
                                                       AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                       builder.setTitle( "Already Sent");

                                                       builder.setItems(options, new DialogInterface.OnClickListener() {
                                                           @Override
                                                           public void onClick(DialogInterface dialogInterface, int i) {

                                                               if (i == 0){
                                                                   chatRef.child(currentUserID).child(listUserId)
                                                                           .removeValue()
                                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                               @Override
                                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                                   if (task.isSuccessful()){
                                                                                       chatRef.child(listUserId).child(currentUserID )
                                                                                               .removeValue()
                                                                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                   @Override
                                                                                                   public void onComplete(@NonNull Task<Void> task) {
                                                                                                       if (task.isSuccessful()){

                                                                                                           Toast.makeText(getContext(), "Canceled Request", Toast.LENGTH_SHORT).show();
                                                                                                       }
                                                                                                   }
                                                                                               });
                                                                                   }
                                                                               }
                                                                           });
                                                               }



                                                           }
                                                       });
                                                       builder.show();
                                                   }
                                               });
                                           }

                                           @Override
                                           public void onCancelled(@NonNull DatabaseError error) {

                                           }
                                       });
                                   }
                               }
                           }

                           @Override
                           public void onCancelled(@NonNull DatabaseError error) {

                           }
                       });

                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        RequestViewHolder holder = new RequestViewHolder(view);
                        return holder;
                    }
                };

        myRequest.setAdapter(adapter);
        adapter.startListening();
    }

    public static  class RequestViewHolder extends  RecyclerView.ViewHolder{

        TextView username, userabout;
        CircleImageView profile;
        Button Accept, Decline;
        LinearLayout buttonsView;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);


            username = itemView.findViewById(R.id.profil_name);
            userabout = itemView.findViewById(R.id.profil_Stats);
            profile = itemView.findViewById(R.id.user_profileImage);
            buttonsView = itemView.findViewById(R.id.requrequ);
            Accept = itemView.findViewById(R.id.Accept);
            Decline = itemView.findViewById(R.id.Decline);
        }
    }
}