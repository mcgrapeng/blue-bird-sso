package com.bird.sso.web.controller.manage.range;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/6/3 14:12
 */
@Getter
@Setter
public class UserRangeForm {

    @NotBlank(message = "应用类型不能为空",groups = {Range1.class,Range2.class,Range3.class})
    private String appType;

    @NotNull(message = "角色标识不能为空",groups = {Range1.class,Range2.class,Range3.class})
    private Long roleId;

    @NotNull(message = "组织标识不能为空",groups = {Range2.class})
    private Long orgId;

    @Size(min = 1, message = "组织列表不能为空",groups = {Range3.class})
    private List<Long> range;


    public interface Range1{}
    public interface Range2{}
    public interface Range3{}
}
