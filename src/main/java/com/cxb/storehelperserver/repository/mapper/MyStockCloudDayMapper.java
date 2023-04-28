package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;
import java.util.List;

/**
 * desc: 仓库库存日快照查询
 * auth: cxb
 * date: 2023/1/13
 */
@Mapper
public interface MyStockCloudDayMapper {
    @Select({"<script>",
            "select count(t1.id) from t_stock_cloud_day t1 left join t_commodity t2 on t1.cid = t2.id where t1.sid = #{sid}",
            "and t1.aid = #{aid} and t1.asid = #{asid} and t1.cdate = #{date} <if test='null != search'>and t2.name like #{search}</if>",
            "</script>"})
    int count(int sid, int aid, int asid, Date date, String search);

    @Select({"<script>",
            "select t1.id, t1.gid, t1.sid, t1.cid, t1.price, t1.weight, t1.value, t2.code, t2.name, t2.cid as ctid, t2.remark",
            "from t_stock_cloud_day t1 left join t_commodity t2 on t1.cid = t2.id where t1.sid = #{sid} and t1.aid = #{aid} and t1.asid = #{asid}",
            "and t1.cdate = #{date} <if test='null != search'>and t2.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination(int offset, int limit, int sid, int aid, int asid, Date date, String search);

    @Select({"<script>",
            "select t2.id, t2.gid, t2.sid, t1.cid, t2.price, t2.weight, t2.value, t3.code, t3.name, t3.cid as ctid, t3.remark",
            "from t_commodity_storage t1 left join (select id, gid, sid, cid, price, weight, value, cdate from t_stock_cloud_day",
            "where sid = #{sid} and t1.aid = #{aid} and t1.asid = #{asid} and cdate = #{date}) t2 on t1.cid = t2.cid left join t_commodity t3 on t1.cid = t3.id",
            "where t1.sid = #{sid} <if test='null != search'>and t3.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination_all(int offset, int limit, int sid, int aid, int asid, Date date, String search);

    @Select({"<script>",
            "select sid as id, sum(weight) as total, cdate from t_stock_cloud_day where gid = #{gid} <if test='0 != sid'>and sid = #{sid}</if>",
            "and t1.aid = #{aid} and t1.asid = #{asid} and cdate <![CDATA[ >= ]]> #{start} and cdate <![CDATA[ < ]]> #{end} group by sid, cdate",
            "</script>"})
    List<MyStockReport> selectReport(int gid, int sid, int aid, int asid, Date start, Date end);
}
