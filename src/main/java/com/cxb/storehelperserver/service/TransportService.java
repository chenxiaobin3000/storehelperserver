package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.repository.PurchaseFareRepository;
import com.cxb.storehelperserver.service.model.PageData;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.cxb.storehelperserver.util.TypeDefine.CompleteType.COMPLETE_NOT;
import static com.cxb.storehelperserver.util.TypeDefine.ReviewType.REVIEW_HAS;

/**
 * desc:
 * auth: cxb
 * date: 2023/4/24
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class TransportService {
    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private StorageOrderService storageOrderService;

    @Resource
    private AgreementOrderService agreementOrderService;

    @Resource
    private PurchaseFareRepository purchaseFareRepository;

    public RestResult getPurchaseFareList(int id, int gid, int type, int page, int limit, String date, String search) {
        int total = purchaseOrderService.total(gid, type, REVIEW_HAS, COMPLETE_NOT, date, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = purchaseOrderService.pagination(gid, type, page, limit, REVIEW_HAS, COMPLETE_NOT, date, search);
        return RestResult.ok();
    }

    public RestResult getStorageFareList(int id, int gid, int type, int page, int limit, String date, String search) {
        int total = storageOrderService.total(gid, type, REVIEW_HAS, date, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = storageOrderService.pagination(gid, type, page, limit, REVIEW_HAS, date, search);
        return RestResult.ok();
    }

    public RestResult getAgreementFareList(int id, int gid, int aid, int asid, int type, int page, int limit, String date, String search) {
        int total = agreementOrderService.total(gid, aid, asid, type, REVIEW_HAS, COMPLETE_NOT, date, search);
        if (0 == total) {
            return RestResult.ok(new PageData());
        }
        val list = agreementOrderService.pagination(gid, aid, asid, type, page, limit, REVIEW_HAS, COMPLETE_NOT, date, search);
        return RestResult.ok();
    }
}
