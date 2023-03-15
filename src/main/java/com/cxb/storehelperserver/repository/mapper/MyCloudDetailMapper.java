package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.repository.model.MyStockDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * desc: 云仓库存明细查询
 * auth: cxb
 * date: 2023/1/13
 */
@Mapper
public interface MyCloudDetailMapper {
    @Select({"<script>",
            "select count(t1.id) from t_cloud_detail t1",
            "left join t_commodity t2 on t1.cid = t2.id",
            "where t1.gid = #{gid}<if test='0 != sid'>and t1.sid = #{sid}</if>",
            "<if test='null != search'>and t2.name like #{search}</if>",
            "</script>"})
    int count(int gid, int sid, String search);

    @Select({"<script>",
            "select t1.id, t1.gid, t1.sid, t1.price, t1.weight, t1.value,",
            "t2.id as cid, t2.code, t2.name, t2.cid as ctid, t2.remark",
            "from t_cloud_detail t1 left join t_commodity t2 on t1.cid = t2.id",
            "where t1.gid = #{gid}<if test='0 != sid'>and t1.sid = #{sid}</if>",
            "<if test='null != search'>and t2.name like #{search}</if>",
            "limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockDetail> pagination(int offset, int limit, int gid, int sid, String search);
}
