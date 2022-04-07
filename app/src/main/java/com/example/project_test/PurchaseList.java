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
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.project_test.Models.ItemData;
import com.example.project_test.Models.PurchasesListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PurchaseList extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference referenceToItems, referenceToOthers, referenceToOM;
    PurchasesListAdapter adapter;
    ArrayList<ItemData> purchasesList;
    ImageView backArrow;
    ProgressDialog message;
    int myItemCount = 0, OtherItemCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_list);

        referenceToItems = FirebaseDatabase.getInstance().getReference("User").
                child(FirebaseAuth.getInstance().getUid()).child("Item Placed");
        referenceToOthers = FirebaseDatabase.getInstance().getReference("User").
                child(FirebaseAuth.getInstance().getUid()).child("Other Members");

        backArrow = findViewById(R.id.goBack);
        recyclerView = findViewById(R.id.purchasesListItems);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        purchasesList = new ArrayList<>();
        adapter = new PurchasesListAdapter(this, purchasesList);
        recyclerView.setAdapter(adapter);

        message = new ProgressDialog(PurchaseList.this);
        message.setTitle("");
        message.setMessage("Loading...");

        if (!isConnected(this))
            Toast.makeText(PurchaseList.this, "You're device is not connected to internet", Toast.LENGTH_LONG).show();
        else {
            message.show();
            message.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            showPurchasesList();
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

    private void showPurchasesList() {
        referenceToItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ItemData items = ds.getValue(ItemData.class);
                        purchasesList.add(items);
                        myItemCount++;
                    }
                } else
                    myItemCount = 0;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PurchaseList.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        referenceToOthers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        long totalChildren = snapshot.getChildrenCount();
                        String ID = ds.getKey().toString();
                        referenceToOM = FirebaseDatabase.getInstance().getReference("User")
                                .child(ID).child("Item Placed");
                        referenceToOM.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        ItemData items = ds.getValue(ItemData.class);

                                        if (purchasesList.contains(items.getName())) {
                                        } else {
                                            message.dismiss();
                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                            purchasesList.add(items);
                                        }
                                    }
                                } else {
                                    OtherItemCount++;
                                    if (OtherItemCount == totalChildren && myItemCount == 0)
                                        Toast.makeText(PurchaseList.this, "Go Easy! Nothing to Purchase Right Now", Toast.LENGTH_SHORT).show();
                                    message.dismiss();
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                }
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(PurchaseList.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                } else {
                    if (myItemCount == 0)
                        Toast.makeText(PurchaseList.this, "Go Easy! Nothing to Purchase Right Now", Toast.LENGTH_SHORT).show();
                    message.dismiss();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PurchaseList.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isConnected(PurchaseList CheckInternet) {
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