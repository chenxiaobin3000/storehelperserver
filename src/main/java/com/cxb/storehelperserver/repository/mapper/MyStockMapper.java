package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * desc: 仓库库存明细查询
 * auth: cxb
 * date: 2023/1/13
 */
@Mapper
public interface MyStockMapper {
    @Select({"<script>",
            "select sid as id, sum(weight) as total from t_stock where gid = #{gid} <if test='0 != sid'>and sid = #{sid}</if>",
            "and cdate <![CDATA[ >= ]]> #{start} and cdate <![CDATA[ < ]]> #{end} group by sid",
            "</script>"})
    List<MyStockReport> selectReport(int gid, int sid, Date start, Date end);

    @Select({"<script>",
            "select t1.gid, t1.sid, t1.cid, sum(t1.price) as price, sum(t1.weight) as weight, group_concat(norm) as norm, sum(t1.value) as value,",
            "date(t1.cdate) as date, t2.code, t2.name, t2.cid as ctid, t2.remark from t_stock t1 left join t_commodity t2",
            "on t1.cid = t2.id where t1.gid = #{gid} and t1.sid = #{sid} and cdate <![CDATA[ >= ]]> #{start}",
            "and cdate <![CDATA[ <= ]]> #{end} group by t1.cid, date order by t1.cid, date",
            "</script>"})
    List<MyStockCommodity> selectHistory_all(int gid, int sid, Date start, Date end);

    @Select({"<script>",
            "select gid, sid, cid, sum(price) as price, sum(weight) as weight, group_concat(norm) as norm, sum(value) as value, date(cdate) as date",
            "from t_stock where gid = #{gid} and sid = #{sid} and cid = #{cid} and cdate <![CDATA[ >= ]]> #{start}",
            "and cdate <![CDATA[ <= ]]> #{end} group by cid, date order by cid, date",
            "</script>"})
    List<MyStockCommodity> selectHistory(int gid, int sid, int cid, Date start, Date end);
}
