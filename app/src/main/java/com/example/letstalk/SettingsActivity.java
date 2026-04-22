package com.example.letstalk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import androidx.annotation.NonNull;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button Save;
    private EditText username, userStatus;
    private CircleImageView userProfile;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference userProfileStore;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileStore = FirebaseStorage.getInstance()
                .getReference()
                .child("profile Images");

        InitializeFields();

        Save.setOnClickListener(view -> UpdateSetting());

        RetriveUserInfo();
    }

    // ✅ Image Crop Launcher (NEW WAY)
    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
            registerForActivityResult(new CropImageContract(), result -> {

                if (result.isSuccessful()) {

                    Uri resultUri = result.getUriContent();

                    userProfile.setImageURI(resultUri);

                    uploadProfileImage(resultUri);

                } else {
                    Toast.makeText(this,
                            "Image Error: " + result.getError(),
                            Toast.LENGTH_SHORT).show();
                }
            });

    private void uploadProfileImage(Uri resultUri) {

        StorageReference filePath =
                userProfileStore.child(currentUserID + ".jpg");

        filePath.putFile(resultUri)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        filePath.getDownloadUrl()
                                .addOnSuccessListener(uri -> {

                                    String downloadUri = uri.toString();

                                    rootRef.child("LetsTalk Users")
                                            .child(currentUserID)
                                            .child("image")
                                            .setValue(downloadUri);

                                    Toast.makeText(this,
                                            "Profile Image Saved",
                                            Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Toast.makeText(this,
                                "Upload Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void RetriveUserInfo() {

        rootRef.child("LetsTalk Users")
                .child(currentUserID)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            if (snapshot.hasChild("name")) {
                                username.setText(
                                        snapshot.child("name")
                                                .getValue().toString());
                            }

                            if (snapshot.hasChild("status")) {
                                userStatus.setText(
                                        snapshot.child("status")
                                                .getValue().toString());
                            }

                            if (snapshot.hasChild("image")) {
                                String image =
                                        snapshot.child("image")
                                                .getValue().toString();

                                Picasso.get().load(image)
                                        .into(userProfile);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // Open Gallery + Crop
        userProfile.setOnClickListener(view -> {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            startActivityForResult(intent, 1);
        });
    }

    // Gallery Result Only
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 &&
                resultCode == RESULT_OK &&
                data != null) {

            Uri imageUri = data.getData();

            CropImageOptions options = new CropImageOptions();
            options.guidelines = com.canhub.cropper.CropImageView.Guidelines.ON;
            options.aspectRatioX = 1;
            options.aspectRatioY = 1;
            options.fixAspectRatio = true;

            cropImage.launch(
                    new CropImageContractOptions(imageUri, options));
        }
    }

    private void UpdateSetting() {

        String setUserName = username.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this,
                    "Please write your Username",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(setUserStatus)) {
            Toast.makeText(this,
                    "Please write your Status",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> profileMap = new HashMap<>();
        profileMap.put("uid", currentUserID);
        profileMap.put("name", setUserName);
        profileMap.put("status", setUserStatus);

        rootRef.child("LetsTalk Users")
                .child(currentUserID)
                .updateChildren(profileMap)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(this,
                                "Profile Updated Successfully",
                                Toast.LENGTH_SHORT).show();

                        SendUserToMainActivity();

                    } else {
                        Toast.makeText(this,
                                "Update Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void InitializeFields() {

        Save = findViewById(R.id.settings_button);
        username = findViewById(R.id.username);
        userStatus = findViewById(R.id.status);
        userProfile = findViewById(R.id.profile_image);
    }

    private void SendUserToMainActivity() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}