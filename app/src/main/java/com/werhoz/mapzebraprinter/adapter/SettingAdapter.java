package com.werhoz.mapzebraprinter.adapter;

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

    private List<BluetoothDevice> devices = new ArrayList<>();

    public void submitList(List<BluetoothDevice> list) {
        this.devices = list;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, address;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.device_name);
            address = itemView.findViewById(R.id.device_address);
        }
    }

    @NonNull
    @Override
    public SettingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_setting, parent, false);
        return new ViewHolder(view);
    }

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