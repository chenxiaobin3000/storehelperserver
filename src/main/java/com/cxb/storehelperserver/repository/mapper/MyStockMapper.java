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
            "select count(t1.id) from t_stock t1 left join t_commodity t2 on t1.cid = t2.id",
            "where t1.gid = #{gid}<if test='0 != sid'>and t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ <= ]]> #{end}",
            "and t1.ctype = 1 <if test='null != search'>and t2.name like #{search}</if> group by t1.cid",
            "</script>"})
    int count_commodity(int gid, int sid, Date start, Date end, String search);

    @Select({"<script>",
            "select count(t1.id) from t_stock t1 left join t_halfgood t2 on t1.cid = t2.id",
            "where t1.gid = #{gid}<if test='0 != sid'>and t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ <= ]]> #{end}",
            "and t1.ctype = 2 <if test='null != search'>and t2.name like #{search}</if> group by t1.cid",
            "</script>"})
    int count_halfgood(int gid, int sid, Date start, Date end, String search);

    @Select({"<script>",
            "select count(t1.id) from t_stock t1 left join t_original t2 on t1.cid = t2.id",
            "where t1.gid = #{gid}<if test='0 != sid'>and t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ <= ]]> #{end}",
            "and t1.ctype = 3 <if test='null != search'>and t2.name like #{search}</if> group by t1.cid",
            "</script>"})
    int count_original(int gid, int sid, Date start, Date end, String search);

    @Select({"<script>",
            "select t1.gid, t1.sid, t1.cid, sum(t1.price) as price, sum(t1.weight) as weight, sum(t1.value) as value, t2.code, t2.name, t2.cid as ctid, t2.remark",
            "from t_stock t1 left join t_commodity t2 on t1.cid = t2.id where t1.gid = #{gid}<if test='0 != sid'>and t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ <= ]]> #{end} and t1.ctype = 1",
            "<if test='null != search'>and t2.name like #{search}</if> group by t1.cid limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination_commodity(int offset, int limit, int gid, int sid, Date start, Date end, String search);

    @Select({"<script>",
            "select t1.gid, t1.sid, t1.cid, sum(t1.price) as price, sum(t1.weight) as weight, sum(t1.value) as value, t2.code, t2.name, t2.cid as ctid, t2.remark",
            "from t_stock t1 left join t_halfgood t2 on t1.cid = t2.id where t1.gid = #{gid}<if test='0 != sid'>and t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ <= ]]> #{end} and t1.ctype = 2",
            "<if test='null != search'>and t2.name like #{search}</if> group by t1.cid limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination_halfgood(int offset, int limit, int gid, int sid, Date start, Date end, String search);

    @Select({"<script>",
            "select t1.gid, t1.sid, t1.cid, sum(t1.price) as price, sum(t1.weight) as weight, sum(t1.value) as value, t2.code, t2.name, t2.cid as ctid, t2.remark",
            "from t_stock t1 left join t_original t2 on t1.cid = t2.id where t1.gid = #{gid}<if test='0 != sid'>and t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ <= ]]> #{end} and t1.ctype = 3",
            "<if test='null != search'>and t2.name like #{search}</if> group by t1.cid limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination_original(int offset, int limit, int gid, int sid, Date start, Date end, String search);

    @Select({"<script>",
            "select sid as id, sum(weight) as total from t_stock where gid = #{gid} <if test='0 != sid'>and sid = #{sid}</if>",
            "and cdate <![CDATA[ >= ]]> #{start} and cdate <![CDATA[ < ]]> #{end} and ctype = #{ctype} group by sid",
            "</script>"})
    List<MyStockReport> selectReport(int gid, int sid, int ctype, Date start, Date end);

    @Select({"<script>",
            "select t1.gid, t1.sid, t1.ctype, t1.cid, sum(t1.price) as price, sum(t1.weight) as weight, sum(t1.value) as value,",
            "DATE(t1.cdate) as date, t2.code, t2.name, t2.cid as ctid, t2.remark from t_stock t1 left join t_commodity t2",
            "on t1.cid = t2.id where t1.gid = #{gid} and t1.sid = #{sid} and t1.ctype = 1 and cdate <![CDATA[ >= ]]> #{start}",
            "and cdate <![CDATA[ <= ]]> #{end} group by t1.cid, date order by t1.cid, date",
            "</script>"})
    List<MyStockCommodity> selectHistory_commodity_all(int gid, int sid, Date start, Date end);

    @Select({"<script>",
            "select t1.gid, t1.sid, t1.ctype, t1.cid, sum(t1.price) as price, sum(t1.weight) as weight, sum(t1.value) as value,",
            "DATE(t1.cdate) as date, t2.code, t2.name, t2.cid as ctid, t2.remark from t_stock t1 left join t_halfgood t2",
            "on t1.cid = t2.id where t1.gid = #{gid} and t1.sid = #{sid} and t1.ctype = 2 and cdate <![CDATA[ >= ]]> #{start}",
            "and cdate <![CDATA[ <= ]]> #{end} group by t1.cid, date order by t1.cid, date",
            "</script>"})
    List<MyStockCommodity> selectHistory_halfgood_all(int gid, int sid, Date start, Date end);

    @Select({"<script>",
            "select t1.gid, t1.sid, t1.ctype, t1.cid, sum(t1.price) as price, sum(t1.weight) as weight, sum(t1.value) as value,",
            "DATE(t1.cdate) as date, t2.code, t2.name, t2.cid as ctid, t2.remark from t_stock t1 left join t_original t2",
            "on t1.cid = t2.id where t1.gid = #{gid} and t1.sid = #{sid} and t1.ctype = 3 and cdate <![CDATA[ >= ]]> #{start}",
            "and cdate <![CDATA[ <= ]]> #{end} group by t1.cid, date order by t1.cid, date",
            "</script>"})
    List<MyStockCommodity> selectHistory_original_all(int gid, int sid, Date start, Date end);

    @Select({"<script>",
            "select t1.gid, t1.sid, t1.ctype, t1.cid, sum(t1.price) as price, sum(t1.weight) as weight, sum(t1.value) as value, DATE(t1.cdate) as date,",
            "t2.code, t2.name, t2.cid as ctid, t2.remark from t_stock t1 left join t_commodity t2 on t1.cid = t2.id",
            "where t1.gid = #{gid} and t1.sid = #{sid} and t1.ctype = 1 and t1.cid = #{cid} and cdate <![CDATA[ >= ]]> #{start}",
            "and cdate <![CDATA[ <= ]]> #{end} group by t1.cid, date order by t1.cid, date",
            "</script>"})
    List<MyStockCommodity> selectHistory_commodity(int gid, int sid, int cid, Date start, Date end);

    @Select({"<script>",
            "select t1.gid, t1.sid, t1.ctype, t1.cid, sum(t1.price) as price, sum(t1.weight) as weight, sum(t1.value) as value, DATE(t1.cdate) as date,",
            "t2.code, t2.name, t2.cid as ctid, t2.remark from t_stock t1 left join t_halfgood t2 on t1.cid = t2.id",
            "where t1.gid = #{gid} and t1.sid = #{sid} and t1.ctype = 2 and t1.cid = #{cid} and cdate <![CDATA[ >= ]]> #{start}",
            "and cdate <![CDATA[ <= ]]> #{end} group by t1.cid, date order by t1.cid, date",
            "</script>"})
    List<MyStockCommodity> selectHistory_halfgood(int gid, int sid, int cid, Date start, Date end);

    @Select({"<script>",
            "select t1.gid, t1.sid, t1.ctype, t1.cid, sum(t1.price) as price, sum(t1.weight) as weight, sum(t1.value) as value, DATE(t1.cdate) as date,",
            "t2.code, t2.name, t2.cid as ctid, t2.remark from t_stock t1 left join t_original t2 on t1.cid = t2.id",
            "where t1.gid = #{gid} and t1.sid = #{sid} and t1.ctype = 3 and t1.cid = #{cid} and cdate <![CDATA[ >= ]]> #{start}",
            "and cdate <![CDATA[ <= ]]> #{end} group by t1.cid, date order by t1.cid, date",
            "</script>"})
    List<MyStockCommodity> selectHistory_original(int gid, int sid, int cid, Date start, Date end);
}
