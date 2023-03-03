package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudAttachmentMapper;
import com.cxb.storehelperserver.model.TCloudAttachment;
import com.cxb.storehelperserver.model.TCloudAttachmentExample;
import lombok.extern.slf4j.Slf4j;
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
public class CloudAttachmentRepository extends BaseRepository<List> {
    @Resource
    private TCloudAttachmentMapper cloudAttachmentMapper;

    public CloudAttachmentRepository() {
        init("cloudAtt::");
    }

    public TCloudAttachment find(int id) {
        return cloudAttachmentMapper.selectByPrimaryKey(id);
    }

    public List<TCloudAttachment> findByOid(int oid) {
        List<TCloudAttachment> cloudAttachments = getCache(oid, List.class);
        if (null != cloudAttachments) {
            return cloudAttachments;
        }

        // 缓存没有就查询数据库
        TCloudAttachmentExample example = new TCloudAttachmentExample();
        example.or().andOidEqualTo(oid);
        cloudAttachments = cloudAttachmentMapper.selectByExample(example);
        if (null != cloudAttachments) {
            setCache(oid, cloudAttachments);
        }
        return cloudAttachments;
    }

    public TCloudAttachment insert(int oid, int imagesrc, String path, String name) {
        TCloudAttachment row = new TCloudAttachment();
        row.setOid(oid);
        row.setSrc(imagesrc);
        row.setPath(path);
        row.setName(name);
        if (cloudAttachmentMapper.insert(row) > 0) {
            delCache(oid);
            return row;
        }
        return null;
    }

    public boolean update(TCloudAttachment row) {
        if (cloudAttachmentMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int oid, int id) {
        delCache(oid);
        return cloudAttachmentMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean deleteByOid(int oid) {
        delCache(oid);
        TCloudAttachmentExample example = new TCloudAttachmentExample();
        example.or().andOidEqualTo(oid);
        return cloudAttachmentMapper.deleteByExample(example) > 0;
    }
}
