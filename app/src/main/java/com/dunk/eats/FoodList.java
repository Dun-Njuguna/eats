package com.dunk.eats;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

public class FoodList extends AppCompatActivity {

    @BindView(R.id.recycler_food)
    RecyclerView recycler_food;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference foodlist;
    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        ButterKnife.bind(this);

        //firebase
        database = FirebaseDatabase.getInstance();
        foodlist = database.getReference("Category");

        //loadfoodlist
        recycler_food.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_food.setLayoutManager(layoutManager);

        //recieving intent with category id
        if (getIntent() != null) {
            categoryId = getIntent().getStringExtra("CategoryId");
        }

        if (!categoryId.isEmpty() && categoryId != null) {
            loodLFoodList(categoryId);
        }
    }



    private void loodLFoodList(String categoryId){

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Foods").orderByChild("menuId").equalTo(categoryId);


        FirebaseRecyclerOptions<Food> options =
                new FirebaseRecyclerOptions.Builder<Food>()
                        .setQuery(query, new SnapshotParser<Food>() {
                            @NonNull
                            @Override
                            public Food parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Food(
                                        snapshot.child("name").getValue().toString(),
                                        snapshot.child("image").getValue().toString());

                            }
                        }).build();


        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);

                return new FoodViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(FoodViewHolder viewHolder, final int position, Food model) {
                viewHolder.food_name.setText(model.getDescription());
                Picasso.get().load(model.getDiscount()).into(viewHolder.food_image);
                final Food clickitem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onclick(View view, int position, boolean isLongClick) {
                        Toast.makeText(FoodList.this, "Clicked", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        };
        recycler_food.setAdapter(adapter);
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

