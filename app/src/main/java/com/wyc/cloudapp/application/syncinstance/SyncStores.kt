package com.wyc.cloudapp.application.syncinstance

class SyncStores:AbstractSyncBase("shop_stores", arrayOf("stores_id","stores_name","manager","telphone","region","status","nature","wh_id")
        , "正在同步仓库", "/api/scale/get_stores")