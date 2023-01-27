package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.model.TStockCommodity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * desc: 仓库商品查询
 * auth: cxb
 * date: 2023/1/13
 */
@Mapper
public interface MyStockCommodityMapper {
    @Select({"<script>",
            "select count(t1.id) from t_stock_commodity t1",
            "left join t_commodity t2 on t1.cid = t2.id",
            "where t1.sid = #{sid} and t1.cdate = #{date}",
            "<if test='null != search'>",
            "and t2.name like #{search}",
            "</if>",
            "</script>"})
    int countByExample(int sid, Date date, String search);

    @Select({"<script>",
            "select t2.id as id, t2.code as code, t2.name as name, t2.gid as gid,",
            "t2.atid as atid, t2.cid as cid, t2.price as price, t2.remark as remark",
            "from t_stock_commodity t1",
            "left join t_commodity t2 on t1.cid = t2.id",
            "where t1.sid = #{sid} and t1.cdate = #{date}",
            "<if test='null != search'>",
            "and t2.name like #{search}",
            "</if>",
            "limit #{offset}, #{limit}",
            "</script>"})
    List<TStockCommodity> selectByExample(int offset, int limit, int sid, Date date, String search);
}
