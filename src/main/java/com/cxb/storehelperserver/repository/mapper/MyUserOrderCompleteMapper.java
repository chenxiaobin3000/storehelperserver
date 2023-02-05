package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.repository.model.MyUserOrderComplete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;
import java.util.List;

/**
 * desc: 用户完成订单查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyUserOrderCompleteMapper {
    @Select({"<script>",
            "select count(t1.id) as cnum, sum(t2.value) as ctotal, t1.otype as otype, t1.cdate as cdate",
            "from t_user_order_complete t1 left join t_agreement_order_commodity t2 on t1.oid = t2.oid",
            "where <if test='0 == sid'>t1.gid = #{gid}</if><if test='0 != sid'>t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ < ]]> #{end}",
            "and (t1.otype=#{type1} or t1.otype=#{type2}) group by t1.otype, t1.cdate",
            "</script>"})
    List<MyUserOrderComplete> selectByAgreement(int gid, int sid, int type1, int type2, Date start, Date end);

    @Select({"<script>",
            "select count(t1.id) as cnum, sum(t2.value) as ctotal, t1.otype as otype, t1.cdate as cdate",
            "from t_user_order_complete t1 left join t_product_order_commodity t2 on t1.oid = t2.oid",
            "where <if test='0 == sid'>t1.gid = #{gid}</if><if test='0 != sid'>t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ < ]]> #{end}",
            "and (t1.otype=#{type1} or t1.otype=#{type2}) group by t1.otype, t1.cdate",
            "</script>"})
    List<MyUserOrderComplete> selectByProduct(int gid, int sid, int type1, int type2, Date start, Date end);

    @Select({"<script>",
            "select count(t1.id) as cnum, sum(t2.value) as ctotal, t1.otype as otype, t1.cdate as cdate",
            "from t_user_order_complete t1 left join t_storage_order_commodity t2 on t1.oid = t2.oid",
            "where <if test='0 == sid'>t1.gid = #{gid}</if><if test='0 != sid'>t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ < ]]> #{end}",
            "and (t1.otype=#{type1} or t1.otype=#{type2}) group by t1.otype, t1.cdate",
            "</script>"})
    List<MyUserOrderComplete> selectByStorage(int gid, int sid, int type1, int type2, Date start, Date end);
}
