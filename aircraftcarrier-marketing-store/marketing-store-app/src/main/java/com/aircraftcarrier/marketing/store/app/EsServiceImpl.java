package com.aircraftcarrier.marketing.store.app;

import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import com.aircraftcarrier.framework.tookit.BeanUtilBySpring;
import com.aircraftcarrier.marketing.store.client.EsService;
import com.aircraftcarrier.marketing.store.client.es.EsDocumentVo;
import com.aircraftcarrier.marketing.store.infrastructure.es.EsDocumentMapper;
import com.aircraftcarrier.marketing.store.infrastructure.es.data.EsDocument;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    public void scroll() {
        String[] includeFiled = {"title", "content"};
        String[] excludeFiled = {};
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(new BoolQueryBuilder())
                .size(100)
                .fetchSource(includeFiled, excludeFiled)
                .timeout(new TimeValue(60, TimeUnit.SECONDS));

        SearchRequest request = new SearchRequest()
                .indices("xxx_info_index")
                .source(sourceBuilder)
                .searchType(SearchType.DEFAULT)
                .scroll(TimeValue.timeValueSeconds(60));

        SearchResponse response;
        try {
            response = esDocumentMapper.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String scrollId = response.getScrollId();
        // 循环游标
        try {
            while (true) {
                if (scrollId == null) {
                    break;
                }

                long startTimeScroll = System.currentTimeMillis();
                SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId).scroll(TimeValue.timeValueSeconds(60));
                response = esDocumentMapper.scroll(searchScrollRequest, RequestOptions.DEFAULT);
                long endTimeScroll = System.currentTimeMillis();
                long processScroll = endTimeScroll - startTimeScroll;

                if (response != null && response.getHits().getHits().length > 0) {
                    // 处理数据。。。
                    scrollId = response.getScrollId();
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
