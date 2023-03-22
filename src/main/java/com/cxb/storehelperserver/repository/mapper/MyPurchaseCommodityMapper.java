package com.cxb.storehelperserver.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * desc: 采购订单商品查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyPurchaseCommodityMapper {
    @Select({"<script>select sum(value*price) as total from t_purchase_commodity where oid = #{oid} group by oid</script>"})
    BigDecimal count(int oid);
}
