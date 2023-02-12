package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.repository.model.MyOrderCommodity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * desc: 履约订单商品查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyAgreementOrderCommodityMapper {
    @Select({"<script>",
            "select t1.id, t1.oid, t1.cid, t1.ctype, t1.unit, t1.value, t1.price, t2.otype as io",
            "from t_agreement_order_commodity t1 left join t_agreement_order t2 on t1.oid = t2.id",
            "where t2.gid = #{gid} and t2.apply_time <![CDATA[ >= ]]> #{start}",
            "and t2.apply_time <![CDATA[ < ]]> #{end} and t2.review > 0",
            "</script>"})
    List<MyOrderCommodity> selectByGid(int gid, Date start, Date end);

    @Select({"<script>",
            "select t1.id, t1.oid, t1.cid, t1.ctype, t1.unit, t1.value, t1.price, t2.otype as io",
            "from t_agreement_order_commodity t1 left join t_agreement_order t2 on t1.oid = t2.id",
            "where t2.sid = #{sid} and t2.apply_time <![CDATA[ >= ]]> #{start}",
            "and t2.apply_time <![CDATA[ < ]]> #{end} and t2.review > 0",
            "</script>"})
    List<MyOrderCommodity> selectBySid(int sid, Date start, Date end);
}
