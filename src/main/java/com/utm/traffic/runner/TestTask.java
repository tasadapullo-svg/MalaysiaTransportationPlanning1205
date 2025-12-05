package com.utm.traffic.runner;
import com.utm.traffic.controller.HereController;
import com.utm.traffic.controller.TomTomController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Slf4j
@Component
public class TestTask implements CommandLineRunner {
    @Autowired
    private HereController hereController;

    @Autowired
    TomTomController tomTomController;

    @Override
    public void run(String... args) throws Exception {
        //todo 测试用例使用完删除
        log.info("定时任务开启");
//        hereController.HereTask();
//
//        tomTomController.scheduledTomTomTask();
    }
}
