package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc: 生产订单缓存业务
 * auth: cxb
 * date: 2023/1/27
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductOrderService extends BaseService<HashMap> {
    @Resource
    private ProductOrderRepository productOrderRepository;

    @Resource
    private ProductCommodityRepository productCommodityRepository;

    @Resource
    private ProductAttachmentRepository productAttachmentRepository;

    @Resource
    private ProductRemarkRepository productRemarkRepository;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private DateUtil dateUtil;

    public ProductOrderService() {
        init("productServ::");
    }

    public HashMap<String, Object> find(int oid) {
        HashMap<String, Object> datas = getCache(oid, HashMap.class);
        if (null != datas) {
            return datas;
        }

        // 商品
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val commoditys = new ArrayList<HashMap<String, Object>>();
        val productCommodities = productCommodityRepository.find(oid);
        if (null != productCommodities && !productCommodities.isEmpty()) {
            for (TProductCommodity sc : productCommodities) {
                val data = new HashMap<String, Object>();
                data.put("id", sc.getId());
                data.put("cid", sc.getCid());
                data.put("price", sc.getPrice());
                data.put("weight", sc.getWeight());
                data.put("norm", sc.getNorm());
                data.put("value", sc.getValue());
                commoditys.add(data);

                // 获取商品单位信息
                TCommodity commodity = commodityRepository.find(sc.getCid());
                if (null != commodity) {
                    data.put("code", commodity.getCode());
                    data.put("name", commodity.getName());
                }
            }
        }

        // 附件
        datas = new HashMap<>();
        datas.put("comms", commoditys);
        datas.put("attrs", productAttachmentRepository.findByOid(oid));

        // 备注
        val remarks = productRemarkRepository.findByOid(oid);
        if (null != remarks && !remarks.isEmpty()) {
            val tmps = new ArrayList<HashMap<String, Object>>();
            for (TProductRemark remark : remarks) {
                val tmp = new HashMap<String, Object>();
                tmp.put("id", remark.getId());
                tmp.put("remark", remark.getRemark());
                tmp.put("cdate", dateFormat.format(remark.getCdate()));
                tmps.add(tmp);
            }
            datas.put("remarks", tmps);
        }
        setCache(oid, datas);
        return datas;
    }

    public int total(int gid, int type, ReviewType review, Date start, Date end, String search) {
        if (null == search || search.isEmpty()) {
            return productOrderRepository.total(gid, type, review, start, end);
        } else {
            val ids = commodityRepository.search(search);
            if (null == ids || ids.isEmpty()) {
                return 0;
            }
            return productCommodityRepository.total(gid, type, review, start, end, ids);
        }
    }

    public List<TProductOrder> pagination(int gid, int type, int page, int limit, ReviewType review, Date start, Date end, String search) {
        if (null == search || search.isEmpty()) {
            return productOrderRepository.pagination(gid, type, page, limit, review, start, end);
        } else {
            val ids = commodityRepository.search(search);
            if (null == ids || ids.isEmpty()) {
                return null;
            }
            return productCommodityRepository.pagination(gid, type, page, limit, review, start, end, ids);
        }
    }

    public String update(int oid, List<TProductCommodity> comms, List<Integer> attrs) {
        delCache(oid);
        for (TProductCommodity c : comms) {
            c.setOid(oid);
        }
        if (!productCommodityRepository.update(comms, oid)) {
            return "生成订单商品数据失败";
        }

        if (null != attrs && !attrs.isEmpty()) {
            // 删除多余附件
            val productAttachments = productAttachmentRepository.findByOid(oid);
            if (null != productAttachments) {
                for (TProductAttachment attr : productAttachments) {
                    boolean find = false;
                    for (Integer aid : attrs) {
                        if (attr.getId().equals(aid)) {
                            find = true;
                            break;
                        }
                    }
                    if (!find) {
                        if (!productAttachmentRepository.delete(oid, attr.getId())) {
                            return "删除订单附件失败";
                        }
                    }
                }
            }

            // 添加新附件
            for (Integer attr : attrs) {
                TProductAttachment productAttachment = productAttachmentRepository.find(attr);
                if (null != productAttachment) {
                    if (null == productAttachment.getOid() || 0 == productAttachment.getOid()) {
                        productAttachment.setOid(oid);
                        if (!productAttachmentRepository.update(productAttachment)) {
                            return "添加订单附件失败";
                        }
                    }
                }
            }
        }
        return null;
    }

    public void clean(int oid) {
        delCache(oid);
    }
}
