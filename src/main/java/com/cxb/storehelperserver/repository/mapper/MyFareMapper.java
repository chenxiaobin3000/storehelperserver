package com.cxb.storehelperserver.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

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

    @Update({"<script>update t_storage_fare set review = null, review_time = null where oid = #{oid}</script>"})
    int setStorageFareReviewNull(int oid);
}
