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
    int count_commodity(int sid, String search);

    @Select({"<script>",
            "select count(t1.id) from t_halfgood_storage t1 left join t_halfgood t2 on t1.cid = t2.id",
            "where t1.sid = #{sid} <if test='null != search'>and t2.name like #{search}</if>",
            "</script>"})
    int count_halfgood(int sid, String search);

    @Select({"<script>",
            "select count(t1.id) from t_original_storage t1 left join t_original t2 on t1.cid = t2.id",
            "where t1.sid = #{sid} <if test='null != search'>and t2.name like #{search}</if>",
            "</script>"})
    int count_original(int sid, String search);

    @Select({"<script>",
            "select t2.id, t2.code, t2.name, t2.gid, t2.cid, t2.remark from t_commodity_storage t1",
            "left join t_commodity t2 on t1.cid = t2.id where t1.sid = #{sid}",
            "<if test='null != search'>and t2.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<TCommodity> pagination_commodity(int offset, int limit, int sid, String search);

    @Select({"<script>",
            "select t2.id, t2.code, t2.name, t2.gid, t2.cid, t2.remark from t_halfgood_storage t1",
            "left join t_halfgood t2 on t1.cid = t2.id where t1.sid = #{sid}",
            "<if test='null != search'>and t2.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<THalfgood> pagination_halfgood(int offset, int limit, int sid, String search);

    @Select({"<script>",
            "select t2.id, t2.code, t2.name, t2.gid, t2.cid, t2.remark from t_original_storage t1",
            "left join t_original t2 on t1.cid = t2.id where t1.sid = #{sid}",
            "<if test='null != search'>and t2.name like #{search}</if> limit #{offset}, #{limit}",
            "</script>"})
    List<TOriginal> pagination_original(int offset, int limit, int sid, String search);
}
