package com.tochange.yang.sector.background;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.tochange.yang.sector.R;

public class LaunchAppAdapter extends BaseAdapter
{
    private LayoutInflater inflater;

    private List<LoadLaunchAppData> mlist;

    class SelectViewHolder
    {
        CheckBox check;

        TextView text;

        ImageView image;
    }

    public LaunchAppAdapter(Context context, List<LoadLaunchAppData> alllist)
    {
        this.inflater = LayoutInflater.from(context);
        this.mlist = alllist;
    }

    @Override
    public int getCount()
    {
        return mlist == null ? 0 : mlist.size();
    }

    @Override
    public Object getItem(int position)
    {
        return this.mlist.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        SelectViewHolder viewHolder = null;
        Listener listener = null;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.list_item, null);
            viewHolder = new SelectViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
            viewHolder.check = (CheckBox) convertView.findViewById(R.id.check);
            listener = new Listener();
            viewHolder.check.setOnClickListener(listener);
            convertView.setTag(viewHolder.check.getId(), listener);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (SelectViewHolder) convertView.getTag();
            listener = (Listener) convertView.getTag(viewHolder.check.getId());
        }

        final LoadLaunchAppData tmp = mlist.get(position);
        // log.e(tmp.appName);
        viewHolder.text.setText((String) tmp.appName);
        viewHolder.image.setBackgroundDrawable(tmp.appIcon);
        listener.setItem(tmp);
        viewHolder.check.setChecked(tmp.choosed);

        return convertView;
    }

    class Listener implements View.OnClickListener
    {
        LoadLaunchAppData tmp;

        public void setItem(LoadLaunchAppData tmp)
        {
            this.tmp = tmp;
        }

        @Override
        public void onClick(View arg0)
        {
            tmp.choosed = !tmp.choosed;
        }

    }
}
