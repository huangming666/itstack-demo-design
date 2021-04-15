package org.itstack.demo.design;

import org.itstack.demo.design.store.ICommodity;
import org.itstack.demo.design.store.impl.CardCommodityService;
import org.itstack.demo.design.store.impl.CouponCommodityService;
import org.itstack.demo.design.store.impl.GoodsCommodityService;

import java.util.HashMap;

public class StoreFactory {

    public static HashMap<Integer,ICommodity>  WELFARE = new HashMap<>();

    static {
        WELFARE.put(1,new CouponCommodityService());
        WELFARE.put(2,new GoodsCommodityService());
        WELFARE.put(3,new CardCommodityService());
    }

    public ICommodity getCommodityService(Integer commodityType) {
        return WELFARE.get(commodityType);
    }

}
