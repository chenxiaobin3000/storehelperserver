package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.model.TOrderReviewer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * desc: 用户小程序权限查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyUserRoleMpMapper {
    @Select({"<script>",
            "select t1.id as id, t3.uid as uid, t3.gid as gid, t1.pid as pid",
            "from t_role_permission_mp t1",
            "left join t_user_role_mp t2 on t1.rid = t2.rid",
            "left join t_user_group t3 on t2.uid = t3.uid",
            "where t3.gid = #{gid} and (t1.pid = #{p1} or t1.pid = #{p2} or t1.pid = #{p3})",
            "</script>"})
    List<TOrderReviewer> select(int gid, int p1, int p2, int p3);
}
