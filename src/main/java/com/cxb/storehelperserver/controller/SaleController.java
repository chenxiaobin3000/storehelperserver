package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.purchase.SetPurchasePayValid;
import com.cxb.storehelperserver.controller.request.sale.*;
import com.cxb.storehelperserver.model.TSaleOrder;
import com.cxb.storehelperserver.service.SaleService;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.cxb.storehelperserver.util.TypeDefine.OrderType.*;

/**
 * desc: 销售接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/sale")
public class SaleController {
    @Resource
    private SaleService saleService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/sale")
    public RestResult sale(@Validated @RequestBody SaleValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TSaleOrder order = new TSaleOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setPid(req.getPid());
        order.setOtype(SALE_SALE_ORDER.getValue());
        order.setPayPrice(new BigDecimal(0));
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return saleService.sale(req.getId(), order);
    }

    @PostMapping("/delSale")
    public RestResult delSale(@Validated @RequestBody DelAfterValid req) {
        return saleService.delSale(req.getId(), req.getOid());
    }

    @PostMapping("/reviewSale")
    public RestResult reviewSale(@Validated @RequestBody ReviewAfterValid req) {
        return saleService.reviewSale(req.getId(), req.getOid());
    }

    @PostMapping("/revokeSale")
    public RestResult revokeSale(@Validated @RequestBody RevokeAfterValid req) {
        return saleService.revokeSale(req.getId(), req.getOid());
    }

    @PostMapping("/setSalePay")
    public RestResult setSalePay(@Validated @RequestBody SetSalePayValid req) {
        return saleService.setSalePay(req.getId(), req.getOid(), req.getPay());
    }

    @PostMapping("/after")
    public RestResult after(@Validated @RequestBody AfterValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TSaleOrder order = new TSaleOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(SALE_AFTER_ORDER.getValue());
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return saleService.after(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setAfter")
    public RestResult setAfter(@Validated @RequestBody SetAfterValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return saleService.setAfter(req.getId(), req.getOid(), req.getSid(), applyTime, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delAfter")
    public RestResult delAfter(@Validated @RequestBody DelAfterValid req) {
        return saleService.delAfter(req.getId(), req.getOid());
    }

    @PostMapping("/reviewAfter")
    public RestResult reviewAfter(@Validated @RequestBody ReviewAfterValid req) {
        return saleService.reviewAfter(req.getId(), req.getOid());
    }

    @PostMapping("/revokeAfter")
    public RestResult revokeAfter(@Validated @RequestBody RevokeAfterValid req) {
        return saleService.revokeAfter(req.getId(), req.getOid());
    }

    @PostMapping("/loss")
    public RestResult loss(@Validated @RequestBody AfterValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TSaleOrder order = new TSaleOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(SALE_LOSS_ORDER.getValue());
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return saleService.loss(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setLoss")
    public RestResult setLoss(@Validated @RequestBody SetAfterValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return saleService.setLoss(req.getId(), req.getOid(), req.getSid(), applyTime, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delLoss")
    public RestResult delLoss(@Validated @RequestBody DelAfterValid req) {
        return saleService.delLoss(req.getId(), req.getOid());
    }

    @PostMapping("/reviewLoss")
    public RestResult reviewLoss(@Validated @RequestBody ReviewAfterValid req) {
        return saleService.reviewLoss(req.getId(), req.getOid());
    }

    @PostMapping("/revokeLoss")
    public RestResult revokeLoss(@Validated @RequestBody RevokeAfterValid req) {
        return saleService.revokeLoss(req.getId(), req.getOid());
    }
}
