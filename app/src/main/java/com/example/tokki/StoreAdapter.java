package com.example.tokki;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tokki.java.Store;

import java.util.List;

public class StoreAdapter extends BaseAdapter {

    private Context context;
    private List<Store> stores;

    public StoreAdapter(Context context, List<Store> stores) {
        this.context = context;
        this.stores = stores;
    }

    public void setStores(List<Store> stores) {
        this.stores = stores;
    }

    @Override
    public int getCount() {
        return stores != null ? stores.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return stores.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.store_item, parent, false);
            holder = new ViewHolder();
            holder.storeName = convertView.findViewById(R.id.store_name);
            holder.storeCategory = convertView.findViewById(R.id.store_category);
            holder.storeRating = convertView.findViewById(R.id.store_rating);
            holder.storeLogo = convertView.findViewById(R.id.store_logo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Store store = stores.get(position);
        holder.storeName.setText(store.getStoreName());
        holder.storeCategory.setText(store.getFoodCategory());
        holder.storeRating.setText(String.format("★ %.1f", store.getStars()));
        String logoName = store.getStoreLogo(); // e.g. "logo_mcdonalds"
        int resId = context.getResources().getIdentifier(logoName, "drawable", context.getPackageName());
        holder.storeLogo.setImageResource(resId);

        return convertView;
    }

    static class ViewHolder {
        TextView storeName;
        TextView storeCategory;
        TextView storeRating;
        ImageView storeLogo;
    }
}