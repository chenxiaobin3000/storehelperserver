package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.repository.model.MyStockHalfgood;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;
import java.util.List;

/**
 * desc: 仓库半成品查询
 * auth: cxb
 * date: 2023/1/13
 */
@Mapper
public interface MyStockHalfgoodDayMapper {
    @Select({"<script>",
            "select count(t1.id) from t_stock_halfgood_day t1",
            "left join t_halfgood t2 on t1.cid = t2.id",
            "where t1.sid = #{sid} and t1.cdate = #{date}",
            "<if test='null != search'>and t2.name like #{search}</if>",
            "</script>"})
    int countBySid(int sid, Date date, String search);

    @Select({"<script>",
            "select t1.id, t1.gid, t1.sid, t1.unit, t1.value, t1.price,",
            "t2.id as cid, t2.code, t2.name, t2.cid as ctid, t2.remark",
            "from t_stock_halfgood_day t1 left join t_halfgood t2 on t1.cid = t2.id",
            "where t1.sid = #{sid} and t1.cdate = #{date}",
            "<if test='null != search'>and t2.name like #{search}</if>",
            "limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockHalfgood> selectBySid(int offset, int limit, int sid, Date date, String search);

    @Select({"<script>",
            "select count(t1.id) from t_stock_halfgood_day t1",
            "left join t_halfgood t2 on t1.cid = t2.id",
            "where t1.gid = #{gid} and t1.cdate = #{date}",
            "<if test='null != search'>and t2.name like #{search}</if>",
            "</script>"})
    int countByGid(int gid, Date date, String search);

    @Select({"<script>",
            "select t1.id, t1.gid, t1.sid, t1.unit, t1.value, t1.price,",
            "t2.id as cid, t2.code, t2.name, t2.cid as ctid, t2.remark",
            "from t_stock_halfgood_day t1 left join t_halfgood t2 on t1.cid = t2.id",
            "where t1.gid = #{gid} and t1.cdate = #{date}",
            "<if test='null != search'>and t2.name like #{search}</if>",
            "limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockHalfgood> selectByGid(int offset, int limit, int gid, Date date, String search);

    @Select({"<script>",
            "select sid as id, sum(value) as total, cdate from t_stock_halfgood_day where gid = #{gid}",
            "and cdate <![CDATA[ >= ]]> #{start} and cdate <![CDATA[ < ]]> #{end} group by sid, cdate",
            "</script>"})
    List<MyStockReport> selectReport(int gid, Date start, Date end);
}
