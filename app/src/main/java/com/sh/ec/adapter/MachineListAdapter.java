package com.sh.ec.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import com.appdevice.domyos.DCEquipment;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.sh.ec.R;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.sh.ec.entity.EquipmentTypeContant.BIKE;
import static com.sh.ec.entity.EquipmentTypeContant.EPLLPTICAL;
import static com.sh.ec.entity.EquipmentTypeContant.TREADMILL;

public class MachineListAdapter extends BaseQuickAdapter<DCEquipment, BaseViewHolder> {

    private List<DCEquipment> list;
    public static int selectedPosition = -1;// 选中的位置

    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }

    public MachineListAdapter(List<DCEquipment> list) {
        super(R.layout.machine_list_item_layout,list);
        this.list = list;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, DCEquipment dcEquipment) {
        String name = dcEquipment.getName();
        Log.e("Machine", "deviceName:  " + name);
        if (name.contains(BIKE)) {
            holder.setText(R.id.machine_list_type, getContext().getString(R.string.bike_txt));
            holder.setImageResource(R.id.machine_list_img,R.mipmap.icon_equipment_bike);

        } else if (name.contains(EPLLPTICAL)) {
            holder.setText(R.id.machine_list_type,getContext().getString( R.string.eplliptical_txt));
            holder.setImageResource(R.id.machine_list_img,R.mipmap.icon_equipment_ep);

        } else if (name.contains(TREADMILL)) {
            holder.setText(R.id.machine_list_type,getContext().getString( R.string.treadmill_txt));
            holder.setImageResource(R.id.machine_list_img,R.mipmap.icon_equipment_treadmill);

        }else {
            holder.setText(R.id.machine_list_type,getContext().getString( R.string.unknow));
            holder.setImageResource(R.id.machine_list_img,R.mipmap.ic_launcher);
        }
        holder.setText(R.id.machine_list_name,name);

        if (holder.getPosition()==selectedPosition ) {
            holder.itemView.setBackgroundColor(getContext().getColor(R.color.button_color_select));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

    }

}
