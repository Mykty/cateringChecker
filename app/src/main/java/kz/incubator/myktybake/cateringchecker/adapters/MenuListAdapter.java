package kz.incubator.myktybake.cateringchecker.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import kz.incubator.myktybake.cateringchecker.R;
import kz.incubator.myktybake.cateringchecker.module.PMenu;

public class MenuListAdapter extends RecyclerView.Adapter<MenuListAdapter.MyViewHolder> {

    private ArrayList<PMenu> dataSet;
    Activity activity;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView desc;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.title  = itemView.findViewById(R.id.mTitle);
            this.desc   = itemView.findViewById(R.id.mDesc);

        }
    }

    public MenuListAdapter(Activity activity, ArrayList<PMenu> data) {
        this.dataSet = data;
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_list, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        PMenu menu = dataSet.get(position);
        holder.title.setText(menu.getTitle());
        holder.desc.setText(menu.getDesc());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void removeItem(int position) {
        dataSet.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(PMenu menu, int position) {
        dataSet.add(position, menu);
        notifyItemInserted(position);
    }

}