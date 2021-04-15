package com.design.util;

import com.design.bean.Result;
import com.design.enums.ResultEnum;

/**
 * @Description
 * @Author huangming
 * @Date 2021/4/15
 **/
public class ResultUtil {

    public static Result success() {
        Result result = new Result();
        result.setCode(ResultEnum.SUCCESS.getCode());
        result.setMessage(ResultEnum.SUCCESS.getMessage());
        return result;
    }

    public static Result success(String message) {
        Result result = new Result();
        result.setCode(ResultEnum.SUCCESS.getCode());
        result.setMessage(message);
        return result;
    }

    public static Result fail(String message) {
        Result result = new Result();
        result.setCode(ResultEnum.FALSE.getCode());
        result.setMessage(message);
        return result;
    }

    public static Result fail() {
        Result result = new Result();
        result.setCode(ResultEnum.FALSE.getCode());
        result.setMessage(ResultEnum.FALSE.getMessage());
        return result;
    }

}
