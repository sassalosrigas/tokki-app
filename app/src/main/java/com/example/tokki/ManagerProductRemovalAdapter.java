package com.example.tokki;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;

import com.example.tokki.java.Product;

import java.io.IOException;
import java.util.List;

public class ManagerProductRemovalAdapter extends BaseAdapter {
    private Context context;
    private List<Product> products;

    private ManagerProductRemove productRemoveListener;
    public ManagerProductRemovalAdapter(Context context, List<Product> products, ManagerProductRemove productRemoveListener){
        this.context = context;
        this.products = products;
        this.productRemoveListener = productRemoveListener;

    }

    public interface ManagerProductRemove {
        void onSwitchFlipped(int position) throws IOException, ClassNotFoundException;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.product_item_removal, parent, false);
            holder = new ViewHolder();
            holder.productTitle = convertView.findViewById(R.id.product_title);
            holder.productCategory = convertView.findViewById(R.id.product_category);
            holder.productPrice = convertView.findViewById(R.id.product_price);
            holder.availabilitySwitch = convertView.findViewById(R.id.switch1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = products.get(position);
        holder.productTitle.setText(product.getProductName());
        holder.productCategory.setText(product.getProductType());
        holder.productPrice.setText(String.format("€%.2f", product.getPrice()));
        holder.availabilitySwitch.setOnCheckedChangeListener(null);
        holder.availabilitySwitch.setChecked(true);
        holder.availabilitySwitch.setEnabled(true);

        holder.availabilitySwitch.setOnClickListener(v -> {
            SwitchCompat sw = (SwitchCompat) v;
            sw.setEnabled(false);
            Toast.makeText(context, "Switch toggled and now locked.", Toast.LENGTH_SHORT).show();
        });

        holder.availabilitySwitch.setOnCheckedChangeListener(null);
        holder.availabilitySwitch.setChecked(true);


        holder.availabilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                Toast.makeText(context, "REMOVED", Toast.LENGTH_SHORT).show();
                if(productRemoveListener!=null){
                    try {
                        productRemoveListener.onSwitchFlipped(position);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView productTitle;
        TextView productCategory;
        TextView productPrice;
        SwitchCompat availabilitySwitch;
    }

}