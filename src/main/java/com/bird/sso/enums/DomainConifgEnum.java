package com.bird.sso.enums;

import lombok.Getter;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/12/18 19:58
 */
@Getter
public enum DomainConifgEnum {


    APP_PB("party.bjchy.gov.cn", "党建"),
    APP_EP("ep.police.bird.com", "防疫"),
    SSO("lion.sso.bird.com", "认证中心"),
    SSO_ADMIN("SSO_ADMIN", "认证管理后台"),
    IM("IM", "即时通讯"),
    APP_GA("wastesort.bjchy.bird.com", "垃圾分类"),
    APP_UF("miniapp.bjcytz.bird.com", "统战平台"),
    APP_JA("needs.szcs.360.cn", "需求共享平台"),;


    private String domain;

    private String desc;


    DomainConifgEnum(String domain, String desc) {
        this.domain = domain;
        this.desc = desc;
    }


    public static String getDomain(String appType) {

        for (DomainConifgEnum domainConifg : DomainConifgEnum.values()) {
            if (domainConifg.name().equalsIgnoreCase(appType)) {
                return domainConifg.getDomain();
            }
        }
        return null;
    }
}
