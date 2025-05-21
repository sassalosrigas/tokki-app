package com.example.tokki;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.tokki.java.Product;

import java.util.List;

public class OrderConfirmationProductAdapter extends BaseAdapter {

    private Context context;
    private List<Product> selectedProducts;
    private List<Integer> quantities;

    public OrderConfirmationProductAdapter(Context context, List<Product> selectedProducts, List<Integer> quantities) {
        this.context = context;
        this.selectedProducts = selectedProducts;
        this.quantities = quantities;
    }

    @Override
    public int getCount() {
        return selectedProducts != null ? selectedProducts.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return selectedProducts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.order_product_item, parent, false);
            holder = new ViewHolder();
            holder.productTitle = convertView.findViewById(R.id.product_title);
            holder.productCategory = convertView.findViewById(R.id.product_category);
            holder.productPrice = convertView.findViewById(R.id.product_price);
            holder.productQuantity = convertView.findViewById(R.id.product_quantity);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = selectedProducts.get(position);
        holder.productTitle.setText(product.getProductName());
        holder.productCategory.setText(product.getProductType());
        holder.productPrice.setText(String.format("\u20AC%.2f", product.getPrice()));
        holder.productQuantity.setText("x" + quantities.get(position));

        return convertView;
    }

    static class ViewHolder {
        TextView productTitle;
        TextView productCategory;
        TextView productPrice;
        TextView productQuantity;
    }
}
