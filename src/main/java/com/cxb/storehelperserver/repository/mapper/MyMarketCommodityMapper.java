package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.repository.model.MyMarketCommodity;
import com.cxb.storehelperserver.repository.model.MyMarketDetailCommodity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;
import java.util.List;

/**
 * desc: 市场对接商品查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyMarketCommodityMapper {
    @Select({"<script>",
            "select t1.id as id, t1.code as code, t1.name as name, t1.cid as cid, t1.price as price,",
            "t1.unit as unit, t1.remark as remark, t2.id as mcid, t2.name as mname, t2.price as alarm",
            "from t_commodity t1 left join (select id, cid, name, price, ctime from t_market_commodity where mid = #{mid}) as t2",
            "on t1.id = t2.cid where t1.gid = #{gid} <if test='null != search'>and t1.name like #{search}</if>",
            "order by t2.ctime desc, t1.ctime desc limit #{offset}, #{limit}",
            "</script>"})
    List<MyMarketCommodity> select(int offset, int limit, int gid, int mid, String search);

    @Select({"<script>",
            "select t2.id as id, t1.cid as cid, t1.name as mname, t2.value as value, t2.price as mprice,",
            "t3.code as code, t3.name as name, t3.price as price from t_market_commodity t1",
            "left join (select id, cid, value, price from t_market_commodity_detail where gid = #{gid} and mid = #{mid}",
            "and cdate = #{date}) as t2 on t1.cid = t2.cid left join t_commodity t3 on t1.cid = t3.id",
            "where t1.gid = #{gid} and t1.mid= #{mid} order by t1.ctime desc",
            "</script>"})
    List<MyMarketDetailCommodity> selectDetail(int offset, int limit, int gid, int mid, Date date, String search);
}
