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
public interface MyStockDayMapper {
    @Select({"<script>",
            "select count(t1.id) from t_stock_day t1 left join t_commodity t2 on t1.cid = t2.id where t1.sid = #{sid}",
            "and t1.ctype = 1 and t1.cdate = #{date} <if test='null != search'>and t2.name like #{search}</if>",
            "</script>"})
    int count_commodity(int sid, Date date, String search);

    @Select({"<script>",
            "select count(t1.id) from t_stock_day t1 left join t_halfgood t2 on t1.cid = t2.id where t1.sid = #{sid}",
            "and t1.ctype = 2 and t1.cdate = #{date} <if test='null != search'>and t2.name like #{search}</if>",
            "</script>"})
    int count_halfgood(int sid, Date date, String search);

    @Select({"<script>",
            "select count(t1.id) from t_stock_day t1 left join t_original t2 on t1.cid = t2.id where t1.sid = #{sid}",
            "and t1.ctype = 3 and t1.cdate = #{date} <if test='null != search'>and t2.name like #{search}</if>",
            "</script>"})
    int count_original(int sid, Date date, String search);

    @Select({"<script>",
            "select count(t1.id) from t_stock_day t1 left join t_standard t2 on t1.cid = t2.id where t1.sid = #{sid}",
            "and t1.ctype = 4 and t1.cdate = #{date} <if test='null != search'>and t2.name like #{search}</if>",
            "</script>"})
    int count_standard(int sid, Date date, String search);

    @Select({"<script>",
            "select t1.id, t1.gid, t1.sid, t1.cid, t1.price, t1.weight, t1.value, t2.code, t2.name, t2.cid as ctid, t2.remark",
            "from t_stock_day t1 left join t_commodity t2 on t1.cid = t2.id where t1.sid = #{sid} and t1.ctype = 1",
            "and t1.cdate = #{date} <if test='null != search'>and t2.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination_commodity(int offset, int limit, int sid, Date date, String search);

    @Select({"<script>",
            "select t1.id, t1.gid, t1.sid, t1.cid, t1.price, t1.weight, t1.value, t2.code, t2.name, t2.cid as ctid, t2.remark",
            "from t_stock_day t1 left join t_halfgood t2 on t1.cid = t2.id where t1.sid = #{sid} and t1.ctype = 2",
            "and t1.cdate = #{date} <if test='null != search'>and t2.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination_halfgood(int offset, int limit, int sid, Date date, String search);

    @Select({"<script>",
            "select t1.id, t1.gid, t1.sid, t1.cid, t1.price, t1.weight, t1.value, t2.code, t2.name, t2.cid as ctid, t2.remark",
            "from t_stock_day t1 left join t_original t2 on t1.cid = t2.id where t1.sid = #{sid} and t1.ctype = 3",
            "and t1.cdate = #{date} <if test='null != search'>and t2.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination_original(int offset, int limit, int sid, Date date, String search);

    @Select({"<script>",
            "select t1.id, t1.gid, t1.sid, t1.cid, t1.price, t1.weight, t1.value, t2.code, t2.name, t2.cid as ctid, t2.remark",
            "from t_stock_day t1 left join t_standard t2 on t1.cid = t2.id where t1.sid = #{sid} and t1.ctype = 4",
            "and t1.cdate = #{date} <if test='null != search'>and t2.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination_standard(int offset, int limit, int sid, Date date, String search);

    @Select({"<script>",
            "select t2.id, t2.gid, t2.sid, t1.cid, t2.price, t2.weight, t2.value, t3.code, t3.name, t3.cid as ctid, t3.remark",
            "from t_commodity_storage t1 left join (select id, gid, sid, ctype, cid, price, weight, value, cdate from t_stock_day",
            "where sid = #{sid} and ctype = 1 and cdate = #{date}) t2 on t1.cid = t2.cid left join t_commodity t3 on t1.cid = t3.id",
            "where t1.sid = #{sid} <if test='null != search'>and t3.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination_commodity_all(int offset, int limit, int sid, Date date, String search);

    @Select({"<script>",
            "select t2.id, t2.gid, t2.sid, t1.cid, t2.price, t2.weight, t2.value, t3.code, t3.name, t3.cid as ctid, t3.remark",
            "from t_halfgood_storage t1 left join (select id, gid, sid, ctype, cid, price, weight, value, cdate from t_stock_day",
            "where sid = #{sid} and ctype = 2 and cdate = #{date}) t2 on t1.cid = t2.cid left join t_halfgood t3 on t1.cid = t3.id",
            "where t1.sid = #{sid} <if test='null != search'>and t3.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination_halfgood_all(int offset, int limit, int sid, Date date, String search);

    @Select({"<script>",
            "select t2.id, t2.gid, t2.sid, t1.cid, t2.price, t2.weight, t2.value, t3.code, t3.name, t3.cid as ctid, t3.remark",
            "from t_original_storage t1 left join (select id, gid, sid, ctype, cid, price, weight, value, cdate from t_stock_day",
            "where sid = #{sid} and ctype = 3 and cdate = #{date}) t2 on t1.cid = t2.cid left join t_original t3 on t1.cid = t3.id",
            "where t1.sid = #{sid} <if test='null != search'>and t3.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination_original_all(int offset, int limit, int sid, Date date, String search);

    @Select({"<script>",
            "select t2.id, t2.gid, t2.sid, t1.cid, t2.price, t2.weight, t2.value, t3.code, t3.name, t3.cid as ctid, t3.remark",
            "from t_standard_storage t1 left join (select id, gid, sid, ctype, cid, price, weight, value, cdate from t_stock_day",
            "where sid = #{sid} and ctype = 4 and cdate = #{date}) t2 on t1.cid = t2.cid left join t_standard t3 on t1.cid = t3.id",
            "where t1.sid = #{sid} <if test='null != search'>and t3.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination_standard_all(int offset, int limit, int sid, Date date, String search);

    @Select({"<script>",
            "select sid as id, sum(weight) as total, cdate from t_stock_day where gid = #{gid} <if test='0 != sid'>and sid = #{sid}</if>",
            "and ctype = #{ctype} and cdate <![CDATA[ >= ]]> #{start} and cdate <![CDATA[ < ]]> #{end} group by sid, cdate",
            "</script>"})
    List<MyStockReport> selectReport(int gid, int sid, int ctype, Date start, Date end);
}
