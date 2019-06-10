package com.dunk.eats;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dunk.eats.Common.Common;
import com.dunk.eats.Interface.ItemClickListener;
import com.dunk.eats.ViewHolder.MenuViewHolder;
import com.dunk.eats.models.Category;
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

public class all_categories extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference category;
    TextView txtFullName;
    @BindView(R.id.all_categories)
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    @BindView(R.id.all_categories_refresh) SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_categories);
        ButterKnife.bind(this);

        //firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        //load menu
        recycler_menu.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2){
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        };
        recycler_menu.setLayoutManager(gridLayoutManager);

        //swipe refresh layout
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //check internet connection then load menue
                if (Common.isConnectedInternet(getBaseContext()) == true){
                    loadmenue();
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
                //check internet connection then load menue
                if (Common.isConnectedInternet(getBaseContext()) == true){
                    loadmenue();
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
        loadmenue();
    }

    private void loadmenue(){

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("categories");

        FirebaseRecyclerOptions<Category> options =
                new FirebaseRecyclerOptions.Builder<Category>()
                        .setQuery(query, new SnapshotParser<Category>() {
                            @NonNull
                            @Override
                            public Category parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Category(
                                        snapshot.child("image").getValue().toString(),
                                        snapshot.child("name").getValue().toString());

                            }
                        }).build();


        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_categories_layout, parent, false);

                return new MenuViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(MenuViewHolder viewHolder, final int position, Category model) {
                viewHolder.all_category_name.setText(model.getImage());
                Picasso.get().load(model.getName()).into(viewHolder.all_category_image);

                viewHolder.setItemClickListener(new ItemClickListener(){
                    @Override
                    public void onclick(View view, int position, boolean isLongClick) {
                        //get category id when user clicks and send to new activity
                        Intent intent = new Intent(all_categories.this, FoodList.class);
                        intent.putExtra("CategoryId", adapter.getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }
        };
        adapter.startListening();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Common.isConnectedInternet(this))
            adapter.stopListening();
    }
}
