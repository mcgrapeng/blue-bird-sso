package com.bird.sso.domain;

import com.bird.sso.api.enums.PublicEnum;
import lombok.Data;

import java.util.Date;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/7 11:26
 */
@Data
public class AppVersion{

    private Integer id;

    private String key;
    //版本号
    private String versionCode;
    //apk路径
    private String downloadUrl;

    private String originalFilename;

    //升级信息
    private String describe;
    private String md5;
    private String isForce = PublicEnum.Y.name();
    //包名
    private String packageName;

    private String signature;

    private Date createTime;


}
