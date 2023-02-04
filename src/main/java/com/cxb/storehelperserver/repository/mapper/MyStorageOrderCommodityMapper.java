package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.repository.model.MyOrderCommodity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * desc: 仓储订单商品查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyStorageOrderCommodityMapper {
    @Select({"<script>",
            "select t1.id as id, t1.oid as oid, t1.cid as cid, t1.ctype as ctype,",
            "t1.unit as unit, t1.value as value, t1.price as price, t2.otype as io",
            "from t_storage_order_commodity t1",
            "left join t_storage_order t2 on t1.oid = t2.id",
            "where t2.gid = #{gid} and t2.apply_time <![CDATA[ >= ]]> #{start} and t2.apply_time <![CDATA[ < ]]> #{end} and t2.review > 0",
            "</script>"})
    List<MyOrderCommodity> selectByGid(int gid, Date start, Date end);

    @Select({"<script>",
            "select t1.id as id, t1.oid as oid, t1.cid as cid, t1.ctype as ctype,",
            "t1.unit as unit, t1.value as value, t1.price as price, t2.otype as io",
            "from t_storage_order_commodity t1",
            "left join t_storage_order t2 on t1.oid = t2.id",
            "where t2.sid = #{sid} and t2.apply_time <![CDATA[ >= ]]> #{start} and t2.apply_time <![CDATA[ < ]]> #{end} and t2.review > 0",
            "</script>"})
    List<MyOrderCommodity> selectBySid(int sid, Date start, Date end);
}
