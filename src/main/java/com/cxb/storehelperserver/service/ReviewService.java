package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * desc: 采购业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ReviewService {
    @Resource
    private CheckService checkService;

    @Resource
    private UserOrderApplyRepository userOrderApplyRepository;

    @Resource
    private UserOrderReviewRepository userOrderReviewRepository;

    @Resource
    private UserOrderCompleteRepository userOrderCompleteRepository;

    @Resource
    private OrderReviewerRepository orderReviewerRepository;

    public RestResult apply(int id, int gid, int sid, int otype, int oid, String batch, List<Integer> reviews) {
        // 添加用户订单冗余信息
        TUserOrderApply apply = new TUserOrderApply();
        apply.setUid(id);
        apply.setGid(gid);
        apply.setSid(sid);
        apply.setOtype(otype);
        apply.setOid(oid);
        apply.setBatch(batch);
        if (!userOrderApplyRepository.insert(apply)) {
            return RestResult.fail("添加用户订单信息失败");
        }

        // 添加用户订单审核信息
        TUserOrderReview review = new TUserOrderReview();
        review.setGid(gid);
        review.setSid(sid);
        review.setOtype(otype);
        review.setOid(oid);
        review.setBatch(batch);
        for (Integer reviewer : reviews) {
            review.setId(0);
            review.setUid(reviewer);
            if (!userOrderReviewRepository.insert(review)) {
                return RestResult.fail("添加用户订单审核信息失败");
            }
        }
        return RestResult.ok();
    }

    public RestResult update(int otype, int oid, int sid) {
        val userOrderApply = userOrderApplyRepository.find(otype, oid);
        userOrderApply.setSid(sid);
        if (!userOrderApplyRepository.update(userOrderApply)) {
            return RestResult.fail("修改用户订单信息失败");
        }

        val userOrderReviews = userOrderReviewRepository.find(otype, oid);
        for (TUserOrderReview review : userOrderReviews) {
            review.setSid(sid);
            if (!userOrderReviewRepository.update(review)) {
                return RestResult.fail("修改用户订单审核信息失败");
            }
        }
        return null;
    }

    public RestResult delete(Integer review, int otype, int oid) {
        if (null == review) {
            if (!userOrderApplyRepository.delete(otype, oid)) {
                return RestResult.fail("删除订单申请人失败");
            }
            if (!userOrderReviewRepository.delete(otype, oid)) {
                return RestResult.fail("删除订单审核人失败");
            }
        } else {
            if (!userOrderCompleteRepository.delete(otype, oid)) {
                return RestResult.fail("删除完成订单失败");
            }
        }
        return RestResult.ok();
    }

    public boolean checkReview(int id, int otype, int oid) {
        val reviews = userOrderReviewRepository.find(otype, oid);
        boolean find = false;
        for (TUserOrderReview review : reviews) {
            if (review.getUid().equals(id)) {
                find = true;
                break;
            }
        }
        return find;
    }

    public RestResult review(int id, int gid, int sid, int otype, int oid, String batch, Date applyTime) {
        // 删除apply和review信息
        if (!userOrderApplyRepository.delete(otype, oid)) {
            return RestResult.fail("删除用户订单信息失败");
        }
        if (!userOrderReviewRepository.delete(otype, oid)) {
            return RestResult.fail("添加用户订单审核信息失败");
        }

        // 插入complete信息
        TUserOrderComplete complete = new TUserOrderComplete();
        complete.setUid(id);
        complete.setGid(gid);
        complete.setSid(sid);
        complete.setOtype(otype);
        complete.setOid(oid);
        complete.setBatch(batch);
        complete.setCdate(applyTime);
        if (!userOrderCompleteRepository.insert(complete)) {
            return RestResult.fail("完成用户订单审核信息失败");
        }
        return RestResult.ok();
    }

    public RestResult revoke(int id, int gid, int sid, int otype, int oid, String batch, int perm) {
        val orderReviewers = orderReviewerRepository.find(gid);
        if (null == orderReviewers || orderReviewers.isEmpty()) {
            return RestResult.fail("未设置订单审核人，请联系系统管理员");
        }
        val reviews = new ArrayList<Integer>();
        for (TOrderReviewer orderReviewer : orderReviewers) {
            if (orderReviewer.getPid().equals(perm)) {
                reviews.add(orderReviewer.getUid());
            }
        }

        if (!userOrderCompleteRepository.delete(otype, oid)) {
            return RestResult.fail("删除用户订单完成信息失败");
        }

        // 添加用户订单冗余信息
        TUserOrderApply apply = new TUserOrderApply();
        apply.setUid(id);
        apply.setGid(gid);
        apply.setSid(sid);
        apply.setOtype(otype);
        apply.setOid(oid);
        apply.setBatch(batch);
        if (!userOrderApplyRepository.insert(apply)) {
            return RestResult.fail("添加用户订单信息失败");
        }

        // 添加用户订单审核信息
        TUserOrderReview review = new TUserOrderReview();
        review.setGid(gid);
        review.setSid(sid);
        review.setOtype(otype);
        review.setOid(oid);
        review.setBatch(batch);
        for (Integer reviewer : reviews) {
            review.setId(0);
            review.setUid(reviewer);
            if (!userOrderReviewRepository.insert(review)) {
                return RestResult.fail("添加用户订单审核信息失败");
            }
        }
        return null;
    }

    public RestResult checkPerm(int id, int gid, int applyPerm, int reviewPerm, List<Integer> reviews) {
        // 校验申请订单权限
        if (!checkService.checkRolePermissionMp(id, applyPerm)) {
            return RestResult.fail("本账号没有相关的权限，请联系管理员");
        }

        // 验证审核人员信息
        val orderReviewers = orderReviewerRepository.find(gid);
        if (null == orderReviewers || orderReviewers.isEmpty()) {
            return RestResult.fail("未设置订单审核人，请联系系统管理员");
        }
        for (TOrderReviewer orderReviewer : orderReviewers) {
            if (orderReviewer.getPid().equals(reviewPerm)) {
                reviews.add(orderReviewer.getUid());
            }
        }
        if (reviews.isEmpty()) {
            return RestResult.fail("未设置采购订单审核人，请联系系统管理员");
        }
        return null;
    }
}