package com.local.elderhomehelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class InstalledAppAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final List<AppEntry> apps;

    public InstalledAppAdapter(Context context, List<AppEntry> apps) {
        this.inflater = LayoutInflater.from(context);
        this.apps = apps;
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public AppEntry getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.app_list_item, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.appIcon);
            holder.name = convertView.findViewById(R.id.appName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppEntry app = getItem(position);
        holder.name.setText(app.label);
        holder.icon.setImageDrawable(app.icon);
        return convertView;
    }

    private static class ViewHolder {
        ImageView icon;
        TextView name;
    }
}
