package com.sh.ec.adapter;

import android.util.Log;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.sh.ec.R;
import com.sh.ec.entity.ClickEntity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.sh.ec.entity.EquipmentTypeContant.BIKE;
import static com.sh.ec.entity.EquipmentTypeContant.EPLLPTICAL;
import static com.sh.ec.entity.EquipmentTypeContant.TREADMILL;

public class MachineListDemoAdapter extends BaseMultiItemQuickAdapter<ClickEntity, BaseViewHolder> {

    public MachineListDemoAdapter(List<ClickEntity> data) {
        super(data);
        addItemType(ClickEntity.CLICK_ITEM_VIEW, R.layout.machine_list_item_layout);
    }

    public static int selectedPosition = -1;// 选中的位置

    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, ClickEntity clickEntity) {
        switch (holder.getItemViewType()) {
            case ClickEntity.CLICK_ITEM_VIEW:
                for (int i = 0; i < clickEntity.dcEquipments.size(); i++) {

                    String name = clickEntity.dcEquipments.get(i).getName();

                    Log.e("adapter", "--------------------" + name);
                    if (name.contains(BIKE)) {
                        holder.setText(R.id.machine_list_type, getContext().getString(R.string.bike_txt));
                        holder.setImageResource(R.id.machine_list_img, R.mipmap.icon_equipment_bike);

                    } else if (name.contains(EPLLPTICAL)) {
                        holder.setText(R.id.machine_list_type, getContext().getString(R.string.eplliptical_txt));
                        holder.setImageResource(R.id.machine_list_img, R.mipmap.icon_equipment_ep);

                    } else if (name.contains(TREADMILL)) {
                        holder.setText(R.id.machine_list_type, getContext().getString(R.string.treadmill_txt));
                        holder.setImageResource(R.id.machine_list_img, R.mipmap.icon_equipment_treadmill);

                    } else {
                        holder.setText(R.id.machine_list_type, getContext().getString(R.string.unknow));
                        holder.setImageResource(R.id.machine_list_img, R.mipmap.ic_launcher);
                    }
                    holder.setText(R.id.machine_list_name, name);
                }

                Log.e("adapter", "-----------CLICK_ITEM_VIEW---------" + holder.getPosition());
                break;
        }


    }
}
