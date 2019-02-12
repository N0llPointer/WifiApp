package com.nollpointer.wifiapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<String> devices;
    Listener listener;

    public DevicesAdapter(List<String> devices, Listener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.peer_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        TextView textView = viewHolder.itemView.findViewById(android.R.id.text1);
        textView.setText(devices.get(position));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void updateDevices(List<String> devices){
        this.devices = devices;
        notifyDataSetChanged();
    }

    interface Listener{
        void onClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(getAdapterPosition());
                }
            });
        }
    }
}
