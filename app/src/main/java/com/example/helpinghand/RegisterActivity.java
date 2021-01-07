package com.example.helpinghand;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {
    EditText contentET;
    Button enterBT;
    TextView errorTV;
    TextView resendTV;
    TextView changeTV;
    boolean codeSent;
    ProgressBar progressBar;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneCallBacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String phoneNumber;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        contentET=findViewById(R.id.contentET);
        enterBT=findViewById(R.id.enterBT);
        errorTV=findViewById(R.id.errorTV);
        resendTV=findViewById(R.id.resendTV);
        changeTV=findViewById(R.id.changePhoneTV);

        progressBar=findViewById(R.id.progressBar);
        mAuth=FirebaseAuth.getInstance();
        codeSent=false;
        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                errorTV.setText(errorTV.getText().toString()+"\n"+e.getLocalizedMessage());
                progressBar.setVisibility(View.GONE);
                errorTV.setVisibility(View.VISIBLE);
                enterBT.setEnabled(true);

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                mVerificationId = s;
                mResendToken = forceResendingToken;
                progressBar.setVisibility(View.GONE);
                enterBT.setEnabled(true);
                enterBT.setText("Verify");
                contentET.setText("");
                contentET.setHint("Enter Verification Code");
                contentET.setEnabled(true);
                codeSent=true;
                if(resendTV.getVisibility()!=View.VISIBLE)
                    resendTV.setVisibility(View.VISIBLE);
                if(changeTV.getVisibility()!=View.VISIBLE)
                    changeTV.setVisibility(View.VISIBLE);

            }
        };

        enterBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtil.hideKeyboard(RegisterActivity.this);
                String content=contentET.getText().toString();
                if(TextUtils.isEmpty(content)){
                    errorTV.setText("The field cannot be empty!");
                    errorTV.setVisibility(View.VISIBLE);
                }else {
                    progressBar.setVisibility(View.VISIBLE);
                    enterBT.setEnabled(false);
                    contentET.setEnabled(false);

                    if(!codeSent){
                        errorTV.setVisibility(View.GONE);
                        phoneNumber=contentET.getText().toString();
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneNumber,
                                60,
                                TimeUnit.SECONDS,
                                RegisterActivity.this,
                                mCallbacks
                        );
                    }else {

                        if(progressBar.getVisibility()!=View.VISIBLE)
                            progressBar.setVisibility(View.VISIBLE);
                        String code=contentET.getText().toString();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                        signInWithPhoneAuthCredential(credential);
                    }
                }
            }
        });


    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            progressBar.setVisibility(View.GONE);
                            Intent intent=new Intent(RegisterActivity.this, AccountActivity.class);
                            intent.putExtra("phone",phoneNumber);
                            startActivity(intent);
                            finish();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                errorTV.setText(errorTV.getText().toString()+"\nThe verification code entered was invalid");
                                errorTV.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    public void sendVerificationCode(View view) {
        resendVerificationCode(phoneNumber,mResendToken);
    }

    public void changePhoneNumber(View view) {
        enterBT.setEnabled(true);
        enterBT.setText("Enter Phone Number");
        contentET.setText("");
        contentET.setHint("Enter Your Phone Number");
        contentET.setEnabled(true);
        codeSent=false;
        resendTV.setVisibility(View.GONE);
        changeTV.setVisibility(View.GONE);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        UIUtil.hideKeyboard(RegisterActivity.this);
        return super.dispatchTouchEvent(ev);
    }
}
