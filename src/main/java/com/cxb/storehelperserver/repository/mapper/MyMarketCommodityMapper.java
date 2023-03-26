package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.model.TMarketCommodity;
import com.cxb.storehelperserver.repository.model.MyMarketCommodity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;
import java.util.List;

/**
 * desc: 市场对接云仓商品查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyMarketCommodityMapper {
    @Select({"<script>",
            "select count(t1.id) from t_commodity_cloud t1 left join t_market_commodity t2 on t1.cid = t2.cid",
            "and t1.sid = t2.sid where t1.sid = #{sid} <if test='0 != mid'>and t2.mid = #{mid}</if>",
            "<if test='null != search'>and t2.code like #{search}</if>",
            "</script>"})
    int count(int sid, int mid, String search);

    @Select({"<script>",
            "select t2.id, t2.sid, t2.mid, t1.cid, t2.code, t2.name, t2.remark, t2.price from t_commodity_cloud t1",
            "left join t_market_commodity t2 on t1.cid = t2.cid and t1.sid = t2.sid where t1.sid = #{sid} <if test='0 != mid'>and t2.mid = #{mid}</if>",
            "<if test='null != search'>and t2.code like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<TMarketCommodity> pagination(int offset, int limit, int sid, int mid, String search);

    @Select({"<script>",
            "select count(t1.id) from t_market_commodity t1 left join t_market_commodity_detail t2 on t1.sid = t2.sid and t1.mid = t2.mid and t1.cid = t2.cid",
            "where t1.sid = #{sid} and t1.mid = #{mid} <if test='null != search'>and t1.code like #{search}</if>",
            "</script>"})
    int countDetail(int sid, int mid, String search);

    @Select({"<script>",
            "select t2.id, t1.sid, t1.mid, t1.cid, t1.code, t1.name, t1.remark, t1.price as alarm, t2.price, t2.value from t_market_commodity t1",
            "left join (select id, sid, mid, cid, price, value, cdate from t_market_commodity_detail where cdate = #{cdate})",
            "t2 on t1.sid = t2.sid and t1.mid = t2.mid and t1.cid = t2.cid where t1.sid = #{sid} and t1.mid = #{mid}",
            "<if test='null != search'>and t1.code like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<MyMarketCommodity> paginationDetail(int offset, int limit, int sid, int mid, Date cdate, String search);
}
