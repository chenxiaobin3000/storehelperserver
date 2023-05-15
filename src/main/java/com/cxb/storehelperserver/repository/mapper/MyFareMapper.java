package com.cxb.storehelperserver.repository.mapper;

import com.cxb.storehelperserver.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * desc: 运费审核撤销
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyFareMapper {
    @Update({"<script>update t_agreement_fare set review = null, review_time = null where oid = #{oid}</script>"})
    int setAgreementFareReviewNull(int oid);

    @Update({"<script>update t_purchase_fare set review = null, review_time = null where oid = #{oid}</script>"})
    int setPurchaseFareReviewNull(int oid);

    @Update({"<script>update t_product_fare set review = null, review_time = null where oid = #{oid}</script>"})
    int setProductFareReviewNull(int oid);

    @Update({"<script>update t_offline_fare set review = null, review_time = null where oid = #{oid}</script>"})
    int setOfflineFareReviewNull(int oid);

    @Update({"<script>update t_storage_fare set review = null, review_time = null where oid = #{oid}</script>"})
    int setStorageFareReviewNull(int oid);

    @Select({"<script>",
            "select count(t1.id) from t_agreement_fare t1 left join t_agreement_order t2 on t1.oid = t2.id <if test='sid != 0'>",
            "left join t_agreement_storage t3 on t2.id = t3.oid</if> where t2.gid = #{gid} <if test='sid != 0'>and t3.sid = #{sid}</if>",
            "<if test='aid != 0'>and t2.aid = #{aid}</if> and t2.otype = #{type} and t2.apply_time <![CDATA[ >= ]]> #{start} and t2.apply_time <![CDATA[ < ]]> #{end}",
            "<if test='review == 2'>and t2.review is not null</if><if test='review == 3'>and t2.review is null</if>",
            "<if test='complete == 1'>and t2.complete = 1</if><if test='complete == 2'>and t2.complete = 0</if>",
            "</script>"})
    int countAgreementOrder(int gid, int aid, int sid, int type, int review, int complete, Date start, Date end);

    @Select({"<script>",
            "select t1.id, t1.oid, t1.ship, t1.code, t1.phone, t1.fare, t1.remark, t1.cdate from t_agreement_fare t1 left join t_agreement_order t2",
            "on t1.oid = t2.id <if test='sid != 0'>left join t_agreement_storage t3 on t2.id = t3.oid</if> where t2.gid = #{gid}",
            "<if test='sid != 0'>and t3.sid = #{sid}</if> <if test='aid != 0'>and t2.aid = #{aid}</if> and t2.otype = #{type} and t2.apply_time <![CDATA[ >= ]]> #{start}",
            "and t2.apply_time <![CDATA[ < ]]> #{end} <if test='review == 2'>and t2.review is not null</if><if test='review == 3'>and t2.review is null</if>",
            "<if test='complete == 1'>and t2.complete = 1</if><if test='complete == 2'>and t2.complete = 0</if> limit #{offset}, #{limit}",
            "</script>"})
    List<TAgreementFare> paginationAgreementOrder(int offset, int limit, int gid, int aid, int sid, int type, int review, int complete, Date start, Date end);

    @Select({"<script>",
            "select count(t1.id) from t_purchase_fare t1 left join t_purchase_order t2 on t1.oid = t2.id <if test='sid != 0'>",
            "left join t_purchase_storage t3 on t2.id = t3.oid</if> where t2.gid = #{gid} <if test='sid != 0'>and t3.sid = #{sid}</if>",
            "<if test='supplier != 0'>and t2.supplier = #{supplier}</if> and t2.otype = #{type} and t2.apply_time <![CDATA[ >= ]]> #{start} and t2.apply_time <![CDATA[ < ]]> #{end}",
            "<if test='review == 2'>and t2.review is not null</if><if test='review == 3'>and t2.review is null</if>",
            "<if test='complete == 1'>and t2.complete = 1</if><if test='complete == 2'>and t2.complete = 0</if>",
            "</script>"})
    int countPurchaseOrder(int gid, int sid, int supplier, int type, int review, int complete, Date start, Date end);

    @Select({"<script>",
            "select t1.id, t1.oid, t1.ship, t1.code, t1.phone, t1.fare, t1.remark, t1.cdate from t_purchase_fare t1 left join t_purchase_order t2",
            "on t1.oid = t2.id <if test='sid != 0'>left join t_purchase_storage t3 on t2.id = t3.oid</if> where t2.gid = #{gid}",
            "<if test='sid != 0'>and t3.sid = #{sid}</if><if test='supplier != 0'>and t2.supplier = #{supplier}</if> and t2.otype = #{type} and t2.apply_time <![CDATA[ >= ]]> #{start}",
            "and t2.apply_time <![CDATA[ < ]]> #{end} <if test='review == 2'>and t2.review is not null</if><if test='review == 3'>and t2.review is null</if>",
            "<if test='complete == 1'>and t2.complete = 1</if><if test='complete == 2'>and t2.complete = 0</if> limit #{offset}, #{limit}",
            "</script>"})
    List<TPurchaseFare> paginationPurchaseOrder(int offset, int limit, int gid, int sid, int supplier, int type, int review, int complete, Date start, Date end);

    @Select({"<script>",
            "select count(t1.id) from t_product_fare t1 left join t_product_order t2 on t1.oid = t2.id <if test='sid != 0'>",
            "left join t_product_storage t3 on t2.id = t3.oid</if> where t2.gid = #{gid} <if test='sid != 0'>and t3.sid = #{sid}</if>",
            "and t2.otype = #{type} and t2.apply_time <![CDATA[ >= ]]> #{start} and t2.apply_time <![CDATA[ < ]]> #{end}",
            "<if test='review == 2'>and t2.review is not null</if><if test='review == 3'>and t2.review is null</if>",
            "<if test='complete == 1'>and t2.complete = 1</if><if test='complete == 2'>and t2.complete = 0</if>",
            "</script>"})
    int countProductOrder(int gid, int sid, int type, int review, int complete, Date start, Date end);

    @Select({"<script>",
            "select t1.id, t1.oid, t1.ship, t1.code, t1.phone, t1.fare, t1.remark, t1.cdate from t_product_fare t1 left join t_product_order t2",
            "on t1.oid = t2.id <if test='sid != 0'>left join t_product_storage t3 on t2.id = t3.oid</if> where t2.gid = #{gid}",
            "<if test='sid != 0'>and t3.sid = #{sid}</if> and t2.otype = #{type} and t2.apply_time <![CDATA[ >= ]]> #{start}",
            "and t2.apply_time <![CDATA[ < ]]> #{end} <if test='review == 2'>and t2.review is not null</if><if test='review == 3'>and t2.review is null</if>",
            "<if test='complete == 1'>and t2.complete = 1</if><if test='complete == 2'>and t2.complete = 0</if> limit #{offset}, #{limit}",
            "</script>"})
    List<TProductFare> paginationProductOrder(int offset, int limit, int gid, int sid, int type, int review, int complete, Date start, Date end);

    @Select({"<script>",
            "select count(t1.id) from t_offline_fare t1 left join t_offline_order t2 on t1.oid = t2.id <if test='sid != 0'>",
            "left join t_offline_storage t3 on t2.id = t3.oid</if> where t2.gid = #{gid} <if test='sid != 0'>and t3.sid = #{sid}</if>",
            "<if test='aid != 0'>and t2.aid = #{aid}</if> and t2.otype = #{type} and t2.apply_time <![CDATA[ >= ]]> #{start} and t2.apply_time <![CDATA[ < ]]> #{end}",
            "<if test='review == 2'>and t2.review is not null</if><if test='review == 3'>and t2.review is null</if>",
            "<if test='complete == 1'>and t2.complete = 1</if><if test='complete == 2'>and t2.complete = 0</if>",
            "</script>"})
    int countOfflineOrder(int gid, int aid, int sid, int type, int review, int complete, Date start, Date end);

    @Select({"<script>",
            "select t1.id, t1.oid, t1.ship, t1.code, t1.phone, t1.fare, t1.remark, t1.cdate from t_offline_fare t1 left join t_offline_order t2",
            "on t1.oid = t2.id <if test='sid != 0'>left join t_offline_storage t3 on t2.id = t3.oid</if> where t2.gid = #{gid}",
            "<if test='sid != 0'>and t3.sid = #{sid}</if> <if test='aid != 0'>and t2.aid = #{aid}</if> and t2.otype = #{type} and t2.apply_time <![CDATA[ >= ]]> #{start}",
            "and t2.apply_time <![CDATA[ < ]]> #{end} <if test='review == 2'>and t2.review is not null</if><if test='review == 3'>and t2.review is null</if>",
            "<if test='complete == 1'>and t2.complete = 1</if><if test='complete == 2'>and t2.complete = 0</if> limit #{offset}, #{limit}",
            "</script>"})
    List<TOfflineFare> paginationOfflineOrder(int offset, int limit, int gid, int aid, int sid, int type, int review, int complete, Date start, Date end);

    @Select({"<script>",
            "select count(t1.id) from t_storage_fare t1 left join t_storage_order t2 on t1.oid = t2.id where t2.gid = #{gid}",
            "<if test='sid != 0'>and t2.sid = #{sid}</if> and t2.otype = #{type} and t2.apply_time <![CDATA[ >= ]]> #{start} and t2.apply_time <![CDATA[ < ]]> #{end}",
            "<if test='review == 2'>and t2.review is not null</if><if test='review == 3'>and t2.review is null</if>",
            "<if test='complete == 1'>and t2.complete = 1</if><if test='complete == 2'>and t2.complete = 0</if>",
            "</script>"})
    int countStorageOrder(int gid, int sid, int type, int review, int complete, Date start, Date end);

    @Select({"<script>",
            "select t1.id, t1.oid, t1.ship, t1.code, t1.phone, t1.fare, t1.remark, t1.cdate from t_storage_fare t1 left join t_storage_order t2",
            "on t1.oid = t2.id where t2.gid = #{gid} <if test='sid != 0'>and t2.sid = #{sid}</if> and t2.otype = #{type} and t2.apply_time <![CDATA[ >= ]]> #{start}",
            "and t2.apply_time <![CDATA[ < ]]> #{end} <if test='review == 2'>and t2.review is not null</if><if test='review == 3'>and t2.review is null</if>",
            "<if test='complete == 1'>and t2.complete = 1</if><if test='complete == 2'>and t2.complete = 0</if> limit #{offset}, #{limit}",
            "</script>"})
    List<TStorageFare> paginationStorageOrder(int offset, int limit, int gid, int sid, int type, int review, int complete, Date start, Date end);
}
