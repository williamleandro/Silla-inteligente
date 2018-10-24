package com.proyecto.arduinos.sillainteligente;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;


public class DeviceListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<BluetoothDevice> mData;
   private OnPairButtonClickListener mListener;

    public DeviceListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<BluetoothDevice> data) {
        mData = data;
    }

    public void setListener(OnPairButtonClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getCount() {
        return (mData==null)? 0:mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.device_adapter_view, null);
            holder = new ViewHolder();

            holder.tvDeviceName = convertView.findViewById(R.id.tvDeviceName);
            holder.tvDeviceAddress = convertView.findViewById(R.id.tvDeviceAddress);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = mData.get(position);

        holder.tvDeviceName.setText(device.getName());
        holder.tvDeviceAddress.setText(device.getAddress());
        return convertView;
    }

    public interface OnPairButtonClickListener {
        public abstract void onPairButtonClick(int position);
    }

    static class ViewHolder {
        TextView tvDeviceName;
        TextView tvDeviceAddress;
        Button emparejar;
    }
}
