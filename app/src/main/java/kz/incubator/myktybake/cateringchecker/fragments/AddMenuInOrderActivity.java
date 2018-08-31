package kz.incubator.myktybake.cateringchecker.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kz.incubator.myktybake.cateringchecker.R;
import kz.incubator.myktybake.cateringchecker.adapters.MenuListAdapter;
import kz.incubator.myktybake.cateringchecker.module.Order;
import kz.incubator.myktybake.cateringchecker.module.PMenu;

public class AddMenuInOrderActivity extends AppCompatActivity implements View.OnClickListener {
    RecyclerView menuListView;
    LinearLayoutManager linearLayoutManager;
    MenuListAdapter menuListAdapter;
    ArrayList<PMenu> menuList;
    TextView title;
    TextView orderPersonName;
    TextView date;
    TextView status;
    TextView personCount;
    TextView phoneNumber;
    DatabaseReference mDatabaseRef;
    String keyText;
    Button btnAccepted, btnFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu);
        setupViews();
        updateViews();

    }

    public void updateViews() {
        Intent menuIntent = getIntent();

        keyText = menuIntent.getStringExtra("keys");
        mDatabaseRef.child("orders").child(keyText).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Order order = dataSnapshot.getValue(Order.class);

                title.setText(order.getTitle());
                orderPersonName.setText(order.getOrderPersonName());
                date.setText(order.getDate() + ", " + order.getTime());
                personCount.setText(order.getPersonCount() + " адам");
                phoneNumber.setText(order.getPhoneNumber());
                status.setText(order.getStatus());

                if (status.getText().equals("жаңа"))
                    status.setTextColor(getResources().getColor(R.color.red));
                if (status.getText().equals("қабылданды"))
                    status.setTextColor(getResources().getColor(R.color.tabBack2));
                if (status.getText().equals("дайын"))
                    status.setTextColor(getResources().getColor(R.color.green));

                String updatedMenu = order.getMenu();

                String menuSplit[] = updatedMenu.split("title: ");
                menuList.clear();

                for (int i = 1; i < menuSplit.length; i++) {
                    String descsplit[] = menuSplit[i].split("desc: ");
                    System.out.println("title: " + descsplit[0]);
                    System.out.println("desc: " + descsplit[1]);
                    menuList.add(new PMenu(descsplit[0], descsplit[1]));
                }

                menuListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void setupViews() {
        menuListView = findViewById(R.id.menuRecycleVIew);
        menuListView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);

        menuListView.setLayoutManager(linearLayoutManager);
        menuListView.setItemAnimator(new DefaultItemAnimator());

        menuList = new ArrayList<PMenu>();
        menuListAdapter = new MenuListAdapter(this, menuList);
        menuListView.setAdapter(menuListAdapter);

        FirebaseApp.initializeApp(this);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        title = findViewById(R.id.oTitle);
        orderPersonName = findViewById(R.id.oPerson);
        status = findViewById(R.id.oStatus);
        date = findViewById(R.id.oDate);
        personCount = findViewById(R.id.oCount);
        phoneNumber = findViewById(R.id.oPhoneNumber);

        btnAccepted = findViewById(R.id.btnAccepted);
        btnFinished = findViewById(R.id.btnFinished);
        btnAccepted.setOnClickListener(this);
        btnFinished.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAccepted:

                mDatabaseRef.child("orders").child(keyText).child("status").setValue("қабылданды");
                onBackPressed();

                break;

            case R.id.btnFinished:

                mDatabaseRef.child("orders").child(keyText).child("status").setValue("дайын");
                onBackPressed();

                break;

        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}
