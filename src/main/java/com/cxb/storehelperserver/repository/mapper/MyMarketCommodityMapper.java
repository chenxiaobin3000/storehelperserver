package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.repository.model.MyOrderCommodity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * desc: 市场对接商品查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyMarketCommodityMapper {
    @Select({"<script>",
            "select t1.id as id, t1.oid as oid, t1.cid as cid, t1.ctype as ctype,",
            "t1.unit as unit, t1.value as value, t1.price as price, t2.otype as io",
            "from t_agreement_order_commodity t1 left join t_agreement_order t2 on t1.oid = t2.id",
            "where t2.gid = #{gid} and t2.apply_time <![CDATA[ >= ]]> #{start} and t2.apply_time <![CDATA[ < ]]> #{end} and t2.review > 0",
            "</script>"})
    List<MyOrderCommodity> selectByGid(int gid, Date start, Date end);

    @Select({"<script>",
            "select t1.id as id, t1.code as code, t1.name as name, t1.cid as cid, t1.price as price,",
            "t1.unit as unit, t1.remark as remark, t2.id as mcid, t2.name as mname, t2.price as alarm",
            "from t_commodity as t1 left join t_market_commodity as t2 on t1.id = t2.cid",
            "where t1.gid = #{gid} order by t2.ctime, t1.ctime",
            "</script>"})
    List<MyOrderCommodity> selectBySid(int gid);
}
