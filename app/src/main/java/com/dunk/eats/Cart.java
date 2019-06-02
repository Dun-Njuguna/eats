package com.dunk.eats;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dunk.eats.Common.Common;
import com.dunk.eats.Database.Database;
import com.dunk.eats.ViewHolder.CartAdapter;
import com.dunk.eats.models.Order;
import com.dunk.eats.models.Request;
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
    DatabaseReference requests;

    List<Order> carts = new ArrayList<>();
    CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        ButterKnife.bind(this);

        //firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });

        loadListFood();

    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step");
        alertDialog.setMessage("Enter your address: ");

        final EditText editAddress = new EditText(Cart.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

       editAddress.setLayoutParams(lp);
       alertDialog.setView(editAddress);//add edit text to alert dialog
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);


        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Create new request

                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        editAddress.getText().toString(),
                        textTotalPrice.getText().toString(),
                            "1",
                        carts
                );

                //Submit request to firebase
                //using System.CurrentMilli to key

                requests.child(String.valueOf(System.currentTimeMillis()))
                        .setValue(request);
                //Delete cart
                new Database(getBaseContext()).cleanCart();

                Toast.makeText(Cart.this, "Order Placed", Toast.LENGTH_SHORT).show();
                finish();

            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }
    private void loadListFood() {

    carts = new Database(this).getCarts();
    adapter = new CartAdapter(carts,this);
    recyclerView.setAdapter(adapter);

    int total = 0;
    for (Order order:carts){
        total+=(Integer.parseInt(order.getPrice())*(Integer.parseInt(order.getQuantity())));

        Locale locale = new Locale("en","US");
        NumberFormat fnt = NumberFormat.getCurrencyInstance(locale);

        textTotalPrice.setText(fnt.format(total));

    }
    }
}
