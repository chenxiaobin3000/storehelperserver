package com.cxb.storehelperserver.controller;

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

    @PostMapping("/addAfterInfo")
    public RestResult addAfterInfo(@Validated @RequestBody AddAfterInfoValid req) {
        return saleService.addAfterInfo(req.getId(), req.getOid(), req.getRemark());
    }

    @PostMapping("/delAfterInfo")
    public RestResult delAfterInfo(@Validated @RequestBody DelAfterInfoValid req) {
        return saleService.delAfterInfo(req.getId(), req.getOid(), req.getRid());
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

    @PostMapping("/addLossInfo")
    public RestResult addLossInfo(@Validated @RequestBody AddAfterInfoValid req) {
        return saleService.addLossInfo(req.getId(), req.getOid(), req.getRemark());
    }

    @PostMapping("/delLossInfo")
    public RestResult delLossInfo(@Validated @RequestBody DelAfterInfoValid req) {
        return saleService.delLossInfo(req.getId(), req.getOid(), req.getRid());
    }

    @PostMapping("/offLine")
    public RestResult offLine(@Validated @RequestBody ReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TSaleOrder order = new TSaleOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(SALE_OFFLINE_ORDER.getValue());
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return saleService.offLine(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/setOffLine")
    public RestResult setOffLine(@Validated @RequestBody SetReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        Date applyTime = null;
        try {
            applyTime = simpleDateFormat.parse(req.getDate());
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return saleService.setOffLine(req.getId(), req.getOid(), req.getSid(), applyTime, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delOffLine")
    public RestResult delOffLine(@Validated @RequestBody DelReturnValid req) {
        return saleService.delOffLine(req.getId(), req.getOid());
    }

    @PostMapping("/reviewOffLine")
    public RestResult reviewOffLine(@Validated @RequestBody ReviewReturnValid req) {
        return saleService.reviewOffLine(req.getId(), req.getOid());
    }

    @PostMapping("/revokeOffLine")
    public RestResult revokeOffLine(@Validated @RequestBody RevokeReturnValid req) {
        return saleService.revokeOffLine(req.getId(), req.getOid());
    }

    @PostMapping("/addOffLineInfo")
    public RestResult addOffLineInfo(@Validated @RequestBody AddReturnInfoValid req) {
        return saleService.addOffLineInfo(req.getId(), req.getOid(), req.getRemark());
    }

    @PostMapping("/delOffLineInfo")
    public RestResult delOffLineInfo(@Validated @RequestBody DelReturnInfoValid req) {
        return saleService.delOffLineInfo(req.getId(), req.getOid(), req.getRid());
    }

    @PostMapping("/returnc")
    public RestResult returnc(@Validated @RequestBody ReturnValid req) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        TSaleOrder order = new TSaleOrder();
        order.setGid(req.getGid());
        order.setSid(req.getSid());
        order.setOtype(SALE_RETURN_ORDER.getValue());
        order.setApply(req.getId());
        try {
            order.setApplyTime(simpleDateFormat.parse(req.getDate()));
        } catch (ParseException e) {
            return RestResult.fail("订单制单日期转换失败");
        }
        return saleService.returnc(req.getId(), order, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
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
        return saleService.setReturn(req.getId(), req.getOid(), req.getSid(), applyTime, req.getTypes(), req.getCommoditys(), req.getWeights(), req.getValues(), req.getAttrs());
    }

    @PostMapping("/delReturn")
    public RestResult delReturn(@Validated @RequestBody DelReturnValid req) {
        return saleService.delReturn(req.getId(), req.getOid());
    }

    @PostMapping("/reviewReturn")
    public RestResult reviewReturn(@Validated @RequestBody ReviewReturnValid req) {
        return saleService.reviewReturn(req.getId(), req.getOid());
    }

    @PostMapping("/revokeReturn")
    public RestResult revokeReturn(@Validated @RequestBody RevokeReturnValid req) {
        return saleService.revokeReturn(req.getId(), req.getOid());
    }

    @PostMapping("/addReturnInfo")
    public RestResult addReturnInfo(@Validated @RequestBody AddReturnInfoValid req) {
        return saleService.addReturnInfo(req.getId(), req.getOid(), req.getRemark());
    }

    @PostMapping("/delReturnInfo")
    public RestResult delReturnInfo(@Validated @RequestBody DelReturnInfoValid req) {
        return saleService.delReturnInfo(req.getId(), req.getOid(), req.getRid());
    }
}
