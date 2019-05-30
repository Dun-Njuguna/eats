package com.dunk.eats;

import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.dunk.eats.models.Food;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.Console;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FoodDetail extends AppCompatActivity {

    //initialise view
    @BindView(R.id.image_food_details) ImageView food_image;
    @BindView(R.id.food_name) TextView food_name;
    @BindView(R.id.food_price) TextView foood_price;
    @BindView(R.id.food_description) TextView food_description;
    @BindView(R.id.number_button) ElegantNumberButton numberButton;
    @BindView(R.id.btnCart) FloatingActionButton btnCart;
    CollapsingToolbarLayout collapsingToolbarLayout;


    FirebaseDatabase database;
    DatabaseReference foods;
    String foodId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        ButterKnife.bind(this);


        //firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");

        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //get food id from intent
        if (getIntent() != null) {
            foodId = getIntent().getStringExtra("FoodId");
        }

        if (!foodId.isEmpty()) {
            getFoodDetails(foodId);
        }
    }

    private void getFoodDetails(String foodId) {

        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Food food = dataSnapshot.getValue(Food.class);
                Picasso.get().load(food.getImage()).into(food_image);
                collapsingToolbarLayout.setTitle(food.getName());

                food_name.setText(food.getName());
                foood_price.setText(food.getPrice());
                food_description.setText(food.getDescription());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
