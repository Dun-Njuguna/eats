package com.dunk.eats;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dunk.eats.Common.Common;
import com.dunk.eats.Database.Database;
import com.dunk.eats.Interface.ItemClickListener;
import com.dunk.eats.ViewHolder.FoodViewHolder;
import com.dunk.eats.ViewHolder.MenuViewHolder;
import com.dunk.eats.models.Food;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

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
    FirebaseRecyclerAdapter<Food, FoodViewHolder> firebaseRecyclerAdapter;



    //search functionality
    List<String> suggestList = new ArrayList<>();
    @BindView(R.id.search_bar)
    MaterialSearchBar materialSearchBar;

    //favourites localdb
    Database localdb;

    //facebook share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    @BindView(R.id.foodlist_refresh) SwipeRefreshLayout swipeRefreshLayout;


    //Create target from picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Create photo from Bitmap
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();

            if (ShareDialog.canShow(SharePhotoContent.class)){

                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }

        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        ButterKnife.bind(this);

        //firebase
        database = FirebaseDatabase.getInstance();
        foodlist = database.getReference("Foods");

        //local database
        localdb = new Database(this);

        //init facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);


        //loadfoodlist
        recycler_food.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_food.setLayoutManager(layoutManager);

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
                    loadSuggestions();
                    loodLFoodList(categoryId);
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
                    loadSuggestions();
                    loodLFoodList(categoryId);
                }
                else{
                    Toast.makeText(getBaseContext(), "Check Internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


        //recieving intent with category id
        if (getIntent() != null) {
            categoryId = getIntent().getStringExtra("CategoryId");
        }

        if (!categoryId.isEmpty() && categoryId != null) {
            loodLFoodList(categoryId);
        }


        //search
        materialSearchBar.setHint("search");
        loadSuggestions();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //update search on user input
                List<String> suggest = new ArrayList<String>();
                for (String search:suggestList){
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase())) {
                        suggest.add(search);
                    }
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                // when search bar is closed leave previous adapter on recyclerview
                if (!enabled)
                    recycler_food.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //when search is confiremed start search
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
    }

    private void startSearch(CharSequence text) {

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Foods").orderByChild("name").equalTo(text.toString());


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


       firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);

                return new FoodViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(FoodViewHolder viewHolder, final int position, Food model) {
                System.out.println(model.getName());
                viewHolder.food_name.setText(model.getName());
                Picasso.get().load(model.getImage()).into(viewHolder.food_image);
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onclick(View view, int position, boolean isLongClick) {

                        Intent intent = new Intent(FoodList.this, FoodDetail.class);
                        intent.putExtra("FoodId", firebaseRecyclerAdapter.getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }

        };
        recycler_food.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }




    //load suggestions from firebase
    private void loadSuggestions() {


        foodlist.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    Food post = postSnapshot.getValue(Food.class);
                    suggestList.add(post.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
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
                        .inflate(R.layout.food_item, parent, false);

                return new FoodViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(final FoodViewHolder viewHolder, final int position, final Food model) {
                viewHolder.food_name.setText(model.getName());
                Picasso.get().load(model.getImage()).into(viewHolder.food_image);

                //Add favourites
                if (localdb.isFavourites(adapter.getRef(position).getKey()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

                //click to share
                viewHolder.btnShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.get()
                                .load(model.getImage())
                                .into(target);

                    }
                });


                //click to change status of favourites
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!localdb.isFavourites(adapter.getRef(position).getKey())){
                            localdb.addToFavourites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this, "" + model.getName() + " was added to favourites", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            localdb.removeFromFavourites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this, "" + model.getName() + " was removed from favourites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onclick(View view, int position, boolean isLongClick) {
                        //start activity to navigate to food details page
                        Intent intent = new Intent(FoodList.this, FoodDetail.class);
                        intent.putExtra("FoodId", adapter.getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }

        };
        adapter.startListening();
        recycler_food.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}

