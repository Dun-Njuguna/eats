package com.dunk.eats;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dunk.eats.Common.Common;
import com.dunk.eats.Interface.ItemClickListener;
import com.dunk.eats.ViewHolder.OrderViewHolder;
import com.dunk.eats.models.Order;
import com.dunk.eats.models.Request;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderStatus extends AppCompatActivity {

    @BindView(R.id.listOrders)
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference requests;
    String phone;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);
        ButterKnife.bind(this);

        //firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        if (Common.isConnectedInternet(this) == true){
            phone = Common.currentUser.getPhone();
            if (getIntent() == null)
                loadOrders(getIntent().getStringExtra("userPhone"));
            else
                loadOrders(phone);
        }
        else{
            Toast.makeText(this, "Check Internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void loadOrders(final String phone) {

        Query query = requests.orderByChild("phone").equalTo(phone);


        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(query, new SnapshotParser<Request>() {
                            @NonNull
                            @Override
                            public Request parseSnapshot(@NonNull DataSnapshot snapshot) {
                                List<Order> tfoods = (ArrayList<Order>) snapshot.child("foods").getValue();
                                System.out.println(tfoods);


                                String address = snapshot.child("address").getValue().toString();
                                String name = snapshot.child("name").getValue().toString();
                                String phone =  snapshot.child("phone").getValue().toString();
                                String status =  snapshot.child("status").getValue().toString();
                                String total =  snapshot.child("total").getValue().toString();
                                System.out.println(total);


                                return new Request(phone, name, address, total, status, tfoods);
                            }

                        }).build();


        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_layout, parent, false);

                return new OrderViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(OrderViewHolder viewHolder, final int position, Request model) {
                viewHolder.txtOrderId.setText("Order Id: " +"#" + adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText("Status: " + Common.convertCodeToStatus(model.getStatus()));
                System.out.println(model.getTotal());
                viewHolder.txtOrderPhone.setText("Phone: " + model.getPhone());
                viewHolder.txtOrderAddress.setText("Address: " + model.getAddress());

                System.out.println(model.getStatus());
                System.out.println(model.getTotal());
                System.out.println(model.getName());
                System.out.println(model.getAddress());
                System.out.println(model.getFoods());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onclick(View view, int position, boolean isLongClick) {

                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }





    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}