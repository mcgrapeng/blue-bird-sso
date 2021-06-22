package com.bird.sso.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/3 12:11
 */
@Getter
@Setter
public class SelfReference<T> {

    protected Integer id;

    protected Long sid;
    protected String code;

    protected String name;
    protected String parentName;

    protected String isAvailable;

    protected Integer level;

    protected String appType;

    protected String clientType;

    protected Long pid = 0L;
    protected Integer ord;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date createTime;
    protected String creator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date updateTime;
    protected String editor;

    protected String remark;

    protected List<T> childrens;
}
