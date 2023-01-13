package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.model.TStorageCommodity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * desc: 仓库商店查询
 * auth: cxb
 * date: 2023/1/13
 */
@Mapper
public interface MyStorageCommodityMapper {
    @Select({"<script>",
            "select count(t1.id) from t_storage_commodity t1",
            "left join t_commodity t2 on t1.cid = t2.id",
            "where t1.sid = #{sid}",
            "<if test='null != search'>",
            "and t2.name like #{search}",
            "</if>",
            "</script>"})
    int countByExample(int sid, String search);

    @Select({"<script>",
            "select t2.id as id, t2.code as code, t2.name as name, t2.gid as gid,",
            "t2.atid as atid, t2.cid as cid, t2.price as price, t2.remark as remark",
            "from t_storage_commodity t1",
            "left join t_commodity t2 on t1.cid = t2.id",
            "where t1.sid = #{sid}",
            "<if test='null != search'>",
            "and t2.name like #{search}",
            "</if>",
            "limit #{offset}, #{limit}",
            "</script>"})
    List<TStorageCommodity> selectByExample(int offset, int limit, int sid, String search);
}
