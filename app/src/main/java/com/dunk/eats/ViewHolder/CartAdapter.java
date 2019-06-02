package com.dunk.eats.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.dunk.eats.Interface.ItemClickListener;
import com.dunk.eats.R;
import com.dunk.eats.models.Order;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView txt_cart_name,text_price;
    public ImageView img_cart_count;

    ItemClickListener itemClickListener;

    public TextView getTxt_cart_name() {
        return txt_cart_name;
    }

    public void setTxt_cart_name(TextView txt_cart_name) {
        this.txt_cart_name = txt_cart_name;
    }

    public TextView getText_price() {
        return text_price;
    }

    public void setText_price(TextView text_price) {
        this.text_price = text_price;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);

        txt_cart_name = (TextView)itemView.findViewById(R.id.cart_item_name);
        text_price = (TextView)itemView.findViewById(R.id.cart_item_Price);
        img_cart_count = (ImageView)itemView.findViewById(R.id.cart_item_count);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onclick(v, getAdapterPosition(),false);

    }
}


public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    private List<Order>  listData ;
    private Context context;

    public CartAdapter(List<Order> listData, Context context) {
        this.listData = listData;
        this.context = context;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.cart_layout,viewGroup,false);
        return  new CartViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder cartViewHolder, int i) {
        TextDrawable drawable = TextDrawable.builder().buildRound(""+listData.get(i).getQuantity(), Color.RED);
        cartViewHolder.img_cart_count.setImageDrawable(drawable);

        Locale locale = new Locale("en","US");
        NumberFormat fnt = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt(listData.get(i).getPrice()))*(Integer.parseInt(listData.get(i).getQuantity()));
        cartViewHolder.text_price.setText(fnt.format(price));

        cartViewHolder.txt_cart_name.setText(listData.get(i).getProductName());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}
