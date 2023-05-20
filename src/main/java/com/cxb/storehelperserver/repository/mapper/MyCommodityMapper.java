package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * desc: 商品订单数据统计查询
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyCommodityMapper {
    @Select({"<script>select sum(value*price) as total from t_purchase_commodity where oid = #{oid} group by oid</script>"})
    BigDecimal count_purchase(int oid);

    @Select({"<script>select sum(value*price) as total from t_storage_commodity where oid = #{oid} group by oid</script>"})
    BigDecimal count_storage(int oid);

    @Select({"<script>",
            "select count(id) from t_agreement_order where gid = #{gid} <if test='aid != 0'>and aid = #{aid}</if> and otype = #{type} <if test='review == 2'>and review > 0</if>",
            "<if test='review == 3'>and review is null</if><if test='complete == 1'>and complete = 1</if> <if test='complete == 2'>and complete = 0</if>",
            "and apply_time <![CDATA[ >= ]]> #{start} and apply_time <![CDATA[ < ]]> #{end} and id in (select oid from t_agreement_commodity where cid in",
            "<foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>#{id}</foreach> group by oid)",
            "</script>"})
    int countByAgreement(int gid, int aid, int type, int review, int complete, Date start, Date end, List<Integer> ids);

    @Select({"<script>",
            "select count(id) from t_offline_order where gid = #{gid} <if test='aid != 0'>and aid = #{aid}</if> and otype = #{type} <if test='review == 2'>and review > 0</if>",
            "<if test='review == 3'>and review is null</if><if test='complete == 1'>and complete = 1</if> <if test='complete == 2'>and complete = 0</if>",
            "and apply_time <![CDATA[ >= ]]> #{start} and apply_time <![CDATA[ < ]]> #{end} and id in (select oid from t_offline_commodity where cid in",
            "<foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>#{id}</foreach> group by oid)",
            "</script>"})
    int countByOffline(int gid, int aid, int type, int review, int complete, Date start, Date end, List<Integer> ids);

    @Select({"<script>",
            "select count(id) from t_product_order where gid = #{gid} and otype = #{type} <if test='review == 2'>and review > 0</if>",
            "<if test='review == 3'>and review is null</if> and apply_time <![CDATA[ >= ]]> #{start} and apply_time <![CDATA[ < ]]> #{end}",
            "and id in (select oid from t_product_commodity where cid in",
            "<foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>#{id}</foreach> group by oid)",
            "</script>"})
    int countByProduct(int gid, int type, int review, Date start, Date end, List<Integer> ids);

    @Select({"<script>",
            "select count(id) from t_purchase_order where gid = #{gid} and otype = #{type} <if test='review == 2'>and review > 0</if>",
            "<if test='review == 3'>and review is null</if><if test='complete == 1'>and complete = 1</if> <if test='complete == 2'>and complete = 0</if>",
            "and apply_time <![CDATA[ >= ]]> #{start} and apply_time <![CDATA[ < ]]> #{end} and id in (select oid from t_purchase_commodity where cid in",
            "<foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>#{id}</foreach> group by oid)",
            "</script>"})
    int countByPurchase(int gid, int type, int review, int complete, Date start, Date end, List<Integer> ids);

    @Select({"<script>",
            "select count(id) from t_sale_order where gid = #{gid} and otype = #{type} <if test='review == 2'>and review > 0</if>",
            "<if test='review == 3'>and review is null</if> and apply_time <![CDATA[ >= ]]> #{start} and apply_time <![CDATA[ < ]]> #{end}",
            "and id in (select oid from t_sale_commodity where cid in",
            "<foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>#{id}</foreach> group by oid)",
            "</script>"})
    int countBySale(int gid, int type, int review, Date start, Date end, List<Integer> ids);

    @Select({"<script>",
            "select count(id) from t_storage_order where gid = #{gid} and otype = #{type} <if test='review == 2'>and review > 0</if>",
            "<if test='review == 3'>and review is null</if> and apply_time <![CDATA[ >= ]]> #{start} and apply_time <![CDATA[ < ]]> #{end}",
            "and id in (select oid from t_storage_commodity where cid in",
            "<foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>#{id}</foreach> group by oid)",
            "</script>"})
    int countByStorage(int gid, int type, int review, Date start, Date end, List<Integer> ids);

    @Select({"<script>",
            "select id, gid, aid, batch, otype, price, cur_price as curPrice, value, cur_value as curValue, apply, apply_time as applyTime, review, review_time as reviewTime, complete",
            "from t_agreement_order where gid = #{gid} <if test='aid != 0'>and aid = #{aid}</if> and otype = #{type} <if test='review == 2'>and review > 0</if>",
            "<if test='review == 3'>and review is null</if> <if test='complete == 1'>and complete = 1</if> <if test='complete == 2'>and complete = 0</if>",
            "and apply_time <![CDATA[ >= ]]> #{start} and apply_time <![CDATA[ < ]]> #{end} and id in (select oid from t_agreement_commodity where cid in",
            "<foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>#{id}</foreach> group by oid) limit #{offset}, #{limit}",
            "</script>"})
    List<TAgreementOrder> paginationByAgreement(int gid, int aid, int type, int offset, int limit, int review, int complete, Date start, Date end, List<Integer> ids);

    @Select({"<script>",
            "select id, gid, aid, batch, otype, price, cur_price as curPrice, value, cur_value as curValue, apply, apply_time as applyTime, review, review_time as reviewTime, complete",
            "from t_offline_order where gid = #{gid} <if test='aid != 0'>and aid = #{aid}</if> and otype = #{type} <if test='review == 2'>and review > 0</if>",
            "<if test='review == 3'>and review is null</if> <if test='complete == 1'>and complete = 1</if> <if test='complete == 2'>and complete = 0</if>",
            "and apply_time <![CDATA[ >= ]]> #{start} and apply_time <![CDATA[ < ]]> #{end} and id in (select oid from t_offline_commodity where cid in",
            "<foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>#{id}</foreach> group by oid) limit #{offset}, #{limit}",
            "</script>"})
    List<TOfflineOrder> paginationByOffline(int gid, int aid, int type, int offset, int limit, int review, int complete, Date start, Date end, List<Integer> ids);

    @Select({"<script>",
            "select id, gid, aid, batch, otype, price, cur_price as curPrice, value, cur_value as curValue, apply, apply_time as applyTime, review, review_time as reviewTime, complete",
            "from t_product_order where gid = #{gid} and otype = #{type} <if test='review == 2'>and review > 0</if>",
            "<if test='review == 3'>and review is null</if> and apply_time <![CDATA[ >= ]]> #{start} and apply_time <![CDATA[ < ]]> #{end}",
            "and id in (select oid from t_product_commodity where cid in",
            "<foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>#{id}</foreach> group by oid) limit #{offset}, #{limit}",
            "</script>"})
    List<TProductOrder> paginationByProduct(int gid, int type, int offset, int limit, int review, Date start, Date end, List<Integer> ids);

    @Select({"<script>",
            "select id, gid, aid, batch, otype, price, cur_price as curPrice, value, cur_value as curValue, apply, apply_time as applyTime, review, review_time as reviewTime, complete",
            "from t_purchase_order where gid = #{gid} and otype = #{type} <if test='review == 2'>and review > 0</if>",
            "<if test='review == 3'>and review is null</if> <if test='complete == 1'>and complete = 1</if> <if test='complete == 2'>and complete = 0</if>",
            "and apply_time <![CDATA[ >= ]]> #{start} and apply_time <![CDATA[ < ]]> #{end} and id in (select oid from t_purchase_commodity where cid in",
            "<foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>#{id}</foreach> group by oid) limit #{offset}, #{limit}",
            "</script>"})
    List<TPurchaseOrder> paginationByPurchase(int gid, int type, int offset, int limit,int review, int complete, Date start, Date end, List<Integer> ids);

    @Select({"<script>",
            "select id, gid, aid, batch, otype, price, cur_price as curPrice, value, cur_value as curValue, apply, apply_time as applyTime, review, review_time as reviewTime, complete",
            "from t_sale_order where gid = #{gid} <if test='aid != 0'>and aid = #{aid}</if> and otype = #{type} <if test='review == 2'>and review > 0</if>",
            "<if test='review == 3'>and review is null</if> and apply_time <![CDATA[ >= ]]> #{start} and apply_time <![CDATA[ < ]]> #{end}",
            "and id in (select oid from t_sale_commodity where cid in",
            "<foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>#{id}</foreach> group by oid) limit #{offset}, #{limit}",
            "</script>"})
    List<TSaleOrder> paginationBySale(int gid, int type, int offset, int limit, int review, Date start, Date end, List<Integer> ids);

    @Select({"<script>",
            "select id, gid, aid, batch, otype, price, cur_price as curPrice, value, cur_value as curValue, apply, apply_time as applyTime, review, review_time as reviewTime, complete",
            "from t_storage_order where gid = #{gid} <if test='aid != 0'>and aid = #{aid}</if> and otype = #{type} <if test='review == 2'>and review > 0</if>",
            "<if test='review == 3'>and review is null</if> and apply_time <![CDATA[ >= ]]> #{start} and apply_time <![CDATA[ < ]]> #{end}",
            "and id in (select oid from t_storage_commodity where cid in",
            "<foreach collection='ids' item='id' index='index' open='(' close=')' separator=','>#{id}</foreach> group by oid) limit #{offset}, #{limit}",
            "</script>"})
    List<TStorageOrder> paginationByStorage(int gid, int type, int offset, int limit, int review, Date start, Date end, List<Integer> ids);
}
