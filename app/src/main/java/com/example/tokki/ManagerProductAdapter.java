package com.example.tokki;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tokki.java.Manager;
import com.example.tokki.java.Product;
import com.example.tokki.java.Store;

import java.util.List;

public class ManagerProductAdapter extends BaseAdapter {
    private Context context;
    private List<Product> products;

    public ManagerProductAdapter(Context context, List<Product> products){
        this.context = context;
        this.products = products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.product_item_manager, parent, false);
            holder = new ViewHolder();
            holder.productTitle = convertView.findViewById(R.id.product_title);
            holder.productCategory = convertView.findViewById(R.id.product_category);
            holder.productPrice = convertView.findViewById(R.id.product_price);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = products.get(position);
        holder.productTitle.setText(product.getProductName());
        holder.productCategory.setText(product.getProductType());
        holder.productPrice.setText(String.format("€%.2f", product.getPrice()));

        return convertView;
    }

    static class ViewHolder {
        TextView productTitle;
        TextView productCategory;
        TextView productPrice;
    }

}