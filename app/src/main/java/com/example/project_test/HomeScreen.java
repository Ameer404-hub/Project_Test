package com.example.project_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project_test.Models.ItemData;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class HomeScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView Greetings, dateTimeDisplay;
    String FnameDB = "", whoJoinedMe = "", date;
    Button ItemListBtn, PlaceItemBtn;
    ArrayList<String> ItemList;
    ArrayAdapter adapter;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    Toolbar toolbar;
    DrawerLayout drawer;
    Handler handler;
    ListView HomeScreenList;
    static final float END_SCALE = 0.7f;
    LinearLayout contentView;
    int myItemCount = 0, OtherItemCount = 0;
    ProgressDialog message;
    Calendar calendar;
    SimpleDateFormat dateFormat, Time;

    DatabaseReference referenceToUser, referenceToItems, referenceToOM, referenceToOthers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        referenceToUser = FirebaseDatabase.getInstance().getReference("User")
                .child(FirebaseAuth.getInstance().getUid()).child("ProfileInfo");
        referenceToItems = FirebaseDatabase.getInstance().getReference("User").
                child(FirebaseAuth.getInstance().getUid()).child("Item Placed");
        referenceToOthers = FirebaseDatabase.getInstance().getReference("User").
                child(FirebaseAuth.getInstance().getUid()).child("Other Members");

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        contentView = findViewById(R.id.HomeScreenLayout);
        ItemListBtn = (Button) findViewById(R.id.itemList);
        PlaceItemBtn = (Button) findViewById(R.id.placeitem);
        Greetings = findViewById(R.id.welcome);
        HomeScreenList = findViewById(R.id.homeList);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
        Time = new SimpleDateFormat("hh:mm aaa");
        date = dateFormat.format(calendar.getTime());
        dateTimeDisplay = (TextView)findViewById(R.id.dateTime);
        dateTimeDisplay.setText(date);

        ItemList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, R.layout.item_list, ItemList);
        HomeScreenList.setAdapter(adapter);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        message = new ProgressDialog(HomeScreen.this);
        message.setTitle("");
        message.setMessage("Loading...");
        message.show();
        message.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        if(isConnected(HomeScreen.this)){
            referenceToUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        FnameDB = snapshot.child("firstName").getValue().toString();
                        Greet();
                    }
                    else {
                        ItemList.clear();
                        Greet();
                        ItemList.add("User data not found :(");
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeScreen.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            referenceToItems.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ItemList.clear();
                        if(snapshot.exists()){
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ItemData items = ds.getValue(ItemData.class);
                                String itemName = items.getName();
                                ItemList.add(itemName);
                                myItemCount++;
                            }
                        }
                        else myItemCount = 0;
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeScreen.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            referenceToOthers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                long totalChildren = snapshot.getChildrenCount();
                                String ID = ds.getKey();
                                whoJoinedMe = ds.getValue().toString();
                                FirebaseMessaging.getInstance().subscribeToTopic(whoJoinedMe);
                                referenceToOM = FirebaseDatabase.getInstance().getReference("User")
                                        .child(ID).child("Item Placed");
                                referenceToOM.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                ItemData OM = ds.getValue(ItemData.class);
                                                String itemName = OM.getName();
                                                if(ItemList.contains(itemName)){
                                                }
                                                else{
                                                    message.dismiss();
                                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                    ItemList.add(itemName);
                                                }
                                            }
                                        }
                                        else{
                                            OtherItemCount++;
                                            if (OtherItemCount == totalChildren && myItemCount == 0)
                                                ItemList.add("Go Easy! Nothing to Purchase Right Now");
                                            message.dismiss();
                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        }
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(HomeScreen.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        }
                        else{
                            if (myItemCount == 0)
                                ItemList.add("Go Easy! Nothing to Purchase Right Now");
                            adapter.notifyDataSetChanged();
                            message.dismiss();
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeScreen.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        }
        else {
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    message.dismiss();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    ItemList.clear();
                    Greet();
                    ItemList.add("Ops! No internet connection");
                    adapter.notifyDataSetChanged();
                }
            }, 1000);
        }

        animateNavigationDrawer();

        ItemListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent PlaceItemActivity = new Intent(getApplicationContext(), PurchaseList.class);
                startActivity(PlaceItemActivity);
                finish();
            }
        });

        PlaceItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent PlaceItemActivity = new Intent(getApplicationContext(), PlaceItems.class);
                startActivity(PlaceItemActivity);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerVisible(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    public void Greet(){
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        if(timeOfDay >= 0 && timeOfDay < 12){
            Greetings.setText("Good Morning " + FnameDB + "!");
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            Greetings.setText("Good Afternoon " + FnameDB + "!");
        }else if(timeOfDay >= 16 && timeOfDay < 21){
            Greetings.setText("Good Evening " + FnameDB + "!");
        }else if(timeOfDay >= 21 && timeOfDay < 24){
            Greetings.setText("Good Night " + FnameDB + "!");
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem menuitem) {
        if (menuitem.getItemId() == R.id.editProfile) {
            drawer.closeDrawer(GravityCompat.START);
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent Home = new Intent(getApplicationContext(), EditProfileActivity.class);
                    startActivity(Home);
                    finish();
                }
            }, 350);
        }
        if (menuitem.getItemId() == R.id.joinOthers) {
            drawer.closeDrawer(GravityCompat.START);
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent Home = new Intent(getApplicationContext(), JoinOthers.class);
                    startActivity(Home);
                    finish();
                }
            }, 350);
        }
        if (menuitem.getItemId() == R.id.quickTutorial) {
            drawer.closeDrawer(GravityCompat.START);
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent Home = new Intent(getApplicationContext(), QuickTutorial.class);
                    startActivity(Home);
                    finish();
                }
            }, 350);
        }
        if (menuitem.getItemId() == R.id.about) {
            drawer.closeDrawer(GravityCompat.START);
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent Home = new Intent(getApplicationContext(), About.class);
                    startActivity(Home);
                    finish();
                }
            }, 350);
        }
        if (menuitem.getItemId() == R.id.otherMembers) {
            drawer.closeDrawer(GravityCompat.START);
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent Home = new Intent(getApplicationContext(), OtherMembers.class);
                    startActivity(Home);
                    finish();
                }
            }, 350);
        }
        return true;
    }

    private void animateNavigationDrawer() {

        //Add any color or remove it to use the default one!
        //To make it transparent use Color.Transparent in side setScrimColor();
        //drawer.setScrimColor(getResources().getColor(R.color.ButtonColorPrimary));
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                // Scale the View based on current slide offset
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                contentView.setScaleX(offsetScale);
                contentView.setScaleY(offsetScale);

                // Translate the View, accounting for the scaled width
                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                contentView.setTranslationX(xTranslation);
            }
        });
    }

    private boolean isConnected(HomeScreen CheckInternet){
        ConnectivityManager connectivityManager = (ConnectivityManager) CheckInternet.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileCon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if( (wifiCon!=null && wifiCon.isConnected()) || (mobileCon!=null && mobileCon.isConnected()) )
            return true;
        else
            return false;
    }
}