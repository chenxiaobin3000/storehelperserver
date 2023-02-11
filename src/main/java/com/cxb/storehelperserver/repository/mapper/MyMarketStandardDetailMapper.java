package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.repository.model.MyMarketDetailReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;
import java.util.List;

/**
 * desc: 市场对接标品明细查询
 * auth: cxb
 * date: 2023/2/11
 */
@Mapper
public interface MyMarketStandardDetailMapper {
    @Select({"<script>",
            "select count(id) as id, 1 as type, 0 as cid, sum(value) as value, sum(price) as price,",
            "sum(price*value) as total, cdate from t_market_standard_detail where cdate <![CDATA[ >= ]]> #{start}",
            "and cdate <![CDATA[ < ]]> #{end} <if test='0 != mid'>and mid = #{mid}</if> group by cdate",
            "</script>"})
    List<MyMarketDetailReport> select(int mid, Date start, Date end);
}
