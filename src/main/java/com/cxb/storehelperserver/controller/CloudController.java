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
import java.util.Date;

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
        cloud.setName(req.getName());
        cloud.setContact(req.getContact());
        cloud.setPhone(req.getPhone());
        cloud.setAddress(req.getAddress());
        return cloudMgrService.addCloud(req.getId(), cloud);
    }

    @PostMapping("/setCloud")
    public RestResult setCloud(@Validated @RequestBody SetCloudValid req) {
        TCloud cloud = new TCloud();
        cloud.setId(req.getSid());
        cloud.setGid(req.getGid());
        cloud.setArea(Long.valueOf(req.getArea()));
        cloud.setName(req.getName());
        cloud.setContact(req.getContact());
        cloud.setPhone(req.getPhone());
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
        order.setOtype(CLOUD_PURCHASE_ORDER.getValue());
        order.setApply(req.getId());
        order.setOid(req.getPid());
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.purchase(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setPurchase")
    public RestResult setPurchase(@Validated @RequestBody SetPurchaseValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.setPurchase(req.getId(), req.getOid(), applyTime, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
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

    @PostMapping("/addPurchaseInfo")
    public RestResult addPurchaseInfo(@Validated @RequestBody AddPurchaseInfoValid req) {
        return cloudService.addPurchaseInfo(req.getId(), req.getOid(), req.getRemark());
    }

    @PostMapping("/delPurchaseInfo")
    public RestResult delPurchaseInfo(@Validated @RequestBody DelPurchaseInfoValid req) {
        return cloudService.delPurchaseInfo(req.getId(), req.getOid(), req.getRid());
    }

    @PostMapping("/agreement")
    public RestResult agreement(@Validated @RequestBody PurchaseValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TCloudOrder order = new TCloudOrder();
        order.setOtype(CLOUD_AGREEMENT_ORDER.getValue());
        order.setApply(req.getId());
        order.setOid(req.getPid());
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.agreement(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setAgreement")
    public RestResult setAgreement(@Validated @RequestBody SetPurchaseValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.setAgreement(req.getId(), req.getOid(), applyTime, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delAgreement")
    public RestResult delAgreement(@Validated @RequestBody DelPurchaseValid req) {
        return cloudService.delAgreement(req.getId(), req.getOid());
    }

    @PostMapping("/reviewAgreement")
    public RestResult reviewAgreement(@Validated @RequestBody ReviewPurchaseValid req) {
        return cloudService.reviewAgreement(req.getId(), req.getOid());
    }

    @PostMapping("/revokeAgreement")
    public RestResult revokeAgreement(@Validated @RequestBody RevokePurchaseValid req) {
        return cloudService.revokeAgreement(req.getId(), req.getOid());
    }

    @PostMapping("/addAgreementInfo")
    public RestResult addAgreementInfo(@Validated @RequestBody AddPurchaseInfoValid req) {
        return cloudService.addAgreementInfo(req.getId(), req.getOid(), req.getRemark());
    }

    @PostMapping("/delAgreementInfo")
    public RestResult delAgreementInfo(@Validated @RequestBody DelPurchaseInfoValid req) {
        return cloudService.delAgreementInfo(req.getId(), req.getOid(), req.getRid());
    }

    @PostMapping("/loss")
    public RestResult loss(@Validated @RequestBody LossValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TCloudOrder order = new TCloudOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setCid(req.getSid());
        order.setOtype(CLOUD_LOSS_ORDER.getValue());
        order.setApply(req.getId());
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.loss(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setLoss")
    public RestResult setLoss(@Validated @RequestBody SetLossValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.setLoss(req.getId(), req.getOid(), req.getSid(), applyTime, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
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

    @PostMapping("/addLossInfo")
    public RestResult addLossInfo(@Validated @RequestBody AddPurchaseInfoValid req) {
        return cloudService.addLossInfo(req.getId(), req.getOid(), req.getRemark());
    }

    @PostMapping("/delLossInfo")
    public RestResult delLossInfo(@Validated @RequestBody DelPurchaseInfoValid req) {
        return cloudService.delLossInfo(req.getId(), req.getOid(), req.getRid());
    }

    @PostMapping("/returnc")
    public RestResult returnc(@Validated @RequestBody ReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TCloudOrder order = new TCloudOrder();
        order.setOtype(CLOUD_RETURN_ORDER.getValue());
        order.setApply(req.getId());
        order.setOid(req.getRid());
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.returnc(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setReturn")
    public RestResult setReturn(@Validated @RequestBody SetReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.setReturn(req.getId(), req.getOid(), applyTime, req.getTypes(), req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues(), req.getAttrs());
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

    @PostMapping("/addReturnInfo")
    public RestResult addReturnInfo(@Validated @RequestBody AddReturnInfoValid req) {
        return cloudService.addReturnInfo(req.getId(), req.getOid(), req.getFare(), req.getRemark());
    }

    @PostMapping("/delReturnInfo")
    public RestResult delReturnInfo(@Validated @RequestBody DelReturnInfoValid req) {
        return cloudService.delReturnInfo(req.getId(), req.getOid(), req.getFid(), req.getRid());
    }

    @PostMapping("/backc")
    public RestResult backc(@Validated @RequestBody BackValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TCloudOrder order = new TCloudOrder();
        order.setOtype(CLOUD_BACK_ORDER.getValue());
        order.setApply(req.getId());
        order.setOid(req.getRid());
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.backc(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setBack")
    public RestResult setBack(@Validated @RequestBody SetBackValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.setBack(req.getId(), req.getOid(), applyTime, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delBack")
    public RestResult delBack(@Validated @RequestBody DelBackValid req) {
        return cloudService.delBack(req.getId(), req.getOid());
    }

    @PostMapping("/reviewBack")
    public RestResult reviewBack(@Validated @RequestBody ReviewBackValid req) {
        return cloudService.reviewBack(req.getId(), req.getOid());
    }

    @PostMapping("/revokeBack")
    public RestResult revokeBack(@Validated @RequestBody RevokeBackValid req) {
        return cloudService.revokeBack(req.getId(), req.getOid());
    }

    @PostMapping("/addBackInfo")
    public RestResult addBackInfo(@Validated @RequestBody AddReturnInfoValid req) {
        return cloudService.addBackInfo(req.getId(), req.getOid(), req.getFare(), req.getRemark());
    }

    @PostMapping("/delBackInfo")
    public RestResult delBackInfo(@Validated @RequestBody DelReturnInfoValid req) {
        return cloudService.delBackInfo(req.getId(), req.getOid(), req.getFid(), req.getRid());
    }

    @PostMapping("/dispatch")
    public RestResult dispatch(@Validated @RequestBody DispatchValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TCloudOrder order = new TCloudOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(CLOUD_DISPATCH_ORDER.getValue());
        order.setApply(req.getId());
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.dispatch(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setDispatch")
    public RestResult setDispatch(@Validated @RequestBody SetDispatchValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.setDispatch(req.getId(), req.getOid(), req.getSid(), applyTime, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delDispatch")
    public RestResult delDispatch(@Validated @RequestBody DelDispatchValid req) {
        return cloudService.delDispatch(req.getId(), req.getOid());
    }

    @PostMapping("/reviewDispatch")
    public RestResult reviewDispatch(@Validated @RequestBody ReviewDispatchValid req) {
        return cloudService.reviewDispatch(req.getId(), req.getOid());
    }

    @PostMapping("/revokeDispatch")
    public RestResult revokeDispatch(@Validated @RequestBody RevokeDispatchValid req) {
        return cloudService.revokeDispatch(req.getId(), req.getOid());
    }

    @PostMapping("/addDispatchInfo")
    public RestResult addDispatchInfo(@Validated @RequestBody AddDispatchInfoValid req) {
        return cloudService.addDispatchInfo(req.getId(), req.getOid(), req.getFare(), req.getRemark());
    }

    @PostMapping("/delDispatchInfo")
    public RestResult delDispatchInfo(@Validated @RequestBody DelDispatchInfoValid req) {
        return cloudService.delDispatchInfo(req.getId(), req.getOid(), req.getFid(), req.getRid());
    }

    @PostMapping("/purchase2")
    public RestResult purchase2(@Validated @RequestBody Purchase2Valid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TCloudOrder order = new TCloudOrder();
        order.setSid(req.getSid());
        order.setOtype(CLOUD_PURCHASE2_ORDER.getValue());
        order.setApply(req.getId());
        order.setOid(req.getPid());
        order.setComplete(new Byte("0"));
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.purchase2(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setPurchase2")
    public RestResult setPurchase2(@Validated @RequestBody SetPurchase2Valid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return cloudService.setPurchase2(req.getId(), req.getOid(), req.getSid(), applyTime, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delPurchase2")
    public RestResult delPurchase2(@Validated @RequestBody DelPurchaseValid req) {
        return cloudService.delPurchase2(req.getId(), req.getOid());
    }

    @PostMapping("/reviewPurchase2")
    public RestResult reviewPurchase2(@Validated @RequestBody ReviewPurchaseValid req) {
        return cloudService.reviewPurchase2(req.getId(), req.getOid());
    }

    @PostMapping("/revokePurchase2")
    public RestResult revokePurchase2(@Validated @RequestBody RevokePurchaseValid req) {
        return cloudService.revokePurchase2(req.getId(), req.getOid());
    }

    @PostMapping("/addPurchase2Info")
    public RestResult addPurchase2Info(@Validated @RequestBody AddPurchaseInfoValid req) {
        return cloudService.addPurchase2Info(req.getId(), req.getOid(), req.getRemark());
    }

    @PostMapping("/delPurchase2Info")
    public RestResult delPurchase2Info(@Validated @RequestBody DelPurchaseInfoValid req) {
        return cloudService.delPurchase2Info(req.getId(), req.getOid(), req.getRid());
    }
}
