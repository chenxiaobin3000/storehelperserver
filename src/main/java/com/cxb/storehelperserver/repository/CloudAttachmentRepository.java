package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudAttachmentMapper;
import com.cxb.storehelperserver.model.TCloudAttachment;
import com.cxb.storehelperserver.model.TCloudAttachmentExample;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 云仓出入库附件仓库
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Repository
public class CloudAttachmentRepository extends BaseRepository<TCloudAttachment> {
    @Resource
    private TCloudAttachmentMapper cloudAttachmentMapper;

    public CloudAttachmentRepository() {
        init("cloudAtt::");
    }

    public TCloudAttachment find(int id) {
        TCloudAttachment cloudAttachment = getCache(id, TCloudAttachment.class);
        if (null != cloudAttachment) {
            return cloudAttachment;
        }

        // 缓存没有就查询数据库
        cloudAttachment = cloudAttachmentMapper.selectByPrimaryKey(id);
        if (null != cloudAttachment) {
            setCache(id, cloudAttachment);
        }
        return cloudAttachment;
    }

    public List<TCloudAttachment> findByOid(int oid) {
        TCloudAttachmentExample example = new TCloudAttachmentExample();
        example.or().andOidEqualTo(oid);
        return cloudAttachmentMapper.selectByExample(example);
    }

    public boolean insert(TCloudAttachment row) {
        if (cloudAttachmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TCloudAttachment row) {
        if (cloudAttachmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int oid) {
        val attrs = findByOid(oid);
        for (TCloudAttachment a : attrs) {
            delCache(a.getId());
            if (cloudAttachmentMapper.deleteByPrimaryKey(a.getId()) <= 0) {
                return false;
            }
        }
        return true;
    }
}
