package com.aircraftcarrier.marketing.store.app;

import com.aircraftcarrier.marketing.store.app.common.KeywordQryExe;
import com.aircraftcarrier.marketing.store.client.CommonService;
import com.aircraftcarrier.marketing.store.client.common.KeywordQry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author lzp
 */
@Slf4j
@Service
public class CommonServiceImpl implements CommonService {

    @Resource
    KeywordQryExe keywordQryExe;

    @Override
    public List<Map<String, Object>> keywordsQuery(KeywordQry keywordQry) {
        return keywordQryExe.execute(keywordQry);
    }
}
