/*
 * @Author: zhangjianing 1026597665@qq.com
 * @Date: 2026-04-27 11:25:16
 * @LastEditors: zhangjianing 1026597665@qq.com
 * @LastEditTime: 2026-04-27 11:32:55
 * @FilePath: /ALL-Powerful/Backend/src/main/java/com/allpowerful/backend/BackendApplication.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.allpowerful.backend;

import com.allpowerful.backend.weather.WeatherProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(WeatherProperties.class)
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
