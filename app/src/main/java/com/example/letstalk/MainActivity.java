package com.example.letstalk;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;




import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout mTableLayout;
    private TabAccessAdapter myTabAccessAdpter;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        // ---- Correct Toolbar ----
        mToolbar = findViewById(R.id.main_app_bar);  // Match XML ID
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("LetsTalk");
        }

        // ---- Correct ViewPager ----
        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabAccessAdpter = new TabAccessAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabAccessAdpter);

        mTableLayout = findViewById(R.id.main_tabs);
        mTableLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){
            SendUserToLoginActivity();
        }else{
            updateUserStatus("online");
            VerifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!= null){
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!= null){
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        rootRef.child("LetsTalk Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("name").exists()){
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "Profile not set", Toast.LENGTH_SHORT).show();
                    sendUserToSettings();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_option){
            updateUserStatus("offline");
            mAuth.signOut();
            SendUserToLoginActivity();
        }
        if (item.getItemId() == R.id.main_settings_option){

            sendUserToSettings();

        }
        if (item.getItemId()== R.id.main_find_friends_option){
            sendUserToFindFriends();
        }
        if (item.getItemId()== R.id.main_group_option){
            RequestNewGroup();
        }
        return true;
    }

    private void sendUserToFindFriends() {
        Intent findfriensint = new Intent(this, FindFriendsActivity.class);
        startActivity(findfriensint);
        finish();
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(this);
        groupNameField.setHint("e.g Stokvel");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please write Group Name...", Toast.LENGTH_SHORT).show();
                }else{

                    CreateNewGroup(groupName);

                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
    }

    private void CreateNewGroup(String groupName) {
        rootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupName+"is created", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendUserToSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
    }

    private  void updateUserStatus(String state){
        String saveCurrentTime, saveCjurrentDate;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat curentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCjurrentDate = curentDate.format(calendar.getTime());

        SimpleDateFormat curentTime = new SimpleDateFormat("hh:mm");
        saveCurrentTime = curentTime.format(calendar.getTime());

        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("time", saveCurrentTime);
        onlineState.put("date", saveCjurrentDate);
        onlineState.put("state", "state");

        currentUserID = mAuth.getCurrentUser().getUid();

        rootRef.child("LetsTalk Users").child(currentUserID).child("userState")
                .updateChildren(onlineState);



    }
}