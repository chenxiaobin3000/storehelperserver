package com.cxb.storehelperserver.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * desc: 订单审核撤销
 * auth: cxb
 * date: 2023/1/23
 */
@Mapper
public interface MyOrderMapper {
    @Update({"<script>update t_agreement_order set review = null, review_time = null where id = #{id}</script>"})
    int setAgreementReviewNull(int id);

    @Update({"<script>update t_cloud_order set review = null, review_time = null where id = #{id}</script>"})
    int setCloudReviewNull(int id);

    @Update({"<script>update t_product_order set review = null, review_time = null where id = #{id}</script>"})
    int setProductReviewNull(int id);

    @Update({"<script>update t_purchase_order set review = null, review_time = null where id = #{id}</script>"})
    int setPurchaseReviewNull(int id);

    @Update({"<script>update t_storage_order set review = null, review_time = null where id = #{id}</script>"})
    int setStorageReviewNull(int id);
}
