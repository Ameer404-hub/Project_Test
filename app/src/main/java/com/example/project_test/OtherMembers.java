package com.example.project_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.project_test.Models.ItemData;
import com.example.project_test.Models.OtherMembersAdapter;
import com.example.project_test.Models.PurchasesListAdapter;
import com.example.project_test.Models.UserSignupData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OtherMembers extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference referenceToOthers, referenceToOM;
    OtherMembersAdapter adapter;
    ArrayList<UserSignupData> otherMembersList;
    ImageView backArrow;
    ProgressDialog message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_members);

        referenceToOthers = FirebaseDatabase.getInstance().getReference("User").
                child(FirebaseAuth.getInstance().getUid()).child("Other Members");

        backArrow = findViewById(R.id.goBack);

        backArrow = findViewById(R.id.goBack);
        recyclerView = findViewById(R.id.otherMemersList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        otherMembersList = new ArrayList<>();
        adapter = new OtherMembersAdapter(this, otherMembersList);
        recyclerView.setAdapter(adapter);

        message = new ProgressDialog(OtherMembers.this);
        message.setTitle("");
        message.setMessage("Loading...");

        if (!isConnected(this))
            Toast.makeText(OtherMembers.this, "You're device is not connected to internet", Toast.LENGTH_LONG).show();
        else {
            message.show();
            message.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            showMembers();
        }

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goBack = new Intent(getApplicationContext(), HomeScreen.class);
                startActivity(goBack);
                finish();
            }
        });
    }

    private void showMembers() {
        referenceToOthers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String ID = ds.getKey().toString();
                        referenceToOM = FirebaseDatabase.getInstance().getReference("User").child(ID).child("ProfileInfo");
                        referenceToOM.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    UserSignupData Members = snapshot.getValue(UserSignupData.class);
                                    message.dismiss();
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    otherMembersList.add(Members);
                                }
                                else {
                                    message.dismiss();
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                }
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(OtherMembers.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }
                else {
                    message.dismiss();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Toast.makeText(OtherMembers.this, "You haven't joined anybody :(", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OtherMembers.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private boolean isConnected(OtherMembers CheckInternet) {
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