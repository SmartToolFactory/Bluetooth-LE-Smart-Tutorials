package com.example.tutorial3_1gatt_connect.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tutorial3_1gatt_connect.R;
import com.example.tutorial3_1gatt_connect.model.PeripheralDeviceItem;

import java.text.DecimalFormat;
import java.util.List;


public class DeviceListAdapter
        extends RecyclerView.Adapter<DeviceListAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private List<PeripheralDeviceItem> data;

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private OnRecyclerViewClickListener recyclerClickListener;

    public DeviceListAdapter(Context context, List<PeripheralDeviceItem> data) {
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        PeripheralDeviceItem customBluetoothDevice = data.get(position);

        BluetoothDevice bluetoothDevice = data.get(position).bluetoothDevice;

        String rssi = "RSSI " + customBluetoothDevice.rssi;
        String txPower = "TxPower " + customBluetoothDevice.txPower;
        String accuracy = "Acc " + decimalFormat.format(customBluetoothDevice.getCalculatedAccuracy());


        holder.tvDeviceName.setText(bluetoothDevice.getName());
        holder.tvDeviceAddress.setText(bluetoothDevice.getAddress());
        holder.tvDeviceRSSI.setText(rssi);
        holder.tvDeviceTxPower.setText(txPower);
        holder.tvDeviceAccuracy.setText(accuracy);

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int arg1) {
        View view = inflater.inflate(R.layout.list_item_peripheral_device, parent, false);

        return new MyViewHolder(view);
    }

    /**
     * get an instance of OnRecyclerViewClickListener interface
     *
     * @param recyclerClickListener callback that is used by adapter to invoke the method of the class
     *                              implements the OnRecyclerViewClickListener interface
     */
    public void setClickListener(OnRecyclerViewClickListener recyclerClickListener) {
        this.recyclerClickListener = recyclerClickListener;
    }


    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Views
        private TextView tvDeviceName, tvDeviceAddress, tvDeviceRSSI, tvDeviceTxPower, tvDeviceAccuracy;

        public MyViewHolder(View itemView) {
            super(itemView);

            tvDeviceName = itemView.findViewById(R.id.device_name);
            tvDeviceAddress = itemView.findViewById(R.id.device_address);
            tvDeviceRSSI = itemView.findViewById(R.id.device_rssi);
            tvDeviceTxPower = itemView.findViewById(R.id.device_tx_power);
            tvDeviceAccuracy = itemView.findViewById(R.id.device_accuracy);

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
    public interface OnRecyclerViewClickListener {
        /**
         * This is a callback method that be overriden by the class that implements this
         * interface
         */
        public void onItemClicked(View view, int position);

    }


    public void updateList(List<PeripheralDeviceItem> deviceList) {
        data = deviceList;
        notifyDataSetChanged();
    }

    public void addDevice(PeripheralDeviceItem device) {
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
