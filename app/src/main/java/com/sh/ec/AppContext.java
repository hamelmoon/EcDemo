/*
******************************* Copyright (c)*********************************\
**
**                 (c) Copyright 2015, Allen, china, shanghai
**                          All Rights Reserved
**
**                          
**                         
**-----------------------------------版本信息------------------------------------
** 版    本: V0.1
**
**------------------------------------------------------------------------------
********************************End of Head************************************\
*/
package com.sh.ec;

import android.app.Application;
import android.content.Intent;


import com.github.mikephil.charting.utils.Utils;
import com.sh.ec.service.SportDataService;

import java.util.HashMap;

public class AppContext extends Application {
    private static AppContext appContext;

    public static AppContext getInstance() {
        return appContext;
    }

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public void put(String key,Object object){
        map.put(key, object);
    }

    public Object get(String key){
        return map.get(key);
    }



    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        Utils.init(this);

    }



    

}
