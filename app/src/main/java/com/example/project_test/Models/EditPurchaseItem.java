package com.example.project_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.project_test.Models.ItemData;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class EditPurchaseItem extends AppCompatActivity {

    DatabaseReference referenceToUser, referenceToUserItems, referencetoUserProfile, referenceToAllItems;
    TextInputEditText StatusDialog;
    TextInputLayout iNameField, iStatusField, iQtyField, iDescField, iPlacedbyField, iPurchasedbyField;
    String getItemID, IDmatched, newStatus, myName, Purchaser;
    String iName, iStatus, iQty, iDesc, iPlacedby, iPurchasedby;
    CircularProgressButton updateItem;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_purchase_item);

        getItemID = getIntent().getStringExtra("itemID");

        iNameField = findViewById(R.id.itemName);
        iStatusField = findViewById(R.id.itemStatus);
        iQtyField = findViewById(R.id.itemQty);
        iDescField = findViewById(R.id.itemDesc);
        iPlacedbyField = findViewById(R.id.itemPlaced);
        iPurchasedbyField = findViewById(R.id.itemPurchased);

        updateItem = findViewById(R.id.updateItemBtn);

        referenceToUser = FirebaseDatabase.getInstance().getReference("User");

        referencetoUserProfile = FirebaseDatabase.getInstance().getReference("User")
                .child(FirebaseAuth.getInstance().getUid()).child("ProfileInfo");

        if (!isConnected(this))
            Toast.makeText(EditPurchaseItem.this, "You're device is not connected to internet", Toast.LENGTH_LONG).show();
        else {
            referenceToUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        myName = snapshot.child(FirebaseAuth.getInstance().getUid())
                                .child("ProfileInfo")
                                .child("firstName").getValue().toString()
                                +"_"+
                                snapshot.child(FirebaseAuth.getInstance().getUid())
                                .child("ProfileInfo")
                                .child("lastName").getValue().toString();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String userID = ds.getKey();
                            referenceToUserItems = FirebaseDatabase.getInstance().getReference("User")
                                    .child(userID).child("Item Placed").child(getItemID);
                            referenceToUserItems.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        IDmatched = userID;
                                        ItemData Item = snapshot.getValue(ItemData.class);
                                        iName = snapshot.child("name").getValue().toString();
                                        iStatus = snapshot.child("status").getValue().toString();
                                        iQty = snapshot.child("qty").getValue().toString();
                                        iDesc = snapshot.child("desc").getValue().toString();
                                        iPlacedby = snapshot.child("placedBy").getValue().toString();
                                        iPurchasedby = snapshot.child("purchasedBy").getValue().toString();

                                        iNameField.getEditText().setText(iName);
                                        iStatusField.getEditText().setText(iStatus);
                                        iQtyField.getEditText().setText(iQty);
                                        iDescField.getEditText().setText(iDesc);
                                        iPlacedbyField.getEditText().setText(iPlacedby);
                                        iPurchasedbyField.getEditText().setText(iPurchasedby);
                                    } else {

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(EditPurchaseItem.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(EditPurchaseItem.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            StatusDialog = findViewById(R.id.showStatusDialog);
            StatusDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder showStatusDialog = new AlertDialog.Builder(EditPurchaseItem.this);
                    int checkedItem = 0;
                    String[] items = {"Awaiting", "Purchased", "Not available"};

                    if (iStatusField.getEditText().getText().toString().equals("Awaiting"))
                        checkedItem = 0;
                    else if (iStatusField.getEditText().getText().toString().equals("Purchased"))
                        checkedItem = 1;
                    else if (iStatusField.getEditText().getText().toString().equals("Not available"))
                        checkedItem = 2;

                    showStatusDialog.setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                iStatusField.getEditText().setText(items[0]);
                                iPurchasedbyField.getEditText().setText(items[0]);
                                break;
                            case 1:
                                iStatusField.getEditText().setText(items[1]);
                                iPurchasedbyField.getEditText().setText(myName);
                                break;
                            case 2:
                                iStatusField.getEditText().setText(items[2]);
                                iPurchasedbyField.getEditText().setText(items[2]);
                                break;
                        }
                    });

                    showStatusDialog.setTitle("Status");
                    showStatusDialog.setNegativeButton("Set Status", (dialog, which) -> dialog.dismiss());
                    AlertDialog Dialog = showStatusDialog.create();
                    Dialog.setCancelable(false);
                    Dialog.show();
                }
            });

            updateItem.setOnClickListener(view -> {
                updateItem.startAnimation();

                referenceToAllItems = FirebaseDatabase.getInstance().getReference("Item").child(getItemID);
                referenceToAllItems.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            newStatus = iStatusField.getEditText().getText().toString();
                            HashMap<String, Object> Update = new HashMap<>();
                            Purchaser = iPurchasedbyField.getEditText().getText().toString();
                            Update.put("status", newStatus);
                            Update.put("purchasedBy", Purchaser);
                            referenceToAllItems.updateChildren(Update);
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditPurchaseItem.this, ""+error, Toast.LENGTH_SHORT).show();
                    }
                });
                referenceToUserItems = FirebaseDatabase.getInstance().getReference("User")
                        .child(IDmatched).child("Item Placed").child(getItemID);
                referenceToUserItems.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            newStatus = iStatusField.getEditText().getText().toString();
                            HashMap<String, Object> Update = new HashMap<>();
                            Purchaser = iPurchasedbyField.getEditText().getText().toString();
                            Update.put("status", newStatus);
                            Update.put("purchasedBy", Purchaser);
                            referenceToUserItems.updateChildren(Update);
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditPurchaseItem.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                handler = new Handler();
                handler.postDelayed(() -> {
                    Toast.makeText(EditPurchaseItem.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                    updateItem.doneLoadingAnimation(Color.parseColor("#FFEB3B"),
                            BitmapFactory.decodeResource(getResources(), R.drawable.done));
                }, 1000);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateItem.revertAnimation();

                    }
                }, 1500);
                handler.postDelayed(() -> {
                    FcmNotificationsSender notificationsSender =
                            new FcmNotificationsSender("/topics/"+myName, iName + " " + newStatus,
                                    "Status updated by " + myName,
                                    getApplicationContext(), EditPurchaseItem.this);
                    notificationsSender.SendNotifications();
                    Intent goBack = new Intent(getApplicationContext(), PurchaseList.class);
                    startActivity(goBack);
                    finish();
                }, 2000);
            });
        }
    }

    private boolean isConnected(EditPurchaseItem CheckInternet) {
        ConnectivityManager connectivityManager = (ConnectivityManager) CheckInternet.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiCon != null && wifiCon.isConnected()) || (mobileCon != null && mobileCon.isConnected()))
            return true;
        else
            return false;
    }

    public void onBackPressed() {
        Intent goBack = new Intent(getApplicationContext(), PurchaseList.class);
        startActivity(goBack);
        finish();
    }
}