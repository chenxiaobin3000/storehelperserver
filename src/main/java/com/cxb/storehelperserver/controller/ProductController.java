package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.product.*;
import com.cxb.storehelperserver.model.TProductOrder;
import com.cxb.storehelperserver.service.ProductService;
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
 * desc: 生产接口
 * auth: cxb
 * date: 2023/1/11
 */
@Slf4j
@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Resource
    private ProductService productService;

    @Resource
    private DateUtil dateUtil;

    @PostMapping("/process")
    public RestResult process(@Validated @RequestBody ProcessValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TProductOrder order = new TProductOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(PRODUCT_PROCESS_ORDER.getValue());
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return productService.process(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setProcess")
    public RestResult setProcess(@Validated @RequestBody SetProcessValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TProductOrder order = new TProductOrder();
        order.setId(req.getOid());
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return productService.setProcess(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delProcess")
    public RestResult delProcess(@Validated @RequestBody DelProcessValid req) {
        return productService.delProcess(req.getId(), req.getOid());
    }

    @PostMapping("/reviewProcess")
    public RestResult reviewProcess(@Validated @RequestBody ReviewProcessValid req) {
        return productService.reviewProcess(req.getId(), req.getOid());
    }

    @PostMapping("/revokeProcess")
    public RestResult revokeProcess(@Validated @RequestBody RevokeProcessValid req) {
        return productService.revokeProcess(req.getId(), req.getOid());
    }

    @PostMapping("/complete")
    public RestResult complete(@Validated @RequestBody CompleteValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TProductOrder order = new TProductOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(PRODUCT_COMPLETE_ORDER.getValue());
        order.setApply(req.getId());
        // TODO 关联生产单
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return productService.complete(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setComplete")
    public RestResult setComplete(@Validated @RequestBody SetCompleteValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TProductOrder order = new TProductOrder();
        order.setId(req.getOid());
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return productService.setComplete(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delComplete")
    public RestResult delComplete(@Validated @RequestBody DelCompleteValid req) {
        return productService.delComplete(req.getId(), req.getOid());
    }

    @PostMapping("/reviewComplete")
    public RestResult reviewComplete(@Validated @RequestBody ReviewCompleteValid req) {
        return productService.reviewComplete(req.getId(), req.getOid());
    }

    @PostMapping("/revokeComplete")
    public RestResult revokeComplete(@Validated @RequestBody RevokeCompleteValid req) {
        return productService.revokeComplete(req.getId(), req.getOid());
    }

    @PostMapping("/loss")
    public RestResult loss(@Validated @RequestBody LossValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TProductOrder order = new TProductOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(PRODUCT_LOSS_ORDER.getValue());
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return productService.loss(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setLoss")
    public RestResult setLoss(@Validated @RequestBody SetLossValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TProductOrder order = new TProductOrder();
        order.setId(req.getOid());
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return productService.setLoss(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delLoss")
    public RestResult delLoss(@Validated @RequestBody DelLossValid req) {
        return productService.delLoss(req.getId(), req.getOid());
    }

    @PostMapping("/reviewLoss")
    public RestResult reviewLoss(@Validated @RequestBody ReviewLossValid req) {
        return productService.reviewLoss(req.getId(), req.getOid());
    }

    @PostMapping("/revokeLoss")
    public RestResult revokeLoss(@Validated @RequestBody RevokeLossValid req) {
        return productService.revokeLoss(req.getId(), req.getOid());
    }
}
