package com.example.letstalk;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {


    private Button SendCode, Verify;
    private EditText PhoneInput, CodeInput;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private  String mVerificationId;
    private FirebaseAuth mAuth;
    private ProgressDialog loading;

    private PhoneAuthProvider.ForceResendingToken  mResendToken;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        SendCode = findViewById(R.id.send_code);
        Verify = findViewById(R.id.verify_code);

        PhoneInput = findViewById(R.id.phone_number);
        CodeInput = findViewById(R.id.phone_verify);

        loading = new ProgressDialog(this);

        SendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                String phoneNumber = PhoneInput.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "enter your Phone number", Toast.LENGTH_SHORT).show();
                }else{
                    loading.setTitle("Phone Verification");
                    loading.setMessage("please wait...");
                    loading.setCanceledOnTouchOutside(false);
                    loading.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            PhoneLoginActivity.this,
                            callbacks
                    );
                }
            }
        });
        Verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendCode.setVisibility(view.INVISIBLE);
                PhoneInput.setVisibility(view.INVISIBLE);

                String verificationCode = CodeInput.getText().toString();
                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter a code", Toast.LENGTH_SHORT).show();
                }else{
                    loading.setTitle("Code Verification");
                    loading.setMessage("please wait...");
                    loading.setCanceledOnTouchOutside(false);
                    loading.show();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }

        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(PhoneLoginActivity.this, "please enter the number with your country code", Toast.LENGTH_SHORT).show();

                SendCode.setVisibility(View.VISIBLE);
                PhoneInput.setVisibility(View.VISIBLE);

                CodeInput.setVisibility(View.INVISIBLE);
                Verify.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(PhoneLoginActivity.this, "Code has been Sent", Toast.LENGTH_SHORT).show();
                SendCode.setVisibility(View.INVISIBLE);
                PhoneInput.setVisibility(View.INVISIBLE);

                CodeInput.setVisibility(View.VISIBLE);
                Verify.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task){
                if(task.isSuccessful()){
                   loading.dismiss();
                    Toast.makeText(PhoneLoginActivity.this, "loding completed", Toast.LENGTH_SHORT).show();
                    SendUserToMainActivity();
                }else {
                    String message = task.getException().toString();
                    Toast.makeText(PhoneLoginActivity.this, "Error :"+message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}