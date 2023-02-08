package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.model.TUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * desc: 用户查询
 * auth: cxb
 * date: 2023/1/2
 */
@Mapper
public interface MyUserMapper {
    @Select({"<script>",
            "select count(t1.id) from t_user_group t1 left join t_user t2 on t1.uid = t2.id",
            "where t1.gid = #{gid} <if test='null != search'> and t2.name like #{search} </if>",
            "</script>"})
    int countByExample(int gid, String search);

    @Select({"<script>",
            "select t2.id as id, t2.name as name, t2.phone as phone",
            "from t_user_group t1 left join t_user t2 on t1.uid = t2.id where t1.gid = #{gid}",
            "<if test='null != search'> and t2.name like #{search} </if> limit #{offset}, #{limit}",
            "</script>"})
    List<TUser> selectByExample(int offset, int limit, int gid, String search);
}
