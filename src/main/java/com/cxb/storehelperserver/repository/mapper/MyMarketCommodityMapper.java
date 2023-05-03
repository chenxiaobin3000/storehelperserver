package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.model.TCommodity;
import com.cxb.storehelperserver.model.TMarketCommodity;
import com.cxb.storehelperserver.repository.model.MyMarketCommodity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;
import java.util.List;

/**
 * desc: 市场对接仓库商品查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyMarketCommodityMapper {
    @Select({"<script>",
            "select count(t1.id) from t_market_commodity t1 left join t_commodity t2 on t1.cid = t2.id where t1.aid = #{aid} and t2.name like #{search}",
            "</script>"})
    int count(int aid, String search);

    @Select({"<script>",
            "select t1.id, t1.mid, t1.cid, t1.price, t2.code, t2.name, t2.cid as cate, t2.remark from t_market_commodity t1 left join t_commodity t2",
            "on t1.cid = t2.id where t1.aid = #{aid} <if test='null != search'>and t2.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<MyMarketCommodity> pagination(int offset, int limit, int aid, String search);

    @Select({"<script>",
            "select id from t_market_commodity_detail where sid = #{sid} and aid = #{aid} and cdate = #{cdate}",
            "</script>"})
    List<Integer> findIds(int sid, int aid, Date cdate);

    @Select({"<script>",
            "select count(t1.id) from t_market_commodity t1 left join t_market_commodity_detail t2",
            "on t1.sid = t2.sid and t1.aid = t2.aid and t1.cid = t2.cid where t1.sid = #{sid}",
            "and t1.aid = #{aid} <if test='null != search'>and t1.name like #{search}</if>",
            "</script>"})
    int countDetail(int sid, int aid, String search);

    @Select({"<script>",
            "select t2.id, t1.mid, t1.cid, t1.code, t1.name, t1.remark, t1.price as alarm, t2.price, t2.value from t_market_commodity t1",
            "left join (select id, sid, aid, cid, price, value, cdate from t_market_commodity_detail where cdate = #{cdate})",
            "t2 on t1.sid = t2.sid and t1.aid = t2.aid and t1.cid = t2.cid where t1.sid = #{sid} and t1.aid = #{aid}",
            "<if test='null != search'>and t1.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<MyMarketCommodity> paginationDetail(int offset, int limit, int sid, int aid, Date cdate, String search);

    @Select({"<script>",
            "select t2.cid, t2.code, t2.name, t2.remark, t1.price, t1.value from t_market_commodity_detail t1 left join t_market_commodity t2",
            "on t1.aid = t2.aid and and t1.cid = t2.cid where t1.sid = #{sid} and t1.aid = #{aid} and t1.cdate = #{cdate}",
            "</script>"})
    List<MyMarketCommodity> sale(int aid, Date cdate);
}
