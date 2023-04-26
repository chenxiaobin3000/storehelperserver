package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * desc: 仓库关联商品查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyStorageCommodityMapper {
    @Select({"<script>",
            "select count(t1.id) from t_commodity_storage t1 left join t_commodity t2 on t1.cid = t2.id",
            "where t1.sid = #{sid} <if test='null != search'>and t2.name like #{search}</if>",
            "</script>"})
    int count(int sid, String search);

    @Select({"<script>",
            "select t2.id, t2.code, t2.name, t2.gid, t2.cid, t2.remark from t_commodity_storage t1",
            "left join t_commodity t2 on t1.cid = t2.id where t1.sid = #{sid}",
            "<if test='null != search'>and t2.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<TCommodity> pagination(int offset, int limit, int sid, String search);
}
