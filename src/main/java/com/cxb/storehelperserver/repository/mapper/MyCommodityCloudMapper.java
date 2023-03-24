package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.model.TMarketCommodity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * desc: 市场对接商品查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyCommodityCloudMapper {
    @Select({"<script>",
            "select count(t1.id) from t_commodity_cloud t1 left join t_market_commodity t2 on t1.cid = t2.cid",
            "where t1.sid = #{sid} and t2.mid = #{mid} <if test='null != search'>and t2.code like #{search}</if>",
            "</script>"})
    int count(int sid, int mid, String search);

    @Select({"<script>",
            "select t2.cid, t2.code, t2.name, t2.remark, t2.price from t_commodity_cloud t1",
            "left join t_market_commodity t2 on t1.cid = t2.cid where t1.sid = #{sid} and t2.mid = #{mid}",
            "<if test='null != search'>and t2.code like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<TMarketCommodity> pagination(int offset, int limit, int sid, int mid, String search);
}
