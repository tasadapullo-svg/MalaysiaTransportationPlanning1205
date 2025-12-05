package org.example;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"org.example", "com.utm.traffic"})
@MapperScan("com.utm.traffic.mapper")
@EnableScheduling // 若后续有定时采集任务（如15分钟轮询API），提前启用
@EnableTransactionManagement // 若后续有数据库事务操作（如批量入库），提前启用
public class MalaysiaTransportationPlanningApplication {
    public static void main(String[] args) {
        SpringApplication.run(MalaysiaTransportationPlanningApplication.class, args);
        System.out.println("Spring Boot started!");
        //mvn clean package -DskipTests
    }
}
