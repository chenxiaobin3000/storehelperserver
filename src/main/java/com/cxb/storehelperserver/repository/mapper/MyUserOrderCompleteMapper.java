package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.model.TUserOrderComplete;
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
            "select count(id) from t_user_order_complete where (aid = #{aid} or cid = #{cid})",
            "<if test='null != search'>and batch like %#{search}%</if>",
            "</script>"})
    int count(int aid, int cid, String search);

    @Select({"<script>",
            "select aid, cid, gid, sid, otype, oid, batch, cdate, ctime from t_user_order_complete",
            "where (aid = #{aid} or cid = #{cid}) <if test='null != search'>and batch like %#{search}%</if>",
            "order by ctime desc limit #{offset}, #{limit}",
            "</script>"})
    List<TUserOrderComplete> pagination(int offset, int limit, int aid, int cid, String search);

    @Select({"<script>",
            "select count(t1.id) as cnum, sum(t2.value) as ctotal, t1.otype, t1.cdate",
            "from t_user_order_complete t1 left join t_agreement_commodity t2 on t1.oid = t2.oid",
            "where <if test='0 == sid'>t1.gid = #{gid}</if> <if test='0 != sid'>t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ <= ]]> #{end}",
            "and t1.otype <![CDATA[ >= ]]> #{type1} and t1.otype <![CDATA[ <= ]]> #{type2} group by t1.otype, t1.cdate",
            "</script>"})
    List<MyUserOrderComplete> selectByAgreement(int gid, int sid, int type1, int type2, Date start, Date end);

    @Select({"<script>",
            "select count(t1.id) as cnum, sum(t2.value) as ctotal, t1.otype, t1.cdate",
            "from t_user_order_complete t1 left join t_product_commodity t2 on t1.oid = t2.oid",
            "where <if test='0 == sid'>t1.gid = #{gid}</if> <if test='0 != sid'>t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ <= ]]> #{end}",
            "and t1.otype <![CDATA[ >= ]]> #{type1} and t1.otype <![CDATA[ <= ]]> #{type2} group by t1.otype, t1.cdate",
            "</script>"})
    List<MyUserOrderComplete> selectByProduct(int gid, int sid, int type1, int type2, Date start, Date end);

    @Select({"<script>",
            "select count(t1.id) as cnum, sum(t2.value) as ctotal, t1.otype, t1.cdate",
            "from t_user_order_complete t1 left join t_purchase_commodity t2 on t1.oid = t2.oid",
            "where <if test='0 == sid'>t1.gid = #{gid}</if> <if test='0 != sid'>t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ <= ]]> #{end}",
            "and t1.otype <![CDATA[ >= ]]> #{type1} and t1.otype <![CDATA[ <= ]]> #{type2} group by t1.otype, t1.cdate",
            "</script>"})
    List<MyUserOrderComplete> selectByPurchase(int gid, int sid, int type1, int type2, Date start, Date end);

    @Select({"<script>",
            "select count(t1.id) as cnum, sum(t2.value) as ctotal, t1.otype, t1.cdate",
            "from t_user_order_complete t1 left join t_storage_commodity t2 on t1.oid = t2.oid",
            "where <if test='0 == sid'>t1.gid = #{gid}</if> <if test='0 != sid'>t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ <= ]]> #{end}",
            "and t1.otype <![CDATA[ >= ]]> #{type1} and t1.otype <![CDATA[ <= ]]> #{type2} group by t1.otype, t1.cdate",
            "</script>"})
    List<MyUserOrderComplete> selectByStorage(int gid, int sid, int type1, int type2, Date start, Date end);

    @Select({"<script>",
            "select count(t1.id) as cnum, sum(t2.value) as ctotal, t1.otype, t1.cdate",
            "from t_user_order_complete t1 left join t_sale_commodity t2 on t1.oid = t2.oid",
            "where <if test='0 == sid'>t1.gid = #{gid}</if> <if test='0 != sid'>t1.sid = #{sid}</if>",
            "and t1.cdate <![CDATA[ >= ]]> #{start} and t1.cdate <![CDATA[ <= ]]> #{end}",
            "and t1.otype <![CDATA[ >= ]]> #{type1} and t1.otype <![CDATA[ <= ]]> #{type2} group by t1.otype, t1.cdate",
            "</script>"})
    List<MyUserOrderComplete> selectBySale(int gid, int sid, int type1, int type2, Date start, Date end);
}
