package com.aircraftcarrier.marketing.store.app;

import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import com.aircraftcarrier.framework.tookit.BeanUtil;
import com.aircraftcarrier.framework.tookit.BeanUtilBySpring;
import com.aircraftcarrier.marketing.store.client.EsService;
import com.aircraftcarrier.marketing.store.client.es.EsDocumentVo;
import com.aircraftcarrier.marketing.store.infrastructure.es.data.EsDocument;
import com.aircraftcarrier.marketing.store.infrastructure.es.EsDocumentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liuzhipeng
 */
@Service
public class EsServiceImpl implements EsService {

    private final EsDocumentMapper esDocumentMapper;

    public EsServiceImpl(EsDocumentMapper esDocumentMapper) {
        this.esDocumentMapper = esDocumentMapper;
    }


    @Override
    public Integer insert() {
        // 初始化-> 新增数据
        EsDocument esDocument = new EsDocument();
        esDocument.setTitle("老汉");
        esDocument.setContent("推*技术过硬");
        return esDocumentMapper.insert(esDocument);
    }

    @Override
    public List<EsDocumentVo> search() {
        // 查询出所有标题为老汉的文档列表
        LambdaEsQueryWrapper<EsDocument> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.eq(EsDocument::getTitle, "老汉");
        List<EsDocument> esDocuments = esDocumentMapper.selectList(wrapper);
        return BeanUtilBySpring.convertList(esDocuments, EsDocumentVo.class);
    }
}
