package kz.incubator.myktybake.cateringchecker.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kz.incubator.myktybake.cateringchecker.R;
import kz.incubator.myktybake.cateringchecker.module.Personnel;

public class EatersListAdapter extends RecyclerView.Adapter<EatersListAdapter.MyViewHolder> {

    private ArrayList<Personnel> dataSet;
    Activity activity;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView perName, perTime ;
        ImageView perImage;
        LinearLayout linearLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.perName = itemView.findViewById(R.id.perName);
            this.perImage = itemView.findViewById(R.id.perImage);
            this.perTime = itemView.findViewById(R.id.perTime);
            this.linearLayout = itemView.findViewById(R.id.linearL);
        }
    }

    public EatersListAdapter(Activity activity, ArrayList<Personnel> data) {
        this.activity = activity;
        this.dataSet = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.eaters_adapter_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Personnel item = dataSet.get(position);
        holder.perName.setText(item.getInfo());
        holder.perTime.setText("Ішкен уақыты: "+item.getTime());

        Glide.with(activity)
                .load(item.getPhoto())
                .placeholder(R.drawable.eater_icon3)
                .into(holder.perImage);

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void removeItem(int position) {
        dataSet.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Personnel student, int position) {
        dataSet.add(position, student);
        notifyItemInserted(position);
    }
}