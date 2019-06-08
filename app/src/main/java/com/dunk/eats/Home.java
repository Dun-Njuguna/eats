package com.dunk.eats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.dunk.eats.Common.Common;
import com.dunk.eats.Interface.ItemClickListener;
import com.dunk.eats.ViewHolder.MenuViewHolder;
import com.dunk.eats.models.Banner;
import com.dunk.eats.models.Category;
import com.dunk.eats.models.Token;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.glide.slider.library.Animations.DescriptionAnimation;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.BaseSliderView;
import com.glide.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;
    TextView txtFullName;
    @BindView(R.id.recycler_menue) RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    @BindView(R.id.recycler_popular) RecyclerView recycler_popular;


    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter, adapter2;

    String currentUserPhone;

    @BindView(R.id.swipe_layout) SwipeRefreshLayout swipeRefreshLayout;

    //Slider
    HashMap<String,String> image_list;
    @BindView(R.id.slider) SliderLayout mSlider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");


        //firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

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
                    loadPopular();
                    setUpSlider();
                    mSlider.startAutoCycle();
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
                    loadPopular();
                }
                else{
                    Toast.makeText(getBaseContext(), "Check Internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this,Cart.class);
                startActivity(intent);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //setting users name
        View headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView)headerView.findViewById(R.id.txttfullname);
        txtFullName.setText(Common.currentUser.getName());

        //loadpopular
        recycler_popular.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recycler_popular.setLayoutManager(layoutManager);

        //load menu
        recycler_menu.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recycler_menu.setLayoutManager(gridLayoutManager);


        //init paper
        Paper.init(this);


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        System.out.println(token);
                        updateToken(token);
                    }
                });

        //setup slider
        setUpSlider();

    }

    private void setUpSlider() {
        image_list = new HashMap<>();
        final DatabaseReference banners = database.getReference("Banner");
        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren())
                {
                    Banner banner = postSnapshot.getValue(Banner.class);
                    image_list.put(banner.getName() + "_" + banner.getId(), banner.getImage());
                }
                for (String key:image_list.keySet())
                {
                   String[] keySplit = key.split("_");
                   String nameOfFood = keySplit[0];
                   String idOfFood = keySplit[1];

                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.centerCrop();

                   //Create slider
                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView
                            .image(image_list.get(key))
                            .description(nameOfFood)
                            .setRequestOption(requestOptions)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent = new Intent(Home.this, FoodDetail.class);
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);
                                }
                            });
                    //Add extra bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId", idOfFood);
                    mSlider.addSlider(textSliderView);

                    //remove event after finish
                    banners.removeEventListener(this);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation( new DescriptionAnimation());
        mSlider.setDuration(4000);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        loadmenue();
        loadPopular();
        mSlider.startAutoCycle();
    }

    //add token during login
    private void updateToken(String instanceId) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token token1 = new Token(instanceId, false); //false token is sent from client app
        currentUserPhone = Common.currentUser.getPhone();
        tokens.child(currentUserPhone).setValue(token1);
    }

    private void loadPopular(){

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("PopularCategories");

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


        adapter2 = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_popular_item, parent, false);

                return new MenuViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(MenuViewHolder viewHolder, final int position, Category model) {
                viewHolder.popularTitle.setText(model.getImage());
                Picasso.get().load(model.getName()).into(viewHolder.popularImage);

                viewHolder.setItemClickListener(new ItemClickListener(){
                    @Override
                    public void onclick(View view, int position, boolean isLongClick) {
                        //get category id when user clicks and send to new activity
                        Intent intent = new Intent(Home.this, FoodList.class);
                        intent.putExtra("CategoryId", adapter.getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }
        };
        adapter2.startListening();
        recycler_popular.setAdapter(adapter2);
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
                        .inflate(R.layout.menu_item, parent, false);

                return new MenuViewHolder(view);
            }


            @Override
                protected void onBindViewHolder(MenuViewHolder viewHolder, final int position, Category model) {
                viewHolder.txtMenuName.setText(model.getImage());
                Picasso.get().load(model.getName()).into(viewHolder.imageView);

                viewHolder.setItemClickListener(new ItemClickListener(){
                    @Override
                    public void onclick(View view, int position, boolean isLongClick) {
                        //get category id when user clicks and send to new activity
                        Intent intent = new Intent(Home.this, FoodList.class);
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
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.refresh){
            if (Common.isConnectedInternet(this)){
                loadmenue();
                loadPopular();
                setUpSlider();
            }
            else
                Toast.makeText(this, "Check internet connection", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
        } else if (id == R.id.nav_cart) {

            Intent intent = new Intent(this,Cart.class);
            startActivity(intent);

        } else if (id == R.id.nav_order) {

            Intent intent = new Intent(this,OrderStatus.class);
            intent.putExtra("userPhone", "");
            startActivity(intent);

        } else if (id == R.id.nav_log_out) {

            //Delete key-value pairs for login
            Paper.book().destroy();

            Intent sigOut = new Intent(this,SignIn.class);
            sigOut.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(sigOut);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Common.isConnectedInternet(this))
            adapter.stopListening();
            adapter2.stopListening();
            mSlider.stopAutoCycle();
    }


}
