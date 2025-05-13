package com.werhoz.mapzebraprinter.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.werhoz.mapzebraprinter.R;

import java.util.ArrayList;
import java.util.List;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.ViewHolder> {

    private OnItemClickListener onItemClickListener;
    private List<BluetoothDevice> devices = new ArrayList<>();

    public SettingAdapter(List<BluetoothDevice> devices) {
        this.devices = devices;
    }

    // Interface for click listener
    public interface OnItemClickListener {
        void onItemClick(BluetoothDevice device);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void submitList(List<BluetoothDevice> list) {
        this.devices = list;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, address;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.device_name);
            address = itemView.findViewById(R.id.device_address);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (onItemClickListener != null && position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(devices.get(position));
                }
            });
        }
    }

    @NonNull
    @Override
    public SettingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_setting, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull SettingAdapter.ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        holder.name.setText(device.getName() != null ? device.getName() : "Unknown Device");
        holder.address.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

}