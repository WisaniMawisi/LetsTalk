package com.example.letstalk;

import static com.example.letstalk.R.layout.users_display_layout;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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


public class ChatsFragment extends Fragment {

    private View privateChatsView;
    private RecyclerView chatList;
    private DatabaseReference chatRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;




    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);


       mAuth= FirebaseAuth.getInstance();
       currentUserID = mAuth.getCurrentUser().getUid();
       chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
       userRef = FirebaseDatabase.getInstance().getReference().child("LetsTalk Users");
       chatList = privateChatsView.findViewById(R.id.ChatList);
       chatList.setLayoutManager(new LinearLayoutManager(getContext()));

       return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts>options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, ChatViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ChatViewHolder chatViewHolder, int i, @NonNull Contacts contacts) {

                        final  String usrsIDs = getRef(i).getKey();
                        final String[] retImage = {"defult_image"};

                        userRef.child(usrsIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {


                                if (snapshot.exists()){
                                    if(snapshot.hasChild("image")){
                                        retImage[0] = snapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).placeholder(R.drawable.baseline_person_24).into(chatViewHolder.profileImage);
                                    }

                                    final  String retName = snapshot.child("name").getValue().toString();
                                    final  String retAbout = snapshot.child("status").getValue().toString();

                                    if(snapshot.child("userState").hasChild("state")){

                                        String state = snapshot.child("userState").child("state").getValue().toString();
                                        String date = snapshot.child("userState").child("date").getValue().toString();
                                        String time = snapshot.child("userState").child("time").getValue().toString();
                                        if(state.equals("online")){
                                            chatViewHolder.username.setText("online");
                                        } else if (state.equals("offline")) {
                                            chatViewHolder.userStatus.setText("Last Seen:"+date+" "+time);
                                        }
                                    }
                                    else{
                                        chatViewHolder.userStatus.setText("offline");
                                    }




                                    chatViewHolder.username.setText(retName);
                                    chatViewHolder.userStatus.setText("Last Seen: "+"\n" +"Date Time");


                                    chatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", usrsIDs);
                                            chatIntent.putExtra("visit_name_id", retName);
                                            chatIntent.putExtra("visit_image", retImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.users_display_layout, parent, false);

                        return new ChatViewHolder(view);
                    }
                };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{

        TextView username, userStatus;
        CircleImageView profileImage;


        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.profil_name);
            userStatus = itemView.findViewById(R.id.profil_Stats);
            profileImage = itemView.findViewById(R.id.user_profileImage);
        }
    }
}