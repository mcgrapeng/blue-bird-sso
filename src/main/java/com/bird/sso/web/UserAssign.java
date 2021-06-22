package com.bird.sso.web;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/6/15 15:13
 */
@Setter
@Getter
public class UserAssign {
    @NotNull(message = "参数不合法,缺失【sid】")
    private Long sid;

    @NotBlank(message = "参数不合法,缺失【appType】")
    private String appType;

    @NotBlank(message = "参数不合法,缺失【code】")
    private String code;

    @NotBlank(message = "参数不合法,缺失【name】")
    private String name;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAssign assign = (UserAssign) o;
        return Objects.equals(sid, assign.sid) &&
                Objects.equals(appType, assign.appType);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sid, appType);
    }
}
