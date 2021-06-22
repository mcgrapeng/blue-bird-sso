package com.bird.sso.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SysUserVersion {
    private Integer id;

    private String appType;

    private Long userId;

    private String oldUserName;

    private String newUserName;

    private String realName;

    private String creator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String remark;
}