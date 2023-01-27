package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.repository.model.MyStockStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * desc: 仓库标品查询
 * auth: cxb
 * date: 2023/1/13
 */
@Mapper
public interface MyStockStandardMapper {
    @Select({"<script>",
            "select count(t1.id) from t_storage_standard t1",
            "left join t_standard t2 on t1.stid = t2.id",
            "where t1.sid = #{sid} and t1.cdate = #{date}",
            "<if test='null != search'>",
            "and t2.name like #{search}",
            "</if>",
            "</script>"})
    int countByExample(int sid, Date date, String search);

    @Select({"<script>",
            "select t1.id as id, t1.unit as unit, t1.value as value, t1.total as total, t1.price as price,",
            "t2.id as cid, t2.code as code, t2.name as name, t2.cid as ctid, t2.remark as remark",
            "from t_storage_standard t1",
            "left join t_standard t2 on t1.stid = t2.id",
            "where t1.sid = #{sid} and t1.cdate = #{date}",
            "<if test='null != search'>",
            "and t2.name like #{search}",
            "</if>",
            "limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockStandard> selectByExample(int offset, int limit, int sid, Date date, String search);
}
