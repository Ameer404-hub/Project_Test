package com.example.project_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.project_test.Models.UserSignupData;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;


public class ProfileScreen extends AppCompatActivity {

    TextInputLayout FnameField, LnameField, PhoneField;
    Button Next;
    String FNAME, LNAME, PHONE;
    CircularProgressButton setProfile;
    Handler handler;
    ProgressDialog message;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        FirebaseMessaging.getInstance().subscribeToTopic("check");

        FnameField = findViewById(R.id.Fname);
        LnameField = findViewById(R.id.Lname);
        PhoneField = findViewById(R.id.phone);
        Next = (Button) findViewById(R.id.nextBtn);

        PHONE = getIntent().getStringExtra("PhoneNumber");
        PhoneField.getEditText().setText(PHONE);
        setProfile = findViewById(R.id.nextBtn);

        message = new ProgressDialog(ProfileScreen.this);
        message.setTitle("");
        message.setMessage("Setting up your Profile...");

        setProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FNAME = FnameField.getEditText().getText().toString().trim();
                LNAME = LnameField.getEditText().getText().toString().trim();
                if(!vaidateFNAME() | !vaidateLNAME())
                    return;
                else if(!isConnected(ProfileScreen.this))
                    Toast.makeText(ProfileScreen.this, "You're device is not connected to internet", Toast.LENGTH_LONG).show();
                else{
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    setProfile.startAnimation();
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            insertData();
                        }
                    },750);
                }
            }
        });
    }

    public void insertData(){
        reference = FirebaseDatabase.getInstance().getReference().child("User");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userExist = false;
                for(DataSnapshot ds : snapshot.getChildren()) {
                    UserSignupData userInfo = ds.child("ProfileInfo").getValue(UserSignupData.class);
                    String DBFname = userInfo.getFirstName();
                    String DBLname = userInfo.getLastName();
                    if (DBFname.equals(FNAME) & DBLname.equals(LNAME)) {
                        userExist = true;
                        break;
                    }
                }
                if(userExist == true){
                    setProfile.revertAnimation();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Toast.makeText(ProfileScreen.this, "Name already in use! Try some other Name", Toast.LENGTH_LONG).show();
                }
                else if(userExist == false){
                    AddNewUser();
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            message.dismiss();
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            FcmNotificationsSender notificationsSender =
                                    new FcmNotificationsSender("/topics/check", "WELCOM TO DPM",
                                            "Please refer to the QUICK TOTURIAL available in the side menu",
                                            getApplicationContext(), ProfileScreen.this);
                            notificationsSender.SendNotifications();
                            Intent HomeScreen = new Intent(getApplicationContext(), HomeScreen.class);
                            startActivity(HomeScreen);
                            finish();
                        }
                    } ,2000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileScreen.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void AddNewUser() {
        message.show();
        message.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
        UserSignupData NewUser = new UserSignupData(FNAME, LNAME, PHONE);
        reference.child(FirebaseAuth.getInstance().getUid()).child("ProfileInfo").setValue(NewUser);
    }

    private boolean vaidateFNAME() {
        if(FNAME.isEmpty()){
            FnameField.setError("Field cannot be empty");
            FnameField.setErrorEnabled(true);
            return false;
        }
        else if(FNAME.matches(".*\\d.*") || FNAME.matches("[`~!@#$%^&*()_+=-]+")){
            FnameField.setError("Numerics and special characters not allowed");
            FnameField.setErrorEnabled(true);
            return false;
        }
        else{
            FnameField.setError(null);
            FnameField.setErrorEnabled(false);
            return true;
        }
    }

    private boolean vaidateLNAME() {
        if(LNAME.isEmpty()){
            LnameField.setError("Field cannot be empty");
            LnameField.setErrorEnabled(true);
            return false;
        }
        else if(LNAME.matches(".*\\d.*") || LNAME.matches("[`~!@#$%^&*()_+=-]+")){
            LnameField.setError("Numerics and special characters not allowed");
            LnameField.setErrorEnabled(true);
            return false;
        }
        else{
            LnameField.setError(null);
            LnameField.setErrorEnabled(false);
            return true;
        }
    }

    private boolean isConnected(ProfileScreen CheckInternet){
        ConnectivityManager connectivityManager = (ConnectivityManager) CheckInternet.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if( (wifiCon!=null && wifiCon.isConnected()) || (mobileCon!=null && mobileCon.isConnected()) )
            return true;
        else
            return false;
    }
}