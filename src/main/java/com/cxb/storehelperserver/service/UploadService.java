package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TAgreementAttachment;
import com.cxb.storehelperserver.model.TProductAttachment;
import com.cxb.storehelperserver.model.TStorageAttachment;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static com.cxb.storehelperserver.util.TypeDefine.OrderType;

/**
 * desc: 上传业务
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UploadService {
    @Resource
    private StorageAttachmentRepository storageAttachmentRepository;

    @Resource
    private ProductAttachmentRepository productAttachmentRepository;

    @Resource
    private AgreementAttachmentRepository agreementAttachmentRepository;

    @Resource
    private DateUtil dateUtil;

    @Value("${store-app.config.uploadpath}")
    private String uploadpath;

    @Value("${store-app.config.imagesrc}")
    private int imagesrc;

    @Value("${store-app.config.imagepath}")
    private String imagepath;

    public RestResult addAttach(int id, OrderType type, String name, MultipartFile file) {
        if (file.isEmpty()) {
            return RestResult.fail("上传文件失败，请再试一次");
        }
        SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
        String path = imagepath + simpleDateFormat.format(new Date());
        log.info(uploadpath + path + '/' + name);
        File dest = new File(uploadpath + path + '/' + name);
        if (!dest.getParentFile().exists()) {
            if (!dest.getParentFile().mkdirs()) {
                return RestResult.fail("服务器文件系统失败，请再试一次");
            }
        }
        try {
            file.transferTo(dest);
        } catch (Exception e) {
            return RestResult.fail("上传文件失败，请再试一次");
        }

        // 写入数据库
        val data = new HashMap<String, Object>();
        data.put("name", name);
        switch (type) {
            case STORAGE_IN_ORDER:
            case STORAGE_OUT_ORDER:
                TStorageAttachment storageAttachment = storageAttachmentRepository.insert(0, imagesrc, path, name);
                if (null == storageAttachment) {
                    return RestResult.fail("写入数据失败，请联系管理员");
                }
                data.put("id", storageAttachment.getId());
                break;
            case PRODUCT_IN_ORDER:
            case PRODUCT_OUT_ORDER:
                TProductAttachment productAttachment = productAttachmentRepository.insert(0, imagesrc, path, name);
                if (null == productAttachment) {
                    return RestResult.fail("写入数据失败，请联系管理员");
                }
                data.put("id", productAttachment.getId());
                break;
            case AGREEMENT_IN_ORDER:
            case AGREEMENT_OUT_ORDER:
                TAgreementAttachment agreementAttachment = agreementAttachmentRepository.insert(0, imagesrc, path, name);
                if (null == agreementAttachment) {
                    return RestResult.fail("写入数据失败，请联系管理员");
                }
                data.put("id", agreementAttachment.getId());
                break;
            default:
                return RestResult.fail("数据类型错误");
        }
        return RestResult.ok(data);
    }
}
