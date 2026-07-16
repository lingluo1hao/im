package com.im.groupservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GroupRoleEnum {
    OWNER(1, "群主"),
    ADMIN(2, "管理员"),
    MEMBER(3, "普通成员");

    private final Integer code;
    private final String desc;
}