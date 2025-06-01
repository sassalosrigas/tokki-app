package com.example.tokki;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class StatisticsProductAdapter extends BaseAdapter {
    private Context context;
    private Map<String, Integer> productSalesMap;
    private String func;

    public StatisticsProductAdapter(Context context, Map<String, Integer> productSalesMap, String func) {
        this.context = context;
        this.productSalesMap = productSalesMap;
        this.func = func;
    }

    public void setProductSalesMap(Map<String, Integer> productSalesMap) {
        this.productSalesMap = productSalesMap;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return productSalesMap != null ? productSalesMap.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        // Convert map entry to item
        return productSalesMap.entrySet().toArray()[position];
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

        Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) getItem(position);

        if (func.equals("product_in_store")) {
            holder.productTitle.setText(entry.getKey());
            holder.productPrice.setText(String.valueOf(entry.getValue()));
        }
        holder.productCategory.setText("sales:");

        return convertView;
    }

    static class ViewHolder {
        TextView productTitle;
        TextView productCategory;
        TextView productPrice;
    }
}