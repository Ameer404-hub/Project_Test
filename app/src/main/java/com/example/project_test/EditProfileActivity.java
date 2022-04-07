package com.example.project_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.project_test.Models.UserSignupData;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class EditProfileActivity extends AppCompatActivity {

    TextInputLayout Fname, Lname, Phone;
    String FNAME, LNAME, PHONE;
    String FnameDB = "", LnameDB = "", PhoneDB = "";
    CircularProgressButton saveProfile;
    Handler handler;
    ImageView backArrow;
    ProgressDialog message;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Fname = findViewById(R.id.Fname);
        Lname = findViewById(R.id.Lname);
        Phone = findViewById(R.id.phone);
        backArrow = findViewById(R.id.goBack);
        saveProfile = findViewById(R.id.saveBtn);

        reference = FirebaseDatabase.getInstance().getReference("User")
                .child(FirebaseAuth.getInstance().getUid()).child("ProfileInfo");

        message = new ProgressDialog(EditProfileActivity.this);
        message.setTitle("");
        message.setMessage("Fetching your profile data...");
        message.show();
        message.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        if(isConnected(this))
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        FnameDB = snapshot.child("firstName").getValue().toString().trim();
                        LnameDB = snapshot.child("lastName").getValue().toString().trim();
                        PhoneDB = snapshot.child("phone").getValue().toString().trim();
                        Fname.getEditText().setText(FnameDB);
                        Lname.getEditText().setText(LnameDB);
                        Phone.getEditText().setText(PhoneDB);
                        message.dismiss();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                    else{
                        message.dismiss();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Fname.getEditText().setText("User not found!");
                        Lname.getEditText().setText("User not found!");
                        Phone.getEditText().setText("User not found!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(EditProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        else{
            message.dismiss();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            Toast.makeText(EditProfileActivity.this, "You're device is not connected to internet", Toast.LENGTH_LONG).show();
        }

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goBack = new Intent(getApplicationContext(), HomeScreen.class);
                startActivity(goBack);
                finish();
            }
        });

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FNAME = Fname.getEditText().getText().toString().trim();
                LNAME = Lname.getEditText().getText().toString().trim();
                if(!vaidateFNAME() | !vaidateLNAME())
                    return;
                if(!isConnected(EditProfileActivity.this))
                    Toast.makeText(EditProfileActivity.this, "You're device is not connected to internet", Toast.LENGTH_LONG).show();
                else{
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    saveProfile.startAnimation();
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            validateDuplication();
                        }
                    },750);
                }
            }
        });
    }

    public void validateDuplication(){
        if(FNAME.equals(FnameDB) && LNAME.equals(LnameDB)){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            Toast.makeText(this, "Same data cannot be updated", Toast.LENGTH_SHORT).show();
            saveProfile.revertAnimation();
        }
        else {
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
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        saveProfile.doneLoadingAnimation(Color.parseColor("#FFEB3B"),
                                BitmapFactory.decodeResource(getResources(),R.drawable.error_item));
                        Toast.makeText(EditProfileActivity.this, "Name already in use! Try some other Name", Toast.LENGTH_LONG).show();
                        handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                saveProfile.revertAnimation();
                            }
                        },1000);
                    }
                    else
                        updateProfile();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(EditProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void updateProfile(){
        reference = FirebaseDatabase.getInstance().getReference("User");
        HashMap<String, Object> Update = new HashMap<>();
        Update.put("firstName", FNAME);
        Update.put("lastName", LNAME);
        reference.child(FirebaseAuth.getInstance().getUid()).child("ProfileInfo").updateChildren(Update);
        Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        saveProfile.doneLoadingAnimation(Color.parseColor("#FFEB3B"),
                BitmapFactory.decodeResource(getResources(),R.drawable.done));
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                saveProfile.revertAnimation();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Intent goBack = new Intent(getApplicationContext(), HomeScreen.class);
                startActivity(goBack);
                finish();
            }
        },1000);

    }

    private boolean vaidateFNAME() {
        if(FNAME.isEmpty()){
            Fname.setError("Field cannot be empty");
            Fname.setErrorEnabled(true);
            return false;
        }
        else if(FNAME.matches(".*\\d.*") || FNAME.matches("[`~!@#$%^&*()_+=-]+")){
            Fname.setError("Numerics and special characters not allowed");
            Fname.setErrorEnabled(true);
            return false;
        }
        else{
            Fname.setError(null);
            Fname.setErrorEnabled(false);
            return true;
        }
    }

    private boolean vaidateLNAME() {
        if(LNAME.isEmpty()){
            Lname.setError("Field cannot be empty");
            Lname.setErrorEnabled(true);
            return false;
        }
        else if(LNAME.matches(".*\\d.*") || LNAME.matches("[`~!@#$%^&*()_+=-]+")){
            Lname.setError("Numerics and special characters not allowed");
            Lname.setErrorEnabled(true);
            return false;
        }
        else{
            Lname.setError(null);
            Lname.setErrorEnabled(false);
            return true;
        }
    }

    private boolean isConnected(EditProfileActivity CheckInternet){
        ConnectivityManager connectivityManager = (ConnectivityManager) CheckInternet.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if( (wifiCon!=null && wifiCon.isConnected()) || (mobileCon!=null && mobileCon.isConnected()) )
            return true;
        else
            return false;
    }

    public void onBackPressed() {
        Intent goBack = new Intent(getApplicationContext(), HomeScreen.class);
        startActivity(goBack);
        finish();
    }
}