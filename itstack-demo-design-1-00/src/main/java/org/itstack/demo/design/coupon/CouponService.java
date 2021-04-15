package org.itstack.demo.design.coupon;

import com.design.bean.Result;
import com.design.util.ResultUtil;

/**
 * 模拟优惠券服务
 */
public class CouponService {

    public Result sendCoupon(String uId, String couponNumber, String uuid) {
        System.out.println("模拟发放优惠券一张：" + uId + "," + couponNumber + "," + uuid);
        return ResultUtil.success();
    }

}
