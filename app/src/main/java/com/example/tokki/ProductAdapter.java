package com.example.tokki;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.tokki.java.Product;

import java.util.List;

public class ProductAdapter extends BaseAdapter {

    private Context context;
    private List<Product> products;
    private OnQuantityChangeListener quantityChangeListener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged(int position, int newQuantity);
    }

    public ProductAdapter(Context context, List<Product> products, OnQuantityChangeListener listener) {
        this.context = context;
        this.products = products;
        this.quantityChangeListener = listener;
    }

    @Override
    public int getCount() {
        return products != null ? products.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
            holder = new ViewHolder();
            holder.productTitle = convertView.findViewById(R.id.product_title);
            holder.productCategory = convertView.findViewById(R.id.product_category);
            holder.productPrice = convertView.findViewById(R.id.product_price);
            holder.buttonDecrease = convertView.findViewById(R.id.button_decrease);
            holder.buttonIncrease = convertView.findViewById(R.id.button_increase);
            holder.textQuantity = convertView.findViewById(R.id.text_quantity);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = products.get(position);
        holder.productTitle.setText(product.getProductName());
        holder.productCategory.setText(product.getProductType());
        holder.productPrice.setText(String.format("€%.2f", product.getPrice()));

        holder.textQuantity.setText("0");

        holder.buttonIncrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(holder.textQuantity.getText().toString());
            quantity++;
            holder.textQuantity.setText(String.valueOf(quantity));
            if (quantityChangeListener != null) {
                quantityChangeListener.onQuantityChanged(position, quantity);
            }
        });

        holder.buttonDecrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(holder.textQuantity.getText().toString());
            if (quantity > 0) {
                quantity--;
                holder.textQuantity.setText(String.valueOf(quantity));
                if (quantityChangeListener != null) {
                    quantityChangeListener.onQuantityChanged(position, quantity);
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView productTitle;
        TextView productCategory;
        TextView productPrice;
        ImageButton buttonDecrease;
        ImageButton buttonIncrease;
        TextView textQuantity;
    }
}