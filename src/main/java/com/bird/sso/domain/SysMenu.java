package com.bird.sso.domain;

import com.google.common.collect.Lists;
import com.bird.sso.web.controller.manage.menu.UserResourceForm;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 14:36
 */
@Data
public class SysMenu extends SelfReference<SysMenu> {
    //private Integer id;
    // private String code;
    // private String name;
//    private Integer level;
//    private Integer parentId;
//    private Integer ord;
    private String type;
    //@NotBlank(message = "请输入菜单路径", groups = {MenuAdd.class, MenuUpd.class})
    private String path;
    //@NotBlank(message = "请上传菜单图标", groups = {MenuAdd.class, MenuUpd.class})
    private String icon;
    //@NotBlank(message = "请选择菜单是否可点击", groups = {MenuAdd.class, MenuUpd.class})
    private String isClick;
    // @NotBlank(message = "请输入菜单路由", groups = {MenuAdd.class, MenuUpd.class})
    private String router;
    /**
     * 是否为外链
     */
    private String isNet;

    private String method;

    /**
     * 菜单分类
     */
    private String posit;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelfReference<?> that = (SelfReference<?>) o;
        return Objects.equals(sid, that.sid) &&
                StringUtils.equals(appType, that.appType);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sid, appType);
    }


    public static List<SysMenu> transform(List<UserResourceForm> resourceList) {
        if (CollectionUtils.isEmpty(resourceList)) return Lists.newArrayList();
        return resourceList.stream().map(t -> {
            SysMenu menu = new SysMenu();
            com.bird.sso.utils.bean.BeanUtils.copyPropertiesIgnoreNull(t, menu);
            return menu;
        }).collect(Collectors.toList());
    }
}
