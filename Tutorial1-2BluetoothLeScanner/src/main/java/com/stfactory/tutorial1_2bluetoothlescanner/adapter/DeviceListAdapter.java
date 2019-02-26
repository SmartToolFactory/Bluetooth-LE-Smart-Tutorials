package com.stfactory.tutorial1_2bluetoothlescanner.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stfactory.tutorial1_2bluetoothlescanner.R;
import com.stfactory.tutorial1_2bluetoothlescanner.model.CustomBluetoothDevice;

import java.util.Collections;
import java.util.List;



public class DeviceListAdapter
        extends RecyclerView.Adapter<DeviceListAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private List<CustomBluetoothDevice> data = Collections.emptyList();

    private OnRecyclerViewMeasureClickListener recyclerClickListener;

    public DeviceListAdapter(Context context, List<CustomBluetoothDevice> data) {
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        CustomBluetoothDevice customBluetoothDevice = data.get(position);

        BluetoothDevice bluetoothDevice = data.get(position).bluetoothDevice;
        int rssi = customBluetoothDevice.rssi;


        holder.tvDeviceName.setText(bluetoothDevice.getName());
        holder.tvDeviceAddress.setText(bluetoothDevice.getAddress());
        holder.tvDeviceRSSI.setText("RSSI " + rssi);

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int arg1) {
        View view = inflater.inflate(R.layout.listitem_device, parent, false);

        return new MyViewHolder(view);
    }

    /**
     * get an instance of OnRecyclerViewClickListener interface
     *
     * @param recyclerClickListener callback that is used by adapter to invoke the method of the class
     *                              implements the OnRecyclerViewClickListener interface
     */
    public void setClickListener(OnRecyclerViewMeasureClickListener recyclerClickListener) {
        this.recyclerClickListener = recyclerClickListener;
    }


    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Views
        private TextView tvDeviceName, tvDeviceAddress, tvDeviceRSSI;

        public MyViewHolder(View itemView) {
            super(itemView);

            tvDeviceName = itemView.findViewById(R.id.device_name);
            tvDeviceAddress = itemView.findViewById(R.id.device_address);
            tvDeviceRSSI = itemView.findViewById(R.id.device_rssi);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (recyclerClickListener != null) {
                recyclerClickListener.onItemClicked(v, getLayoutPosition());
            }
        }

    }

    /**
     * RecyclerViewClickListener interface helps user to set a clickListener to the
     * RecyclerView. By setting this listener, any item of Recycler View can respond
     * to any interaction.
     */
    public interface OnRecyclerViewMeasureClickListener {
        /**
         * This is a callback method that be overriden by the class that implements this
         * interface
         */
        public void onItemClicked(View view, int position);

    }


    public void updateList(List<CustomBluetoothDevice> deviceList) {
        data = deviceList;
        notifyDataSetChanged();
    }

    public void addDevice(CustomBluetoothDevice device) {
        if (data != null && !data.contains(device)) {
            data.add(device);
            notifyDataSetChanged();
        }
    }

    public void clear() {
        if (data != null) {
            data.clear();
            notifyDataSetChanged();
        }
    }
}
