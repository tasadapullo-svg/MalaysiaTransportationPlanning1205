package com.utm.traffic.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hz.utils.UUIDUtils;
import com.utm.traffic.entity.DataTomtomCoordinate;
import com.utm.traffic.entity.DataTomtomFlow;
import com.utm.traffic.mapper.DataTomtomCoordinateMapper;
import com.utm.traffic.mapper.DataTomtomFlowMapper;
import com.utm.traffic.service.IDataTomtomCoordinateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@Service
public class DataTomtomCoordinateImpl implements IDataTomtomCoordinateService {
    @Autowired
    private DataTomtomFlowMapper dataTomtomFlowMapper; // 流数据主表Mapper
    @Autowired
    private DataTomtomCoordinateMapper dataTomtomCoordinateMapper; // 坐标从表Mapper

    /**
     * 事务管理：主表+从表原子保存，要么都成功要么都回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTomTomData(JSONObject jsonObject, String uuid) {
        try {
            // 1. 解析JSON核心数据（从flowSegmentData节点提取）
            JSONObject flowSegmentData = jsonObject.getJSONObject("flowSegmentData");
            if (flowSegmentData == null) {
                log.warn("JSON中无flowSegmentData节点，跳过入库");
                return;
            }
            //关联的从表的flow_uuid
            String flowUuid = UUIDUtils.getUUID();
            // 3. 构建主表（data_tomtom_flow）数据
            DataTomtomFlow tomtomFlow = new DataTomtomFlow();
            tomtomFlow.setUuid(flowUuid); // 主表主键
            tomtomFlow.setPointUuid(uuid); // 关联点位UUID
            tomtomFlow.setCaptureTime(new Date()); // 采集时间（时间戳）
            tomtomFlow.setCurrentSpeed(flowSegmentData.getIntValue("currentSpeed")); // 实时速度
            tomtomFlow.setFreeFlowSpeed(flowSegmentData.getIntValue("freeFlowSpeed")); // 自由流速度
            tomtomFlow.setConfidence(BigDecimal.valueOf(flowSegmentData.getDoubleValue("confidence"))); // 置信度（转BigDecimal）
            tomtomFlow.setRoadClosure(flowSegmentData.getBooleanValue("roadClosure")); // 是否封路（表字段拼写：road_clasure）
            tomtomFlow.setCreateTime(new Date()); // 创建时间
            tomtomFlow.setCurrentTravelTime(flowSegmentData.getIntValue("currentTravelTime"));
            tomtomFlow.setFreeFlowTravelTime(flowSegmentData.getIntValue("freeFlowTravelTime"));
            log.info(tomtomFlow.toString());
            // 4. 保存主表
            dataTomtomFlowMapper.insert(tomtomFlow);

            // 5. 解析坐标数据，构建从表（data_tomtom_coordinates）
            JSONObject coordinates = flowSegmentData.getJSONObject("coordinates");
            JSONArray coordinateList = coordinates.getJSONArray("coordinate");
            if (coordinateList == null || coordinateList.isEmpty()) {
                log.warn("无坐标数据，从表跳过保存，批号->{}", uuid);
                return;
            }
            // 6. 循环保存坐标点（按返回顺序设置sequence_order）
            for (int i = 0; i < coordinateList.size(); i++) {
                JSONObject coord = coordinateList.getJSONObject(i);
                DataTomtomCoordinate coordinate = new DataTomtomCoordinate();
                coordinate.setUuid(UUIDUtils.getUUID()); // 从表主键
                coordinate.setFlowUuid(flowUuid); // 关联主表flow_uuid
                coordinate.setLatitude(BigDecimal.valueOf(coord.getDoubleValue("latitude"))); // 纬度（转BigDecimal）
                coordinate.setLongitude(BigDecimal.valueOf(coord.getDoubleValue("longitude"))); // 经度（转BigDecimal）
                coordinate.setSequenceOrder(i + 1); // 排序号（从1开始）
                coordinate.setCreateTime(new Date()); // 创建时间
                // 保存单个坐标点
                log.info(coordinate.toString());
                dataTomtomCoordinateMapper.insert(coordinate);
            }
            log.warn("TOMTOM单个点位数据库保存结束-组合关联点位起始关联点位UUID->{}",uuid);
        } catch (Exception e) {
            throw new RuntimeException("TomTom数据入库异常", e); // 抛出异常触发事务回滚
        }
    }


}

