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
import com.dunk.eats.models.Request;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.annotations.NotNull;

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

        phone = Common.currentUser.getPhone();

        loadOrders(phone);
    }

    private void loadOrders(final String phone) {

        Query query = requests.orderByChild("phone").equalTo(phone);


        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(query, new SnapshotParser<Request>() {
                            @NonNull
                            @Override
                            public Request parseSnapshot(@NonNull DataSnapshot snapshot){
                                return new Request(
                                    snapshot.child("address").getValue().toString(),
                                    snapshot.child("phone").getValue().toString());
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
            protected void onBindViewHolder(OrderViewHolder viewHolder, final int position, Request request) {
                viewHolder.txtOrderId.setText("Order Id: " +"#" + adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText("Status: " + convertCodeToStatus(request.getStatus()));
                viewHolder.txtOrderPhone.setText("Phone: " + request.getPhone());
                viewHolder.txtOrderAddress.setText("Address: " + request.getAddress());


                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onclick(View view, int position, boolean isLongClick) {
//                        Toast.makeText(OrderStatus.this, "clicked", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        };
        recyclerView.setAdapter(adapter);
    }

    private String convertCodeToStatus(String status) {
        if (status != null && status.equals("0"))
            return "Placed";
        else if (status != null && status.equals("1"))
            return "On my way";
        else
            return "Shipped";
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