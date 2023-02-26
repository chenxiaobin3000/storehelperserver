package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.cloud.*;
import com.cxb.storehelperserver.model.TCloud;
import com.cxb.storehelperserver.model.TCloudOrder;
import com.cxb.storehelperserver.service.CloudMgrService;
import com.cxb.storehelperserver.service.CloudService;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.cxb.storehelperserver.util.TypeDefine.OrderType.*;

/**
 * desc: 云仓接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/cloud")
public class CloudController {
    @Resource
    private CloudMgrService cloudMgrService;

    @Resource
    private CloudService cloudService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/addCloud")
    public RestResult addCloud(@Validated @RequestBody AddCloudValid req) {
        TCloud cloud = new TCloud();
        cloud.setGid(req.getGid());
        cloud.setArea(Long.valueOf(req.getArea()));
        cloud.setContact(req.getContact());
        cloud.setName(req.getName());
        cloud.setAddress(req.getAddress());
        return cloudMgrService.addCloud(req.getId(), cloud);
    }

    @PostMapping("/setCloud")
    public RestResult setCloud(@Validated @RequestBody SetCloudValid req) {
        TCloud cloud = new TCloud();
        cloud.setId(req.getSid());
        cloud.setGid(req.getGid());
        cloud.setArea(Long.valueOf(req.getArea()));
        cloud.setContact(req.getContact());
        cloud.setName(req.getName());
        cloud.setAddress(req.getAddress());
        return cloudMgrService.setCloud(req.getId(), cloud);
    }

    @PostMapping("/delCloud")
    public RestResult delCloud(@Validated @RequestBody DelCloudValid req) {
        return cloudMgrService.delCloud(req.getId(), req.getGid(), req.getSid());
    }

    @PostMapping("/getGroupCloud")
    public RestResult getGroupCloud(@Validated @RequestBody GetGroupCloudValid req) {
        return cloudMgrService.getGroupCloud(req.getId(), req.getPage(), req.getLimit(), req.getSearch());
    }

    @PostMapping("/getGroupAllCloud")
    public RestResult getGroupAllCloud(@Validated @RequestBody GetGroupAllCloudValid req) {
        return cloudMgrService.getGroupAllCloud(req.getId());
    }

    @PostMapping("/purchase")
    public RestResult purchase(@Validated @RequestBody PurchaseValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TCloudOrder order = new TCloudOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(CLOUD_PURCHASE_ORDER.getValue());
        order.setApply(req.getId());
        order.setOid(req.getPid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.purchase(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setPurchase")
    public RestResult setPurchase(@Validated @RequestBody SetPurchaseValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TCloudOrder order = new TCloudOrder();
        order.setId(req.getOid());
        order.setSid(req.getSid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.setPurchase(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delPurchase")
    public RestResult delPurchase(@Validated @RequestBody DelPurchaseValid req) {
        return cloudService.delPurchase(req.getId(), req.getOid());
    }

    @PostMapping("/reviewPurchase")
    public RestResult reviewPurchase(@Validated @RequestBody ReviewPurchaseValid req) {
        return cloudService.reviewPurchase(req.getId(), req.getOid());
    }

    @PostMapping("/revokePurchase")
    public RestResult revokePurchase(@Validated @RequestBody RevokePurchaseValid req) {
        return cloudService.revokePurchase(req.getId(), req.getOid());
    }

    @PostMapping("/loss")
    public RestResult loss(@Validated @RequestBody LossValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TCloudOrder order = new TCloudOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(CLOUD_LOSS_ORDER.getValue());
        order.setApply(req.getId());
        order.setOid(0);
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.loss(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setLoss")
    public RestResult setLoss(@Validated @RequestBody SetLossValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TCloudOrder order = new TCloudOrder();
        order.setId(req.getOid());
        order.setSid(req.getSid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.setLoss(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delLoss")
    public RestResult delLoss(@Validated @RequestBody DelLossValid req) {
        return cloudService.delLoss(req.getId(), req.getOid());
    }

    @PostMapping("/reviewLoss")
    public RestResult reviewLoss(@Validated @RequestBody ReviewLossValid req) {
        return cloudService.reviewLoss(req.getId(), req.getOid());
    }

    @PostMapping("/revokeLoss")
    public RestResult revokeLoss(@Validated @RequestBody RevokeLossValid req) {
        return cloudService.revokeLoss(req.getId(), req.getOid());
    }

    @PostMapping("/returnc")
    public RestResult returnc(@Validated @RequestBody ReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TCloudOrder order = new TCloudOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(CLOUD_RETURN_ORDER.getValue());
        order.setApply(req.getId());
        order.setOid(req.getRid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.returnc(req.getId(), order, req.getFare(), req.getTypes(), req.getCommoditys(), req.getValues(), req.getPrices(), req.getAttrs());
    }

    @PostMapping("/setReturn")
    public RestResult setReturn(@Validated @RequestBody SetReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TCloudOrder order = new TCloudOrder();
        order.setId(req.getOid());
        order.setSid(req.getSid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.setReturn(req.getId(), order, req.getFare(), req.getTypes(), req.getCommoditys(), req.getValues(), req.getPrices(), req.getAttrs());
    }

    @PostMapping("/delReturn")
    public RestResult delReturn(@Validated @RequestBody DelReturnValid req) {
        return cloudService.delReturn(req.getId(), req.getOid());
    }

    @PostMapping("/reviewReturn")
    public RestResult reviewReturn(@Validated @RequestBody ReviewReturnValid req) {
        return cloudService.reviewReturn(req.getId(), req.getOid());
    }

    @PostMapping("/revokeReturn")
    public RestResult revokeReturn(@Validated @RequestBody RevokeReturnValid req) {
        return cloudService.revokeReturn(req.getId(), req.getOid());
    }
}
