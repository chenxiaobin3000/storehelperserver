package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.TypeDefine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * desc: 仓库订单缓存业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StorageOrderService extends BaseService<HashMap> {
    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private StorageCommodityRepository storageCommodityRepository;

    @Resource
    private StorageAttachmentRepository storageAttachmentRepository;

    @Resource
    private StorageFareRepository storageFareRepository;

    @Resource
    private StorageRemarkRepository storageRemarkRepository;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private StandardRepository standardRepository;

    @Resource
    private DateUtil dateUtil;

    public StorageOrderService() {
        init("storageServ::");
    }

    public HashMap<String, Object> find(int oid) {
        HashMap<String, Object> datas = getCache(oid, HashMap.class);
        if (null != datas) {
            return datas;
        }

        // 商品
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val commoditys = new ArrayList<HashMap<String, Object>>();
        val storageCommodities = storageCommodityRepository.find(oid);
        if (null != storageCommodities && !storageCommodities.isEmpty()) {
            for (TStorageCommodity sc : storageCommodities) {
                val data = new HashMap<String, Object>();
                data.put("id", sc.getId());
                data.put("cid", sc.getCid());
                data.put("ctype", sc.getCtype());
                data.put("price", sc.getPrice());
                data.put("weight", sc.getWeight());
                data.put("norm", sc.getNorm());
                data.put("value", sc.getValue());
                commoditys.add(data);

                // 获取商品单位信息
                TypeDefine.CommodityType type = TypeDefine.CommodityType.valueOf(sc.getCtype());
                int cid = sc.getCid();
                switch (type) {
                    case ORIGINAL:
                        TOriginal find3 = originalRepository.find(cid);
                        if (null != find3) {
                            data.put("code", find3.getCode());
                            data.put("name", find3.getName());
                        }
                        break;
                    case STANDARD:
                        TStandard find4 = standardRepository.find(cid);
                        if (null != find4) {
                            data.put("code", find4.getCode());
                            data.put("name", find4.getName());
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        // 附件
        datas = new HashMap<>();
        datas.put("comms", commoditys);
        datas.put("attrs", storageAttachmentRepository.findByOid(oid));

        // 运费
        val fares = storageFareRepository.findByOid(oid);
        if (null != fares && !fares.isEmpty()) {
            val tmps = new ArrayList<HashMap<String, Object>>();
            BigDecimal total = new BigDecimal(0);
            for (TStorageFare fare : fares) {
                val tmp = new HashMap<String, Object>();
                total = total.add(fare.getFare());
                tmp.put("id", fare.getId());
                tmp.put("fare", fare.getFare());
                tmp.put("cdate", dateFormat.format(fare.getCdate()));
                tmps.add(tmp);
            }
            datas.put("total", total);
            datas.put("fares", tmps);
        }

        // 备注
        val remarks = storageRemarkRepository.findByOid(oid);
        if (null != remarks && !remarks.isEmpty()) {
            val tmps = new ArrayList<HashMap<String, Object>>();
            for (TStorageRemark remark : remarks) {
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

    public String update(int oid, List<TStorageCommodity> comms, List<Integer> attrs) {
        delCache(oid);
        for (TStorageCommodity c : comms) {
            c.setOid(oid);
        }
        if (!storageCommodityRepository.update(comms, oid)) {
            return "生成订单商品信息失败";
        }

        // 删除多余附件
        val storageAttachments = storageAttachmentRepository.findByOid(oid);
        if (null != storageAttachments) {
            for (TStorageAttachment attr : storageAttachments) {
                boolean find = false;
                for (Integer aid : attrs) {
                    if (attr.getId().equals(aid)) {
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    if (!storageAttachmentRepository.delete(oid, attr.getId())) {
                        return "删除订单附件失败";
                    }
                }
            }
        }

        // 添加新附件
        for (Integer attr : attrs) {
            TStorageAttachment storageAttachment = storageAttachmentRepository.find(attr);
            if (null != storageAttachment) {
                if (null == storageAttachment.getOid() || 0 == storageAttachment.getOid()) {
                    storageAttachment.setOid(oid);
                    if (!storageAttachmentRepository.update(storageAttachment)) {
                        return "添加订单附件失败";
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
