package com.example.project_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project_test.Models.UserSignupData;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class JoinOthers extends AppCompatActivity {

    TextInputLayout IDField;
    Toolbar toolbar;
    String getID, myName, otherName, notifName, joinLink;
    ImageView backArrow;
    Handler handler;
    DatabaseReference IDreference;
    CircularProgressButton Join;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_others);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        IDField = findViewById(R.id.joinOthers);
        backArrow = findViewById(R.id.goBack);
        Join = findViewById(R.id.join);

        IDreference = FirebaseDatabase.getInstance().getReference("User");
        backArrow.setOnClickListener(view -> {
            Intent goBack = new Intent(getApplicationContext(), HomeScreen.class);
            startActivity(goBack);
            finish();
        });

        Join.setOnClickListener(view -> {
            getID = IDField.getEditText().getText().toString();
            if (!vaidateID())
                return;
            else if (!isConnected(JoinOthers.this))
                Toast.makeText(JoinOthers.this, "You're device is not connected to internet", Toast.LENGTH_LONG).show();
            else {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Join.startAnimation();
                handler = new Handler();
                handler.postDelayed(() -> joinMembers(), 750);
            }
        });
    }

    private void joinMembers() {
        IDreference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userExist = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (getID.equals(ds.getKey())) {
                        userExist = true;
                        break;
                    } else
                        userExist = false;
                }
                if (userExist == true) {
                    UserSignupData myInfo = snapshot.child(FirebaseAuth.getInstance().getUid()).
                            child("ProfileInfo").getValue(UserSignupData.class);
                    String myFname = myInfo.getFirstName();
                    String myLname = myInfo.getLastName();
                    myName = myFname + " " + myLname;
                    notifName = myFname + "_" + myLname;

                    UserSignupData otherInfo = snapshot.child(getID).
                            child("ProfileInfo").getValue(UserSignupData.class);
                    String otherFname = otherInfo.getFirstName();
                    String otherLname = otherInfo.getLastName();
                    otherName = otherFname + "_" + otherLname;

                    IDreference.child(FirebaseAuth.getInstance().getUid()).
                            child("Other Members").child(getID).setValue(otherName);
                    IDreference.child(getID).child("Other Members").
                            child(FirebaseAuth.getInstance().getUid()).setValue(notifName);

                    Join.doneLoadingAnimation(Color.parseColor("#FFEB3B"),
                            BitmapFactory.decodeResource(getResources(), R.drawable.done));
                    Toast.makeText(JoinOthers.this, "You Joined " + otherName, Toast.LENGTH_LONG).show();

                    handler = new Handler();
                    handler.postDelayed(() -> {
                        FcmNotificationsSender notificationsSender =
                                new FcmNotificationsSender("/topics/" + notifName, myName + " has joined you",
                                        "Open your 'Other Members' section to see more details.",
                                        getApplicationContext(), JoinOthers.this);
                        notificationsSender.SendNotifications();
                        Join.revertAnimation();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }, 1000);
                } else if (userExist == false) {
                    Join.doneLoadingAnimation(Color.parseColor("#FFEB3B"),
                            BitmapFactory.decodeResource(getResources(), R.drawable.error_item));
                    Toast.makeText(JoinOthers.this, "User not found!", Toast.LENGTH_LONG).show();
                    handler = new Handler();
                    handler.postDelayed(() -> {
                        Join.revertAnimation();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }, 1000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JoinOthers.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean vaidateID() {
        if (getID.isEmpty()) {
            IDField.setError("Field cannot be empty");
            IDField.setErrorEnabled(true);
            return false;
        } else {
            IDField.setError(null);
            IDField.setErrorEnabled(false);
            return true;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.side_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        joinLink = IDreference.child(FirebaseAuth.getInstance().getUid()).getKey();
        switch (item.getItemId()) {
            case R.id.shareLink:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, joinLink);
                startActivity(Intent.createChooser(shareIntent, "Share Via"));
                break;

            case R.id.copyLink:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Link", joinLink);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                break;

            case R.id.shareVia:
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, joinLink);
                try {
                    startActivity(whatsappIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "Whatsapp not found!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isConnected(JoinOthers CheckInternet) {
        ConnectivityManager connectivityManager = (ConnectivityManager) CheckInternet.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiCon != null && wifiCon.isConnected()) || (mobileCon != null && mobileCon.isConnected()))
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