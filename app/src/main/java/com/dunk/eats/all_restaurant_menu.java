package com.dunk.eats;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dunk.eats.Common.Common;
import com.dunk.eats.Interface.ItemClickListener;
import com.dunk.eats.ViewHolder.FoodViewHolder;
import com.dunk.eats.models.Food;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class all_restaurant_menu extends AppCompatActivity {

    @BindView(R.id.all_list)
    RecyclerView listall;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference foodlist;

    @BindView(R.id.foodlist) SwipeRefreshLayout swipeRefreshLayout;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_restaurant_menu);
        ButterKnife.bind(this);

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        actionBar.setCustomView(R.layout.abs_layout);

        //firebase
        database = FirebaseDatabase.getInstance();
        foodlist = database.getReference("Foods");

        //loadfoodlist
        listall.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listall.setLayoutManager(layoutManager);


        //swipe refresh layout
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //check internet connection
                if (Common.isConnectedInternet(getBaseContext()) == true){
                    loodLFoodList();
                }
                else{
                    Toast.makeText(getBaseContext(), "Check Internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                //check internet connection
                if (Common.isConnectedInternet(getBaseContext()) == true){
                    loodLFoodList();
                }
                else{
                    Toast.makeText(getBaseContext(), "Check Internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        loodLFoodList();
    }

    private void loodLFoodList(){

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Foods");


        FirebaseRecyclerOptions<Food> options =
                new FirebaseRecyclerOptions.Builder<Food>()
                        .setQuery(query, new SnapshotParser<Food>() {
                            @NonNull
                            @Override
                            public Food parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Food(
                                        snapshot.child("description").getValue().toString(),
                                        snapshot.child("discount").getValue().toString(),
                                        snapshot.child("image").getValue().toString(),
                                        snapshot.child("menuId").getValue().toString(),
                                        snapshot.child("name").getValue().toString(),
                                        snapshot.child("price").getValue().toString());

                            }
                        }).build();


        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_menu_items, parent, false);

                return new FoodViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(final FoodViewHolder viewHolder, final int position, final Food model) {
                viewHolder.all_menu_name.setText(model.getName());
                viewHolder.all_menu_description.setText(model.getDescription());
                viewHolder.all_menu_price.setText("Ksh: "+model.getPrice()+".00");
                Picasso.get().load(model.getImage()).into(viewHolder.all_menue_image);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onclick(View view, int position, boolean isLongClick) {
                        //start activity to navigate to food details page
                        Intent intent = new Intent(all_restaurant_menu.this, FoodDetail.class);
                        intent.putExtra("FoodId", adapter.getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }

        };
        adapter.startListening();
        listall.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }


}
