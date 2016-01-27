package com.huichongzi.fastwidget4android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huichongzi.fastwidget4android.R;

/**
 * @author chz
 * @description
 * @date 2016/1/27 9:37
 */
public class AnimationListAdapter extends BaseAdapter {
    private int[] imgs = {R.drawable.banner_a, R.drawable.banner_b, R.drawable.banner_c, R.drawable.banner_d, R.drawable.banner_e};

    private Context mContext;

    public AnimationListAdapter(Context context){
        mContext = context;
    }

    @Override
    public int getCount() {
        return imgs.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.animation_listview_item, null);
            holder.img = (ImageView)convertView.findViewById(R.id.animation_listview_item_img);
            holder.text = (TextView)convertView.findViewById(R.id.animation_listview_item_text);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }
        holder.img.setImageResource(imgs[position]);
        holder.text.setText("page " + position);
        return convertView;
    }

    class ViewHolder{
        public ImageView img;
        public TextView text;
    }
}
