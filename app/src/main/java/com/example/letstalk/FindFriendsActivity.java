package com.example.letstalk;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView findFriends;
    private DatabaseReference userRef;
    private FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        userRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("LetsTalk Users");

        findFriends = findViewById(R.id.rec_friends);
        findFriends.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(userRef, Contacts.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder,
                                            int position,
                                            @NonNull Contacts contacts) {

                holder.username.setText(contacts.getName());
                holder.userStatus.setText(contacts.getStatus());

                Picasso.get()
                        .load(contacts.getImage())
                        .placeholder(R.drawable.baseline_person_24)
                        .into(holder.profileImage);

                holder.itemView.setOnClickListener(view -> {
                    String visit_user_id = getRef(position).getKey();

                    Intent profileInt =
                            new Intent(FindFriendsActivity.this, ProfileActivity.class);

                    profileInt.putExtra("visit_user_id", visit_user_id);
                    startActivity(profileInt);
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                            int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_display_layout, parent, false);

                return new FindFriendsViewHolder(view);
            }
        };

        findFriends.setAdapter(adapter);   // ✅ was wrong name
        adapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {

        TextView username, userStatus;
        CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.profil_name);
            userStatus = itemView.findViewById(R.id.profil_Stats);
            profileImage = itemView.findViewById(R.id.user_profileImage);
        }
    }
}