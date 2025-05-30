package com.example.tokki;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class StringListAdapter extends BaseAdapter {

    private final Context context;
    private final List<String> items;
    private final LayoutInflater inflater;

    public StringListAdapter(Context context, List<String> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position; // Or a stable ID if available
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.category_item, parent, false);
            holder = new ViewHolder();
            holder.titleTextView = convertView.findViewById(R.id.product_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String item = items.get(position);
        holder.titleTextView.setText(item);

        return convertView;
    }

    static class ViewHolder {
        TextView titleTextView;
    }
}
