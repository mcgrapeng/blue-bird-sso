package com.bird.sso.web.controller.manage.menu;

import com.bird.sso.api.enums.PublicEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.enums.MenuTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/3/11 14:34
 */
@Getter
@Setter
public class UserResourceForm {

    @NotNull(message = "sid不能为空", groups = {MenuUpd.class, OperUpd.class})
    private Long sid;
    @NotBlank(message = "请输入代码", groups = {MenuAdd.class, OperAdd.class})
    private String code;
    @NotBlank(message = "请输入名称", groups = {MenuAdd.class, MenuUpd.class, OperAdd.class, OperUpd.class})
    private String name;

    @NotBlank(message = "请选择应用类型", groups = {MenuAdd.class, MenuUpd.class, OperAdd.class, OperUpd.class})
    private String appType;
    @NotBlank(message = "请选择端类型", groups = {MenuAdd.class, OperAdd.class})
    private String clientType;

    @NotNull(message = "请选择父级", groups = {MenuAdd.class, MenuUpd.class, OperAdd.class, OperUpd.class})
    private Long pid = 0L;

    @NotNull(message = "等级不能为空", groups = {MenuAdd.class, MenuUpd.class})
    private Integer level;

    @NotBlank(message = "请选择资源类型", groups = {MenuAdd.class, OperAdd.class})
    private String type;

    private String path;

    private String icon;

    private String router;

    /**
     * 是否可点击
     */
    private String isClick = PublicEnum.Y.name();
    /**
     * 是否为外链
     */
    private String isNet = PublicEnum.N.name();

    /**
     * 是否可用
     */
    private String isAvailable = PublicEnum.Y.name();

    /**
     * 菜单分类
     */
    //@NotBlank(message = "请选择菜单分类", groups = {MenuAdd.class, MenuUpd.class, OperAdd.class, OperUpd.class})
    private String posit;


    /**
     * 菜单添加
     */
    public interface MenuAdd {
    }

    /**
     * 菜单修改
     */
    public interface MenuUpd {
    }

    /**
     * 按钮添加
     */
    public interface OperAdd {
    }

    /**
     * 按钮修改
     */
    public interface OperUpd {
    }


    public Integer getLevel() {
        if (pid == 0) {
            this.level = 1;
        }
        return level;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getIsClick() {
        if (type.equals(MenuTypeEnum.MENU.name())) {
            if (ObjectUtils.isNotEmpty(pid) && pid == 0) {
                this.isClick = PublicEnum.N.name();
            } else {
                this.isClick = PublicEnum.Y.name();
            }
        } else {
            this.isClick = PublicEnum.Y.name();
        }
        return isClick;
    }


    public String getRouter() {
        if (isNet.equals(PublicEnum.N.name()) && StringUtils.isBlank(this.router)) {
            throw SSOException.PARAM_ERR;
        }
        return router;
    }

    public String getPath() {
        if (isNet.equals(PublicEnum.Y.name())) {
            if (StringUtils.isBlank(this.path)) {
                throw SSOException.PARAM_ERR;
            }
        } else {
            if (this.type.equals(MenuTypeEnum.OPER.name())) {
                if (StringUtils.isBlank(this.path) && StringUtils.isBlank(this.router)) {
                    throw SSOException.PARAM_ERR;
                }
            }
        }
        return path;
    }

    @Setter
    @Getter
    public static class ResourceSort {
        @NotBlank(message = "业务类型不能为空")
        private String appTypeUp;
        @NotBlank(message = "业务类型不能为空")
        private String appTypeDw;

        @NotNull(message = "资源ID不能为空")
        private Long sidUp;

        @NotNull(message = "资源ID不能为空")
        private Long sidDw;

        @NotNull(message = "排序字段不能为空")
        private Integer sortUp;
        @NotNull(message = "排序字段不能为空")
        private Integer sortDw;
    }


    @Setter
    @Getter
    public static class ResourceRelation {
        @NotBlank(message = "业务类型不能为空")
        private String appTypeSid;

        @NotBlank(message = "业务类型不能为空")
        private String appTypePid;

        @NotNull(message = "资源ID不能为空")
        private Long sid;

        @NotNull(message = "资源ID不能为空")
        private Long pid;

        @NotNull(message = "父级菜单级别不能为空")
        private Integer pLevel;
    }

}
