package com.sh.ec.adapter;

import android.view.View;

import com.appdevice.domyos.DCEquipment;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.sh.ec.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.sh.ec.entity.EquipmentTypeContant.BIKE;
import static com.sh.ec.entity.EquipmentTypeContant.EPLLPTICAL;
import static com.sh.ec.entity.EquipmentTypeContant.TREADMILL;

public class MachineListNewAdapter extends BaseQuickAdapter<DCEquipment, BaseViewHolder> {

    private List<DCEquipment> list;
    public static int selectedPosition = -1;// 选中的位置

    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }
    public static int connected = -1;// 选中的位置

    public void setConnectedPosition(int connectedPosition) {
        selectedPosition = -1;
        connected = connectedPosition;
    }

    public MachineListNewAdapter(List<DCEquipment> list) {
        super(R.layout.machine_list_item_layout,list);
        this.list = list;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, DCEquipment dcEquipment) {
        String name = dcEquipment.getName();
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
            
            holder.getView(R.id.loading).setVisibility(View.VISIBLE);
            holder.getView(R.id.machine_list_connect).setVisibility(View.GONE);
        } else {
            holder.getView(R.id.loading).setVisibility(View.GONE);
            holder.getView(R.id.machine_list_connect).setVisibility(View.VISIBLE);
            holder.setImageDrawable(R.id.machine_list_connect,getContext().getDrawable(R.mipmap.icon_bluetooth_off));

        }

        if (holder.getPosition()==connected){
            holder.getView(R.id.loading).setVisibility(View.GONE);
            holder.setImageDrawable(R.id.machine_list_connect,getContext().getDrawable(R.mipmap.icon_bluetooth_on));
        }else {
            holder.setImageDrawable(R.id.machine_list_connect,getContext().getDrawable(R.mipmap.icon_bluetooth_off));
        }




    }

}
