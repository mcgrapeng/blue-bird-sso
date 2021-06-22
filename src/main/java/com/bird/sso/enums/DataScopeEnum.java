package com.bird.sso.enums;

/**
 * @author 张朋
 * @version 1.0
 * @desc  数据权限类型
 * @date 2020/12/3 16:13
 */
public enum DataScopeEnum {

    ALL("全部数据权限"),
    CUS("自定义数据权限"),
    DEP("本部门数据权限"),
    DER("本部门及以下数据权限"),;

    private String desc;

    DataScopeEnum(String desc) {
        this.desc = desc;
    }
}
