package com.dunk.eats;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.dunk.eats.Database.Database;
import com.dunk.eats.ViewHolder.CartAdapter;
import com.dunk.eats.models.Order;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.hoang8f.widget.FButton;

public class Cart extends AppCompatActivity {

    @BindView(R.id.listCart) RecyclerView recyclerView;
    @BindView(R.id.total) TextView textTotalPrice;
    @BindView(R.id.btnPlaceOrder) FButton btnPlace;
    
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference request;

    List<Order> carts = new ArrayList<>();
    CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        ButterKnife.bind(this);

        //firebase
        database = FirebaseDatabase.getInstance();
        request = database.getReference("Requests");

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadListFood();

    }

    private void loadListFood() {

    carts = new Database(this).getCarts();
    adapter = new CartAdapter(carts,this);
    recyclerView.setAdapter(adapter);

    int total = 0;
    for (Order order:carts){
        total+=(Integer.parseInt(order.getPrice())*(Integer.parseInt(order.getQuantity())));

        Locale locale = new Locale("en","Kenya");
        NumberFormat fnt = NumberFormat.getCurrencyInstance(locale);

        textTotalPrice.setText(fnt.format(total));

    }
    }
}
