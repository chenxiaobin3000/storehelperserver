package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.model.TMarketCommodity;
import com.cxb.storehelperserver.model.TMarketStandard;
import com.cxb.storehelperserver.repository.model.MyMarketReport;
import com.cxb.storehelperserver.repository.model.MyMarketSaleInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;
import java.util.List;

/**
 * desc: 市场查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyMarketMapper {
    @Select({"<script>",
            "select count(id) as id, 1 as type, 0 as cid, sum(value) as value, sum(price) as price,",
            "sum(price * value) as total, cdate from t_market_commodity_detail where gid = #{gid}",
            "<if test='0 != mid'>and mid = #{mid}</if> and cdate <![CDATA[ >= ]]> #{start}",
            "and cdate <![CDATA[ < ]]> #{end} group by cdate",
            "</script>"})
    List<MyMarketReport> select_commodity(int gid, int mid, Date start, Date end);

    @Select({"<script>",
            "select count(id) as id, 1 as type, 0 as cid, sum(value) as value, sum(price) as price,",
            "sum(price * value) as total, cdate from t_market_standard_detail where gid = #{gid}",
            "<if test='0 != mid'>and mid = #{mid}</if> and cdate <![CDATA[ >= ]]> #{start}",
            "and cdate <![CDATA[ < ]]> #{end} group by cdate",
            "</script>"})
    List<MyMarketReport> select_standard(int gid, int mid, Date start, Date end);

    @Select({"<script>",
            "select cid, sum(value) as value, sum(value * price) as total, cdate",
            "from t_market_commodity_detail where gid = #{gid} <if test='0 != mid'>and mid = #{mid}</if>",
            "and cid in(<foreach collection='cids' separator=',' item='id'>#{id}</foreach>) group by cid, cdate",
            "</script>"})
    List<MyMarketSaleInfo> selectInCids_commodity(int gid, int mid, List<Integer> cids);

    @Select({"<script>",
            "select cid, sum(value) as value, sum(value * price) as total, cdate",
            "from t_market_standard_detail where gid = #{gid} <if test='0 != mid'>and mid = #{mid}</if>",
            "and cid in(<foreach collection='cids' separator=',' item='id'>#{id}</foreach>) group by cid, cdate",
            "</script>"})
    List<MyMarketSaleInfo> selectInCids_standard(int gid, int mid, List<Integer> cids);
}
