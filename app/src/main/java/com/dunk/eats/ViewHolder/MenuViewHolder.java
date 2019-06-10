package com.dunk.eats.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dunk.eats.Home;
import com.dunk.eats.Interface.ItemClickListener;
import com.dunk.eats.R;

public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtMenuName, popularTitle, all_category_name;
    public ImageView imageView, popularImage, all_category_image;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;

    }

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);

        txtMenuName = (TextView)itemView.findViewById(R.id.menu_name);
        popularTitle = (TextView)itemView.findViewById(R.id.popularTitle);
        imageView = (ImageView)itemView.findViewById(R.id.menu_image);
        popularImage = (ImageView)itemView.findViewById(R.id.popularImage);
        all_category_name = (TextView)itemView.findViewById(R.id.all_category_name);
        all_category_image = (ImageView)itemView.findViewById(R.id.all_category_image);


        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onclick(v, getAdapterPosition(),false);
    }

}
