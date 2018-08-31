package kz.incubator.myktybake.cateringchecker.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kz.incubator.myktybake.cateringchecker.R;
import kz.incubator.myktybake.cateringchecker.adapters.OrderListAdapter;
import kz.incubator.myktybake.cateringchecker.adapters.RecyclerItemClickListener;
import kz.incubator.myktybake.cateringchecker.module.Order;

public class OrderFragment extends Fragment{
    View view;
    RecyclerView orderListView;
    private RecyclerView.LayoutManager linearLayoutManager;
    OrderListAdapter orderListAdapter;
    ArrayList<Order> orderList;
    ArrayList<String> menu;
    DatabaseReference mDatabaseRef;
    Dialog orderRequestDialog;
    RelativeLayout relativeLayout;
    Intent menuIntent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_order, container, false);
        getActivity().setTitle("Тапсырыстар");
        setupViews();
        refreshOrders();
        return view;
    }

    public void refreshOrders() {
        Query query = mDatabaseRef.child("orders").orderByChild("date");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                orderList.clear();

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {

                    Order order = orderSnapshot.getValue(Order.class);
                    orderList.add(order);
                }

                orderListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    int pos = 0;
    public void setupViews() {

        FirebaseApp.initializeApp(getActivity());
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        orderListView = view.findViewById(R.id.orderList);
        orderListView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());

        relativeLayout = view.findViewById(R.id.relaviteL);
        orderListView.setLayoutManager(linearLayoutManager);
        orderListView.setItemAnimator(new DefaultItemAnimator());

        menu = new ArrayList<>();
        createRequestDialog();
        menuIntent = new Intent(getActivity(), AddMenuInOrderActivity.class);

        orderList = new ArrayList<>();
        orderListAdapter = new OrderListAdapter(getActivity(), orderList);
        orderListView.setAdapter(orderListAdapter);

        orderListView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), orderListView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        Order cOrder = orderList.get(position);
                        /*pos = position;
                        orderRequestDialog.setTitle("Тапсырыс: "+orderList.get(position).getTitle());
                        orderRequestDialog.show();*/

                        menuIntent.putExtra("title", cOrder.getTitle());
                        menuIntent.putExtra("orderPersonName", cOrder.getOrderPersonName());
                        menuIntent.putExtra("dateEditText", cOrder.getDate());
                        menuIntent.putExtra("time", cOrder.getTime());
                        menuIntent.putExtra("personCount", cOrder.getPersonCount());
                        menuIntent.putExtra("phoneNumber", cOrder.getPhoneNumber());

                        menuIntent.putExtra("status", cOrder.getStatus());
                        menuIntent.putExtra("menu", cOrder.getMenu());
                        menuIntent.putExtra("keys", cOrder.getKeys());

                        getActivity().startActivity(menuIntent);

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                })
        );

    }

    public void createRequestDialog(){
        orderRequestDialog = new Dialog(getActivity());
        orderRequestDialog.setContentView(R.layout.dialog_order_request);
        Button btnAccepted = orderRequestDialog.findViewById(R.id.btnAccepted);
        Button btnFinish = orderRequestDialog.findViewById(R.id.btnFinish);

        btnAccepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabaseRef.child("orders").child(orderList.get(pos).getKeys()).child("status").setValue("қабылданды");
                orderRequestDialog.dismiss();

            }
        });
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseRef.child("orders").child(orderList.get(pos).getKeys()).child("status").setValue("дайын");
                orderRequestDialog.dismiss();
            }
        });
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
