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
public interface MyStockCloudMapper {
    @Select({"<script>",
            "select count(t1.id) from t_stock_cloud t1 left join t_commodity t2 on t1.cid = t2.id",
            "where t1.gid = #{gid}<if test='0 != sid'>and t1.sid = #{sid}</if> and t1.aid = #{aid} and t1.asid = #{asid} ",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ <= ]]> #{end}",
            "<if test='null != search'>and t2.name like #{search}</if> group by t1.cid",
            "</script>"})
    int count(int gid, int sid, int aid, int asid, Date start, Date end, String search);

    @Select({"<script>",
            "select t1.gid, t1.sid, t1.cid, sum(t1.price) as price, sum(t1.weight) as weight, sum(t1.value) as value, t2.code, t2.name, t2.cid as ctid, t2.remark",
            "from t_stock_cloud t1 left join t_commodity t2 on t1.cid = t2.id where t1.gid = #{gid}<if test='0 != sid'>and t1.sid = #{sid}</if>",
            "and t1.aid = #{aid} and t1.asid = #{asid} and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ <= ]]> #{end}",
            "<if test='null != search'>and t2.name like #{search}</if> group by t1.cid limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination(int offset, int limit, int gid, int sid, int aid, int asid, Date start, Date end, String search);

    @Select({"<script>",
            "select sid as id, sum(weight) as total from t_stock_cloud where gid = #{gid} <if test='0 != sid'>and sid = #{sid}</if>",
            "and t1.aid = #{aid} and t1.asid = #{asid} and cdate <![CDATA[ >= ]]> #{start} and cdate <![CDATA[ < ]]> #{end} group by sid",
            "</script>"})
    List<MyStockReport> selectReport(int gid, int sid, int aid, int asid, Date start, Date end);

    @Select({"<script>",
            "select t1.gid, t1.sid, t1.cid, sum(t1.price) as price, sum(t1.weight) as weight, sum(t1.value) as value,",
            "DATE(t1.cdate) as date, t2.code, t2.name, t2.cid as ctid, t2.remark from t_stock_cloud t1 left join t_commodity t2",
            "on t1.cid = t2.id where t1.gid = #{gid} and t1.sid = #{sid} and t1.aid = #{aid} and t1.asid = #{asid}",
            "and cdate <![CDATA[ >= ]]> #{start} and cdate <![CDATA[ <= ]]> #{end} group by t1.cid, date order by t1.cid, date",
            "</script>"})
    List<MyStockCommodity> selectHistory_all(int gid, int sid, int aid, int asid, Date start, Date end);

    @Select({"<script>",
            "select t1.gid, t1.sid, t1.cid, sum(t1.price) as price, sum(t1.weight) as weight, sum(t1.value) as value, DATE(t1.cdate) as date,",
            "t2.code, t2.name, t2.cid as ctid, t2.remark from t_stock_cloud t1 left join t_commodity t2 on t1.cid = t2.id",
            "where t1.gid = #{gid} and t1.sid = #{sid} and t1.aid = #{aid} and t1.asid = #{asid} and t1.cid = #{cid} and cdate <![CDATA[ >= ]]> #{start}",
            "and cdate <![CDATA[ <= ]]> #{end} group by t1.cid, date order by t1.cid, date",
            "</script>"})
    List<MyStockCommodity> selectHistory(int gid, int sid, int aid, int asid, int cid, Date start, Date end);
}
