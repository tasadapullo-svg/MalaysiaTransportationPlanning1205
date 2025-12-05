package com.utm.traffic.service;

import com.alibaba.fastjson.JSONObject;

public interface IBaseMonitorBboxService {
    boolean saveHereData(JSONObject jsonObject,String batchId,String placeId);
}
