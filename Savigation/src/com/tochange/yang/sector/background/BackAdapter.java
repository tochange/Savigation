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

import com.tochange.yang.log;
import com.tochange.yang.sector.R;
import com.tochange.yang.sector.tools.BackItemInfo;

public class BackAdapter extends BaseAdapter
{
    private LayoutInflater inflater;

    private List<BackItemInfo> mlist;

    class SelectViewHolder
    {
        CheckBox check;

        TextView text;

        ImageView image;
    }

    public BackAdapter(Context context, List<BackItemInfo> alllist)
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
//        log.e(".....");
        if (convertView == null)
        {
            viewHolder = new SelectViewHolder();
            convertView = inflater.inflate(R.layout.list_item, null);
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
            viewHolder.check = (CheckBox) convertView.findViewById(R.id.check);

            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (SelectViewHolder) convertView.getTag();

        viewHolder.text.setText((String) mlist.get(position).name);
        viewHolder.image.setBackgroundResource(mlist.get(position).iconResOn);
        if (mlist.get(position).choosed)
            viewHolder.check.setChecked(true);
        else
            viewHolder.check.setChecked(false);

        viewHolder.check.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                mlist.get(position).choosed = !mlist.get(position).choosed;
            }
        });

        viewHolder.check.setTag(viewHolder);
        return convertView;
    }
}
