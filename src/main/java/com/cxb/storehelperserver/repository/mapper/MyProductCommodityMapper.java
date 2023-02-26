package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.repository.model.MyOrderCommodity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * desc: 生产订单商品查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyProductCommodityMapper {
    @Select({"<script>",
            "select t1.id, t1.oid, t1.cid, t1.ctype, t1.unit, t1.value, t1.price, t2.otype as io",
            "from t_product_commodity t1 left join t_product_order t2 on t1.oid = t2.id",
            "where <if test='0 != gid'>t2.gid = #{gid}</if><if test='0 == gid'>t2.sid = #{sid}</if>",
            "and t2.apply_time <![CDATA[ >= ]]> #{start} and t2.apply_time <![CDATA[ < ]]> #{end} and t2.review > 0",
            "</script>"})
    List<MyOrderCommodity> pagination(int gid, Date start, Date end);
}
