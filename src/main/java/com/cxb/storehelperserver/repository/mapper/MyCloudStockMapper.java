package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * desc: 云仓库存查询
 * auth: cxb
 * date: 2023/1/13
 */
@Mapper
public interface MyCloudStockMapper {
    @Select({"<script>",
            "select count(t1.id) from t_cloud_stock t1",
            "left join t_commodity t2 on t1.cid = t2.id",
            "where <if test='0 != gid'>t1.gid = #{gid}</if>",
            "<if test='0 == gid'>t1.sid = #{sid}</if>",
            "<if test='null != search'>and t2.name like #{search}</if>",
            "</script>"})
    int count(int gid, int sid, String search);

    @Select({"<script>",
            "select t1.id, t1.gid, t1.sid, t1.unit, t1.value, t1.price,",
            "t2.id as cid, t2.code, t2.name, t2.cid as ctid, t2.remark",
            "from t_cloud_stock t1 left join t_commodity t2 on t1.cid = t2.id",
            "where <if test='0 != gid'>t1.gid = #{gid}</if>",
            "<if test='0 == gid'>t1.sid = #{sid}</if>",
            "<if test='null != search'>and t2.name like #{search}</if>",
            "limit #{offset}, #{limit}",
            "</script>"})
    List<MyStockCommodity> pagination(int offset, int limit, int gid, int sid, String search);

    @Select({"<script>",
            "select sid as id, sum(value) as total from t_cloud_stock",
            "<if test='0 == sid'>where gid = #{gid} group by sid</if>",
            "<if test='0 != sid'>where t1.sid = #{sid}</if>",
            "</script>"})
    List<MyStockReport> selectReport(int gid, int sid);
}
