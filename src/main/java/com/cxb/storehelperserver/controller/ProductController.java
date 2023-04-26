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
import java.util.Date;

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

    @PostMapping("/collect")
    public RestResult collect(@Validated @RequestBody CollectValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TProductOrder order = new TProductOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(PRODUCT_COLLECT_ORDER.getValue());
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return productService.collect(req.getId(), order, req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues(), req.getCommoditys2(),
                req.getPrices2(), req.getWeights2(), req.getValues2(), req.getCommoditys3(), req.getPrices3(), req.getWeights3(), req.getValues3(), req.getAttrs());
    }

    @PostMapping("/setCollect")
    public RestResult setCollect(@Validated @RequestBody SetCollectValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return productService.setCollect(req.getId(), req.getOid(), req.getSid(), applyTime, req.getCommoditys(), req.getPrices(), req.getWeights(), req.getValues(),
                req.getCommoditys2(), req.getPrices2(), req.getWeights2(), req.getValues2(), req.getCommoditys3(), req.getPrices3(), req.getWeights3(), req.getValues3(), req.getAttrs());
    }

    @PostMapping("/delCollect")
    public RestResult delCollect(@Validated @RequestBody DelCollectValid req) {
        return productService.delCollect(req.getId(), req.getOid());
    }

    @PostMapping("/reviewCollect")
    public RestResult reviewCollect(@Validated @RequestBody ReviewCollectValid req) {
        return productService.reviewCollect(req.getId(), req.getOid());
    }

    @PostMapping("/revokeCollect")
    public RestResult revokeCollect(@Validated @RequestBody RevokeCollectValid req) {
        return productService.revokeCollect(req.getId(), req.getOid());
    }
}
