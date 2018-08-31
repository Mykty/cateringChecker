package kz.incubator.myktybake.cateringchecker.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

import kz.incubator.myktybake.cateringchecker.R;
import kz.incubator.myktybake.cateringchecker.module.Order;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.MyViewHolder> {

    private ArrayList<Order> dataSet;
    Activity activity;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView orderPersonName;
        TextView date;
        TextView status;
        TextView personCount;
        TextView phoneNumber;
        TextView iconText;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.title           = itemView.findViewById(R.id.oTitle);
            this.orderPersonName = itemView.findViewById(R.id.oPerson);
            this.status          = itemView.findViewById(R.id.oStatus);
            this.date            = itemView.findViewById(R.id.oDate);
            this.personCount     = itemView.findViewById(R.id.oCount);
            this.phoneNumber     = itemView.findViewById(R.id.oPhoneNumber);

            this.iconText     = itemView.findViewById(R.id.iconText);
        }
    }

    public OrderListAdapter(Activity activity, ArrayList<Order> data) {
        this.dataSet = data;
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_list, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Order order = dataSet.get(position);
        holder.title.setText(order.getTitle());
        holder.orderPersonName.setText(order.getOrderPersonName());
        holder.status.setText(order.getStatus());
        holder.date.setText(order.getDate()+", "+order.getTime());
        holder.personCount.setText(order.getPersonCount()+ " адам");
        holder.phoneNumber.setText(order.getPhoneNumber());

        holder.iconText.setText(""+order.getTitle().charAt(0));

        if(order.getStatus().equals("жаңа")) holder.status.setTextColor(activity.getResources().getColor(R.color.red));
        if(order.getStatus().equals("қабылданды")) holder.status.setTextColor(activity.getResources().getColor(R.color.tabBack2));
        if(order.getStatus().equals("дайын")) holder.status.setTextColor(activity.getResources().getColor(R.color.green));


    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}