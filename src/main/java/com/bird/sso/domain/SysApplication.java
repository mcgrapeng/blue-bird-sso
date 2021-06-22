package com.bird.sso.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/5/17 14:29
 */
@Getter
@Setter
public class SysApplication {

    private Integer id;

    /**
     * 应用id
     */
    private Long aid;

    /**
     * 名称
     */
    private String name;


    /**
     * 应用代码
     */
    private String code;

    /**
     * 主体名称
     */
    private String subjectName;

    /**
     * 简称
     */
    private String shortName;

    /**
     * LOGO
     */
    private String icon;


    /**
     * 应用状态
     */
    private String status;

    /**
     * 描述
     */
    private String desc;

    /**
     * 域名
     */
    private String domain;


    /**
     * 管理后台地址
     */
    private String webUrl;


    /**
     * 移动端地址
     */
    private String appUrl;


    /**
     * H5端地址
     */
    private String wapUrl;

    /**
     * 平台简介
     */
    private String intro;


    /**
     * 应用绑定的服务
     */
    private String services;


    private Date editTime;

    private Date createTime;

    private String creator;

    private String editor;

    private String ext;

    private String remark;

}
