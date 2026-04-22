package com.example.letstalk;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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


public class ContactsFragment extends Fragment {

    private View ContactView;
    private DatabaseReference contRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private RecyclerView myCont;
    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactView = inflater.inflate(R.layout.fragment_contacts, container, false);


        myCont = ContactView.findViewById(R.id.contView);
        myCont.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        contRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("LetsTalk Users");

        return ContactView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new  FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(contRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, ContactViewHoler> adapter =
                new FirebaseRecyclerAdapter<Contacts, ContactViewHoler>(options){
                    @Override
                    protected void onBindViewHolder(@NonNull ContactViewHoler holder, int position,@NonNull Contacts model){

                        String usersId = getRef(position).getKey();
                        userRef.child(usersId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){

                                    if(snapshot.child("userState").hasChild("state")){

                                        String state = snapshot.child("userState").child("state").getValue().toString();
                                        String date = snapshot.child("userState").child("date").getValue().toString();
                                        String time = snapshot.child("userState").child("time").getValue().toString();
                                        if(state.equals("online")){
                                            holder.onlineIcon.setVisibility(View.VISIBLE);
                                        } else if (state.equals("offline")) {
                                           holder.onlineIcon.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    else{
                                        holder.onlineIcon.setVisibility(View.INVISIBLE);
                                    }

                                    if (snapshot.hasChild("image")){
                                        String profileImage = snapshot.child("image").getValue().toString();
                                        String userName = snapshot.child("name").getValue().toString();
                                        String userAbout = snapshot.child("status").getValue().toString();

                                        holder.username.setText(userName);
                                        holder.userabout.setText(userAbout);
                                        Picasso.get().load(profileImage).placeholder(R.drawable.baseline_person_24).into(holder.profile);

                                    }
                                    else {
                                        String userName = snapshot.child("name").getValue().toString();
                                        String userAbout = snapshot.child("status").getValue().toString();

                                        holder.username.setText(userName);
                                        holder.userabout.setText(userAbout);
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
                    public ContactViewHoler onCreateViewHolder(@NonNull ViewGroup viewGroup, int i){

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        ContactViewHoler viewHoler = new ContactViewHoler(view);
                        return viewHoler;
                    }

                };
        myCont.setAdapter(adapter);
        adapter.startListening();

    }


    public  static  class  ContactViewHoler extends RecyclerView.ViewHolder{

        TextView username, userabout;
        CircleImageView profile;
        ImageView onlineIcon;
        public ContactViewHoler(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.profil_name);
            userabout = itemView.findViewById(R.id.profil_Stats);
            profile = itemView.findViewById(R.id.user_profileImage);
            onlineIcon = itemView.findViewById(R.id.onlineStats);
        }
    }


}