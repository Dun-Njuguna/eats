package com.dunk.eats;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dunk.eats.Common.Common;
import com.dunk.eats.Database.Database;
import com.dunk.eats.Remote.APIService;
import com.dunk.eats.ViewHolder.CartAdapter;
import com.dunk.eats.models.MyResponse;
import com.dunk.eats.models.Notification;
import com.dunk.eats.models.Order;
import com.dunk.eats.models.Request;
import com.dunk.eats.models.Sender;
import com.dunk.eats.models.Token;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart extends AppCompatActivity {

    @BindView(R.id.listCart)
    RecyclerView recyclerView;
    @BindView(R.id.total)
    TextView textTotalPrice;
    @BindView(R.id.btnPlaceOrder)
    FButton btnPlace;
    
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference requests;

    List<Order> carts = new ArrayList<>();
    CartAdapter adapter;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        ButterKnife.bind(this);

        //firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //init service
        mService = Common.getFCMService();

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (carts.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your cart is empty", Toast.LENGTH_SHORT).show();
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
                            "0",
                        carts
                );

                //Submit request to firebase
                //using System.CurrentMilli to key

                String order_number = String.valueOf(System.currentTimeMillis());
                requests.child(order_number).setValue(request);

                sendNotificationOrder(order_number);

                //Delete cart
                new Database(getBaseContext()).cleanCart();
                //refresh ui
                loadListFood();

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

    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        // get all node with isServerToken is true
        Query data = tokens.orderByChild("serverToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    Token token = postSnapshot.getValue(Token.class);
                    System.out.println(token);

                    //create raw payload to send
                    Notification notification = new Notification ("Eats","New order" + order_number);
                    Sender content = new Sender(token.getToken(),notification);

                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success == 1)
                                        {
                                            Toast.makeText(Cart.this, "Order " + order_number + " placed", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                        else
                                        {
                                            Toast.makeText(Cart.this, "Order updated but failed to send notification", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood() {

        carts = new Database(this).getCarts();
        adapter = new CartAdapter(carts,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        int total = 0;
        for (Order order:carts){
            total+=(Integer.parseInt(order.getPrice())*(Integer.parseInt(order.getQuantity())));

            Locale locale = new Locale("en","US");
            NumberFormat fnt = NumberFormat.getCurrencyInstance(locale);

            textTotalPrice.setText(fnt.format(total));

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int order) {
        // remove item from list<order> by position where position is refrenced by order
        carts.remove(order);
        //then delete old data from firebase
        new Database(this).cleanCart();
        //then update database with details from list<order>
        for (Order item: carts)
            new Database(this).addToCart(item);
        //refresh ui
        loadListFood();

    }

}
