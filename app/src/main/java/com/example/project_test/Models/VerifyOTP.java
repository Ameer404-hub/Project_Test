package com.example.project_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

import com.chaos.view.PinView;
import com.example.project_test.Models.UserSignupData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class VerifyOTP extends AppCompatActivity {

    PinView UserPin;
    TextView otpDescription;
    String CodeFromSystem;
    String FirstName = "", LastName = "", CompletePhoneNum;
    CircularProgressButton VerfiyCodeBtn;
    Button resendCode;
    Handler handler;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifyotp);

        UserPin = (PinView) findViewById(R.id.pin_view);
        otpDescription = findViewById(R.id.otp_description_text);
        VerfiyCodeBtn = findViewById(R.id.codeVerify);
        resendCode = findViewById(R.id.resend);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        CompletePhoneNum = getIntent().getStringExtra("Phone Number");
        otpDescription.setText("Waiting to automatically fetch OTP Sent On "+CompletePhoneNum);
        sendVerificationCodeToUser(CompletePhoneNum);

        VerfiyCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isConnected(VerifyOTP.this))
                    Toast.makeText(VerifyOTP.this, "You're device is not connected to internet", Toast.LENGTH_LONG).show();
                else {
                    String code = UserPin.getText().toString();
                    if(!code.isEmpty()){
                        VerfiyCodeBtn.startAnimation();
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                VerifyCode(code);
                            }
                        },750);
                    }
                    else
                        Toast.makeText(VerifyOTP.this, "Enter the OTP sent on "+CompletePhoneNum+" to proceed", Toast.LENGTH_LONG).show();
                }
            }
        });

        resendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isConnected(VerifyOTP.this))
                    Toast.makeText(VerifyOTP.this, "You're device is not connected to internet", Toast.LENGTH_LONG).show();
                else {
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendVerificationCodeToUser(CompletePhoneNum);
                            Toast.makeText(VerifyOTP.this, "Code resent on\n" + CompletePhoneNum, Toast.LENGTH_SHORT).show();
                        }
                    },750);

                }
            }
        });
    }

    private void sendVerificationCodeToUser(String completePhoneNum) {
        if(!isConnected(VerifyOTP.this))
            Toast.makeText(VerifyOTP.this, "You're device is not connected to internet", Toast.LENGTH_LONG).show();
        else {
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                            .setPhoneNumber(completePhoneNum)       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(this)                // Activity (for callback binding)
                            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    CodeFromSystem = s;
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    String Code = phoneAuthCredential.getSmsCode();
                    if(Code!=null){
                        UserPin.setText(Code);
                        if (Build.VERSION.SDK_INT >= 26)
                            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                        else
                            vibrator.vibrate(200);
                        VerfiyCodeBtn.startAnimation();
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                VerifyCode(Code);
                            }
                        },750);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(VerifyOTP.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };

    private void VerifyCode(String Code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(CodeFromSystem, Code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (Build.VERSION.SDK_INT >= 26)
                                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                            else
                                vibrator.vibrate(200);
                            VerfiyCodeBtn.doneLoadingAnimation(Color.parseColor("#FFEB3B"),
                                    BitmapFactory.decodeResource(getResources(),R.drawable.done));
                            registerPhoneNumber();
                            handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    VerfiyCodeBtn.revertAnimation();
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    Intent NextScreen = new Intent(getApplicationContext(), ProfileScreen.class);
                                    NextScreen.putExtra("PhoneNumber", CompletePhoneNum);
                                    startActivity(NextScreen);
                                    finish();
                                }
                            } ,1000);
                        }
                        else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                VerfiyCodeBtn.doneLoadingAnimation(Color.parseColor("#FFEB3B"),
                                        BitmapFactory.decodeResource(getResources(),R.drawable.error_item));
                                Toast.makeText(VerifyOTP.this, "Code not matched! Try again", Toast.LENGTH_LONG).show();
                                handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        VerfiyCodeBtn.revertAnimation();
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    }
                                },1000);
                            }
                        }
                    }
                });
    }

    private void registerPhoneNumber() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Phone Directory");
        UserSignupData NewUser = new UserSignupData(CompletePhoneNum);
        reference.child(CompletePhoneNum).setValue(NewUser);
    }

    private boolean isConnected(VerifyOTP CheckInternet){
        ConnectivityManager connectivityManager = (ConnectivityManager) CheckInternet.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if( (wifiCon!=null && wifiCon.isConnected()) || (mobileCon!=null && mobileCon.isConnected()) )
            return true;
        else
            return false;
    }
}