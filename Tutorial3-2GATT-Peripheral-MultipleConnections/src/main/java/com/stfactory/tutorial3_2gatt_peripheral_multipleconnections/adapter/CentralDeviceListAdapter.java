package com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.R;

import java.util.List;

/**
 * Adapter to be used for listing central devices that connected to this Peripheral device.
 */
public class CentralDeviceListAdapter
        extends RecyclerView.Adapter<CentralDeviceListAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private List<BluetoothDevice> data;

    private OnRecyclerViewClickListener recyclerClickListener;

    public CentralDeviceListAdapter(Context context, List<BluetoothDevice> data) {
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        BluetoothDevice bluetoothDevice = data.get(position);

        String deviceName = bluetoothDevice.getName();
        if (deviceName == null) {
            holder.tvDeviceName.setText("-");
        } else {
            holder.tvDeviceName.setText(bluetoothDevice.getName());
        }
        holder.tvDeviceAddress.setText(bluetoothDevice.getAddress());

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int arg1) {
        View view = inflater.inflate(R.layout.list_item_central_device, parent, false);
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


    class MyViewHolder extends RecyclerView.ViewHolder {
        // Views
        private TextView tvDeviceName, tvDeviceAddress, btnNotify;

        public MyViewHolder(View itemView) {
            super(itemView);

            tvDeviceName = itemView.findViewById(R.id.device_name);
            tvDeviceAddress = itemView.findViewById(R.id.device_address);

            btnNotify = itemView.findViewById(R.id.btnNotify);

            btnNotify.setOnClickListener(v -> {
                if (recyclerClickListener != null) {
                    recyclerClickListener.onItemClicked(v, getLayoutPosition());
                }
            });
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


    public void updateList(List<BluetoothDevice> deviceList) {
        data = deviceList;
        notifyDataSetChanged();
    }

    public void addDevice(BluetoothDevice device) {
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
