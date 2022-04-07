package com.example.project_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
import com.google.firebase.messaging.FirebaseMessaging;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class PlaceItems extends AppCompatActivity{

    TextInputLayout ItemName, ItemQty, ItemDesc;
    String Name = "", Qty = "", Desc = "", Status = "Awaiting", placedBy = "", purchasedBy = "Not Purchased", myName = "", LnameDB = "";
    Handler handler;
    ImageView backArrow;
    CircularProgressButton AddItem;

    DatabaseReference referenceToUser, referenceToItems, referenceToUserItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_items);

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        ItemName = (TextInputLayout) findViewById(R.id.itemname);
        ItemQty = (TextInputLayout) findViewById(R.id.itemQty);
        ItemDesc = (TextInputLayout) findViewById(R.id.itemdesc);
        backArrow = findViewById(R.id.goBack);
        AddItem = findViewById(R.id.addItemBtn);

        referenceToUser = FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getUid()).child("ProfileInfo");
        referenceToItems = FirebaseDatabase.getInstance().getReference().child("Item");

        referenceToUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    myName = snapshot.child("firstName").getValue().toString() +" "+snapshot.child("lastName").getValue().toString();
                    placedBy = myName;
                }
                else
                    placedBy = "User not found";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlaceItems.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goBack = new Intent(getApplicationContext(), HomeScreen.class);
                startActivity(goBack);
                finish();
            }
        });

        AddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Name = ItemName.getEditText().getText().toString().trim();
                Qty = ItemQty.getEditText().getText().toString().trim();
                Desc = ItemDesc.getEditText().getText().toString().trim();
                if(!validateItemName() | !validateItemQty() | !validateItemDecs())
                    return;
                if(!isConnected(PlaceItems.this))
                    Toast.makeText(PlaceItems.this, "You're device is not connected to internet", Toast.LENGTH_LONG).show();
                else{
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    AddItem.startAnimation();
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
        referenceToItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean itemExist = false;
                if(snapshot.exists()){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        ItemData checkItem = ds.getValue(ItemData.class);
                        String DBitemName = checkItem.getName();
                        if (DBitemName.equals(Name)) {
                            itemExist = true;
                            break;
                        }
                    }
                    if(itemExist == true){
                        AddItem.doneLoadingAnimation(Color.parseColor("#FFEB3B"),
                                BitmapFactory.decodeResource(getResources(),R.drawable.error_item));
                        Toast.makeText(PlaceItems.this, "Item already placed!", Toast.LENGTH_LONG).show();
                        handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AddItem.revertAnimation();
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                        },1000);
                    }
                    else if(itemExist == false){
                        ItemData newItem = new ItemData(Name, Qty, Desc, Status, placedBy, purchasedBy);
                        referenceToItems.child(Name).setValue(newItem);
                        insertIntoUser();
                        AddItem.doneLoadingAnimation(Color.parseColor("#FFEB3B"),
                                BitmapFactory.decodeResource(getResources(),R.drawable.done));
                        Toast.makeText(PlaceItems.this, "Item Uploaded", Toast.LENGTH_SHORT).show();
                        ItemName.getEditText().getText().clear();
                        ItemQty.getEditText().getText().clear();
                        ItemDesc.getEditText().getText().clear();
                        handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AddItem.revertAnimation();
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                        },1000);
                    }
                }
                else{
                    ItemData newItem = new ItemData(Name, Qty, Desc, Status, placedBy, purchasedBy);
                    referenceToItems.child(Name).setValue(newItem);
                    insertIntoUser();
                    AddItem.doneLoadingAnimation(Color.parseColor("#FFEB3B"),
                            BitmapFactory.decodeResource(getResources(),R.drawable.done));
                    Toast.makeText(PlaceItems.this, "Item Uploaded", Toast.LENGTH_SHORT).show();
                    ItemName.getEditText().getText().clear();
                    ItemQty.getEditText().getText().clear();
                    ItemDesc.getEditText().getText().clear();
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AddItem.revertAnimation();
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                    },1000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlaceItems.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isConnected(PlaceItems CheckInternet){
        ConnectivityManager connectivityManager = (ConnectivityManager) CheckInternet.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if( (wifiCon!=null && wifiCon.isConnected()) || (mobileCon!=null && mobileCon.isConnected()) )
            return true;
        else
            return false;
    }

    public void insertIntoUser(){
        referenceToUserItems = FirebaseDatabase.getInstance().getReference("User");
        ItemData addInUser = new ItemData(Name, Qty, Desc, Status, placedBy, purchasedBy);
        referenceToUserItems.child(FirebaseAuth.getInstance().getUid()).child("Item Placed").child(Name).setValue(addInUser);
    }

    private boolean validateItemName() {
        if(Name.isEmpty()){
            ItemName.setError("Item name cannot be empty");
            ItemName.setErrorEnabled(true);
            return false;
        }
        else if(Name.equals("e.g. Rice")){
            ItemName.setError("Enter a valid item name");
            ItemName.setErrorEnabled(true);
            return false;
        }
        else{
            ItemName.setError(null);
            ItemName.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateItemQty() {
        if(Qty.isEmpty()){
            ItemQty.setError("Item quantity cannot be empty");
            ItemQty.setErrorEnabled(true);
            return false;
        }
        else if(Qty.equals("e.g. 2 KG")) {
            ItemQty.setError("Enter a valid quantity");
            ItemQty.setErrorEnabled(true);
            return false;
        }
        else{
            ItemQty.setError(null);
            ItemQty.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateItemDecs() {
        if(Desc.isEmpty()){
            ItemDesc.setError("Item description cannot be empty");
            ItemDesc.setErrorEnabled(true);
            return false;
        }
        else if(Desc.equals("e.g. Basmati Rice")) {
            ItemDesc.setError("Enter a valid description");
            ItemDesc.setErrorEnabled(true);
            return false;
        }
        else{
            ItemDesc.setError(null);
            ItemDesc.setErrorEnabled(false);
            return true;
        }
    }

    public void onBackPressed() {
        Intent goBack = new Intent(getApplicationContext(), HomeScreen.class);
        startActivity(goBack);
        finish();
    }
}