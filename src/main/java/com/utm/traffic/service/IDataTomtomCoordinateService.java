package com.utm.traffic.service;

import com.alibaba.fastjson.JSONObject;

public interface IDataTomtomCoordinateService {
    void saveTomTomData(JSONObject jsonObject, String uuid);
}
