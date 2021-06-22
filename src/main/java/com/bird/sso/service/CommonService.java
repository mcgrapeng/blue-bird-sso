package com.bird.sso.service;

import com.bird.sso.utils.CommonUtils;
import com.bird.sso.utils.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/3/5 19:04
 */
@Service
public class CommonService {

    @Autowired
    private RedisUtils redisUtils;


    /**
     * 生成代码
     *
     * @param bizType
     * @param appType
     * @return
     */
    public String produceCode(String template, String bizType, String appType) {
        String code = (String) redisUtils.get(String.format(template, appType));
        String produceCode;
        if (StringUtils.isNotEmpty(code)) {
            produceCode = CommonUtils.getCode(appType, bizType, code);
        } else {
            produceCode = CommonUtils.getCode(appType, bizType, null);
        }
        redisUtils.set(String.format(template, appType), produceCode);
        return produceCode;
    }

}
