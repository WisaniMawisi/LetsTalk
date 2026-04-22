package com.example.letstalk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private Button SignUp;
    private TextView Login, UserNumber;
    private EditText Email, Password;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        InitializeFields();

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendRegisterToLogin();
            }
        });

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });

    }

    private void CreateNewAccount() {
        String email = Email.getText().toString();
        String password = Password.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        }
        else {
            loading.setTitle("Create New Account");
            loading.setMessage("Please wait, while we are creating new account for you..");
            loading.setCanceledOnTouchOutside(true);
            loading.show();


            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull Task<String> task) {

                                        if (!task.isSuccessful()) {
                                            return;
                                        }

                                        String deviceToken = task.getResult();
                                        String currentUserID = mAuth.getCurrentUser().getUid();

                                        // Save user data correctly (DO NOT use setValue(" "))
                                        rootRef.child("LetsTalk Users")
                                                .child(currentUserID)
                                                .child("device_token")
                                                .setValue(deviceToken)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        SendRegisterToMainActivity();
                                                        Toast.makeText(RegisterActivity.this,
                                                                "Account Created Successfully",
                                                                Toast.LENGTH_SHORT).show();

                                                        loading.dismiss();
                                                    }
                                                });
                                    }
                                });
                    }else{
                        String message = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error : "+ message, Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                }
            });
        }
    }

    private void SendRegisterToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendRegisterToLogin() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void InitializeFields() {

        SignUp = findViewById(R.id.register_button);
        Login = findViewById(R.id.login_create);
        UserNumber = findViewById(R.id.register_number);
        Email = findViewById(R.id.login_email);
        Password = findViewById(R.id.login_password);

        loading  = new ProgressDialog(this);
    }
}