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

public class LoginActivity extends AppCompatActivity {



    private Button Login;
    private TextView Create, UserNumber, forgetPass;
    private EditText Email, Password;
    private ProgressDialog loading;

    FirebaseAuth mAuth;
    private DatabaseReference rootRef, userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef = FirebaseDatabase.getInstance().getReference().child("LetsTalk Users");
        InitializeFields();


        Create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendLoginToRegister();
            }
        });

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }
        });

        UserNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phoneIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(phoneIntent);
            }
        });

    }

    private void AllowUserToLogin() {
        String email = Email.getText().toString();
        String password = Password.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        }
        else {

            //Loding Dialog
            loading.setTitle("Sign In");
            loading.setMessage("Please wait while checking your account...");
            loading.setCanceledOnTouchOutside(true);
            loading.show();


            //Logging in
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String currentUserId = mAuth.getCurrentUser().getUid();

                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull Task<String> tokenTask) {

                                        if (!tokenTask.isSuccessful()) {
                                            return;
                                        }

                                        String deviceToken = tokenTask.getResult();

                                        userRef.child(currentUserId).child("device_token")
                                                .setValue(deviceToken)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> databaseTask) {

                                                        if (databaseTask.isSuccessful()) {

                                                            SendUserToMainActivity();
                                                            Toast.makeText(LoginActivity.this,
                                                                    "Successfully logged in",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }

                                                        loading.dismiss();
                                                    }
                                                });
                                    }
                                });

                    }else {
                        String message = task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Error :"+ message , Toast.LENGTH_SHORT).show();
                    }   loading.dismiss();
                }
            });
        }
    }

    private void SendLoginToRegister() {
        Intent registerMain = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerMain);
    }

    private void InitializeFields() {
        Login = findViewById(R.id.login_button);
        UserNumber = findViewById(R.id.register_number);
        Email = findViewById(R.id.login_email);
        Password = findViewById(R.id.login_password);
        forgetPass = findViewById(R.id.login_forget);
        Create = findViewById(R.id.login_create);

        loading  = new ProgressDialog(this);
    }


    private void SendUserToMainActivity() {

            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish();
    }
}