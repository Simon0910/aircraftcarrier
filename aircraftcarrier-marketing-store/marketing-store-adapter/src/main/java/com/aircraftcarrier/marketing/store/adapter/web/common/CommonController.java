package com.aircraftcarrier.marketing.store.adapter.web.common;

import com.aircraftcarrier.framework.model.response.MultiResponse;
import com.aircraftcarrier.marketing.store.adapter.ApiSortConstant;
import com.aircraftcarrier.marketing.store.client.CommonService;
import com.aircraftcarrier.marketing.store.client.common.KeywordQry;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 通用接口
 *
 * @author lzp
 */
@ApiSort(ApiSortConstant.COMMON_CONTROLLER)
@Api(tags = "CommonController", produces = "application/json")
@Slf4j
@RequestMapping(value = "/common/")
@RestController
public class CommonController {

    @Resource
    CommonService commonService;

    /**
     * 根据关键字滑动下拉
     *
     * @param keywordQry 关键词
     * @return MultiResponse
     */
    @ApiOperationSupport(order = 10)
    @ApiOperation(value = "根据关键词查询")
    @PostMapping(value = "/keywordsQuery")
    public MultiResponse<Map<String, Object>> keywordsQuery(@RequestBody KeywordQry keywordQry) {
        List<Map<String, Object>> maps = commonService.keywordsQuery(keywordQry);
        return MultiResponse.ok(maps);
    }

}
