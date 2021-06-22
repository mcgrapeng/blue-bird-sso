package com.bird.sso.config;

import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/11/19 14:34
 */
@Configuration
public class MimeMappingConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
        mappings.add("apk", "application/vnd.android.package-archive; charset=utf-8");
        factory.setMimeMappings(mappings);
    }

}
