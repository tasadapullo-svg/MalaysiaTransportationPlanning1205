package com.utm.traffic.constant;

import com.alibaba.fastjson.JSONObject;
import com.utm.traffic.entity.RedisInternalCombination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
/**
 * 解析接口返回参数方法
 * 保存接口方法
 */
public class AnalysisJsonTomTom {
    @Transactional(rollbackFor = Exception.class)
    public void saveTomTomData(JSONObject jsonObject, RedisInternalCombination combination) {


    }
}
