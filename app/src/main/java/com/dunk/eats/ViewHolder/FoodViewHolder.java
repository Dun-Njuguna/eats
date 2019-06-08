package com.dunk.eats.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dunk.eats.Interface.ItemClickListener;
import com.dunk.eats.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView food_name, all_menu_name, all_menu_description, all_menu_price;
    public ImageView food_image, fav_image, btnShare, all_menue_image;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;

    }

    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);

        food_name = (TextView)itemView.findViewById(R.id.food_name);
        food_image = (ImageView)itemView.findViewById(R.id.food_image);
        fav_image = (ImageView)itemView.findViewById(R.id.fav);
        btnShare = (ImageView)itemView.findViewById(R.id.btnShare);
        all_menu_name = (TextView)itemView.findViewById(R.id.all_menu_name);
        all_menu_description = (TextView)itemView.findViewById(R.id.all_menu_description);
        all_menu_price = (TextView)itemView.findViewById(R.id.all_menu_price);
        all_menue_image = (ImageView)itemView.findViewById(R.id.all_menu_image);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onclick(view, getAdapterPosition(),false);

    }
}
