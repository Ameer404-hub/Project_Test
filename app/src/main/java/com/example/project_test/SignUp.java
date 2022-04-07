package com.example.project_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class SignUp extends AppCompatActivity {

    TextInputLayout PhoneNum;
    CountryCodePicker CountryCode;
    String UserPhoneNumber, CompletePhoneNum;
    CircularProgressButton SigupBtn;
    Handler handler;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        PhoneNum = findViewById(R.id.PhoneNumber);
        CountryCode = findViewById(R.id.countrycodes);
        SigupBtn = findViewById(R.id.Signup_btn);
    }

    public void validateUser(View view) {
        if(!validatePhoneNum())
            return;
        else {
            UserPhoneNumber = PhoneNum.getEditText().getText().toString().trim();
            if (UserPhoneNumber.charAt(0) == '0') {
                UserPhoneNumber = UserPhoneNumber.substring(1);
            }
            CompletePhoneNum = CountryCode.getSelectedCountryCodeWithPlus() + UserPhoneNumber;
            if(!isConnected(this))
                Toast.makeText(this, "You're device is not connected to internet", Toast.LENGTH_LONG).show();
            else{
                SigupBtn.startAnimation();
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                reference = FirebaseDatabase.getInstance().getReference("Phone Directory");
                reference.orderByChild("phone").equalTo(CompletePhoneNum).
                        addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    SigupBtn.revertAnimation();
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    Toast.makeText(SignUp.this, "This Phone number already registered!", Toast.LENGTH_LONG).show();
                                }
                                else{
                                    SigupBtn.revertAnimation();
                                    verifyOTP();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(SignUp.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    public void verifyOTP(){
        Intent OTPscreen = new Intent(getApplicationContext(), VerifyOTP.class);
        OTPscreen.putExtra("Phone Number", CompletePhoneNum);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(OTPscreen);
                finish();
            }
        },750);
    }

    private boolean validatePhoneNum() {
        String val = PhoneNum.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            PhoneNum.setError("Enter a valid phone number");
            return false;
        } else if (val.length() > 11 || val.length() < 10) {
            PhoneNum.setError("Invalid Phone Number!");
            return false;
        }
        else {
            PhoneNum.setError(null);
            PhoneNum.setErrorEnabled(false);
            return true;
        }
    }

    private boolean isConnected(SignUp CheckInternet){
        ConnectivityManager connectivityManager = (ConnectivityManager) CheckInternet.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if( (wifiCon!=null && wifiCon.isConnected()) || (mobileCon!=null && mobileCon.isConnected()) )
            return true;
        else
            return false;
    }
}