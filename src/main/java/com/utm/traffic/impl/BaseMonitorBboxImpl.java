package com.utm.traffic.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hz.utils.UUIDUtils;
import com.utm.traffic.entity.DataHereFlow;
import com.utm.traffic.entity.DataHereShapePoint;
import com.utm.traffic.mapper.DataHereFlowMapper;
import com.utm.traffic.mapper.DataHereShapePointMapper;
import com.utm.traffic.service.IBaseMonitorBboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

/**
 * here接口类实现json解析和保存
 */
@Slf4j
@Service
public class BaseMonitorBboxImpl implements IBaseMonitorBboxService {
    @Autowired
    private DataHereFlowMapper hereFlowMapperLowMapper;

    @Autowired
    private DataHereShapePointMapper hereShapeMapper;

    /**
     * 事务管理：主表+从表原子保存，要么都成功要么都回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveHereData(JSONObject jsonObject, String batchId, String placeId) {
        // 解析 results 数组
        JSONArray results = jsonObject.getJSONArray("results");
        if (results == null || results.isEmpty()) {
            log.warn("HERE 返回数据为空，不执行入库");
            throw new IllegalArgumentException("HERE 返回数据为空，不执行入库");
        }

        for (int i = 0; i < results.size(); i++) {
            JSONObject result = results.getJSONObject(i);
            // 解析 location
            JSONObject location = result.getJSONObject("location");
            JSONObject currentFlow = result.getJSONObject("currentFlow");
            String flowUuid = UUIDUtils.getUUID();

            // ************** 1. 保存主表 data_here_flow **************
            DataHereFlow flow = new DataHereFlow();
            flow.setUuid(UUIDUtils.getUUID());
            //关联 base_monitor_bboxes 表的主键
            flow.setBboxUuid(flowUuid);
            //批号
            flow.setBatchId(batchId);
            //区域编号
            flow.setPlaceId(placeId);
            flow.setCaptureTime(new Date());
            //道路名称
            flow.setRoadName(location.getString("description"));
            //包含多少道路
            flow.setNumberRoads(location.getJSONObject("shape").getJSONArray("links").size());
            // speed
            if (currentFlow != null) {
                flow.setSpeedKmh(currentFlow.getBigDecimal("speed"));
                flow.setJamFactor(currentFlow.getBigDecimal("jamFactor"));
            } else {
                flow.setSpeedKmh(BigDecimal.ZERO);
                flow.setJamFactor(BigDecimal.ZERO);
            }
            flow.setCreateTime(new Date());
            log.info("flow->{}", flow);
            hereFlowMapperLowMapper.insert(flow); // 🔥 入主表

            // ************** 2. 保存从表 data_here_shape_points **************
            ArrayList<DataHereShapePoint> shapeList = new ArrayList<>();
            int seq = 1;
            JSONArray links = location.getJSONObject("shape").getJSONArray("links");
            //一整段路名
            String AllDescriptionStr = location.getString("description");
            //一整段路的长度
            Integer AllLength = location.getInteger("length");
            for (int j = 0; j < links.size(); j++) {
                JSONObject link = links.getJSONObject(j);
                JSONArray points = link.getJSONArray("points");
                //支路长度
                Integer zlLength = link.getInteger("length");
                log.info("link->{}", link);
                Integer functionalClass = link.getInteger("functionalClass");
                log.info("functionalClass->{}", functionalClass);
                for (int k = 0; k < points.size(); k++) {
                    JSONObject point = points.getJSONObject(k);
                    DataHereShapePoint sp = new DataHereShapePoint();
                    sp.setUuid(UUIDUtils.getUUID());
                    //一条数据对应的uuid
                    sp.setFlowUuid(flowUuid);
                    sp.setLatitude(point.getBigDecimal("lat"));
                    sp.setLongitude(point.getBigDecimal("lng"));
                    sp.setSequenceOrder(seq++);
                    sp.setCreateTime(new Date());
                    sp.setAllLength(AllLength);
                    sp.setAllDescriptionStr(AllDescriptionStr);
                    sp.setZlLength(zlLength);
                    sp.setFunctionalClass(functionalClass);
//                    log.info("sp->{}", sp);
                    hereShapeMapper.insert(sp);//写从表
                }
            }

            log.info("HERE子表数据流量数据保存成功 flowUuid->{}", flowUuid);
        }
        return true;
    }

}