package com.bird.sso.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/18 16:56
 */
@Component
@ConfigurationProperties(prefix = "spring.profiles")
public class birdProfileProperties {
    private String active;

    public void setActive(String active) {
        this.active = active;
    }

    public String getActive() {
        return active;
    }
}
