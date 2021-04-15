package com.design.enums;

import lombok.Getter;

@Getter
public enum ResultEnum {
    SUCCESS(0000,"成功"),
    FALSE(0001,"失败");


    private Integer code;
    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
