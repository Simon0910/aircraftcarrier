package com.aircraftcarrier.marketing.store.adapter.web;

import com.aircraftcarrier.framework.excel.util.EasyExcelWriteUtil;
import com.aircraftcarrier.framework.model.BatchResult;
import com.aircraftcarrier.framework.model.Page;
import com.aircraftcarrier.framework.model.response.MultiResponse;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.marketing.store.client.DemoService;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoCmd;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoDeleteCmd;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoDetailQryCmd;
import com.aircraftcarrier.marketing.store.client.demo.cmd.DemoPageQryCmd;
import com.aircraftcarrier.marketing.store.client.demo.excel.DemoImportExcelCmd;
import com.aircraftcarrier.marketing.store.client.demo.excel.template.DemoImportExcel;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoAdd;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoDetailQry;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoPageQry;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoUpdate;
import com.aircraftcarrier.marketing.store.client.demo.view.DemoPageVo;
import com.aircraftcarrier.marketing.store.client.demo.view.DemoVo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 演示
 * {@link com.aircraftcarrier.marketing.store.adapter.web.DemoControllerTest }
 *
 * @author lzp
 */
@Api(tags = "DemoController", produces = "application/json")
@Slf4j
@RequestMapping(value = "/web/demo")
@RestController
public class DemoController {

    @Resource
    private DemoService demoService;


    @ApiOperationSupport(order = 10)
    @ApiOperation(value = "分页查询", notes = "REST风格")
    @PostMapping("/pageList")
    public SingleResponse<Page<DemoPageVo>> pageList(@RequestBody DemoPageQry pageQry) {
        return SingleResponse.ok(demoService.pageList(new DemoPageQryCmd(pageQry)));
    }


    @ApiOperationSupport(order = 11)
    @ApiOperation("新增")
    @PostMapping("add")
    public int add(@RequestBody DemoAdd add) {
        DemoCmd cmd = DemoCmd.builder().demoAdd(add).build();
        return demoService.add(cmd);
    }


    @ApiOperationSupport(order = 12)
    @ApiOperation("更新")
    @PostMapping("update")
    public int update(@RequestBody DemoUpdate update) {
        DemoCmd cmd = DemoCmd.builder().demoUpdate(update).build();
        return demoService.update(cmd);
    }


    @ApiOperationSupport(order = 20)
    @ApiOperation(value = "根据id获取DemoVo", notes = "REST风格")
    @GetMapping("/get/{id}")
    public SingleResponse<DemoVo> getById(@ApiParam(name = "id", value = "数据主键", required = true)
                                          @PathVariable Integer id) {
        return SingleResponse.ok(demoService.getById(id));
    }


    @ApiOperationSupport(order = 30)
    @ApiOperation(value = "根据参数获取List<DemoVo>", notes = "对象参数")
    @PostMapping("/selectList")
    public MultiResponse<DemoVo> selectList(@RequestBody DemoDetailQry detailQry) {
        return MultiResponse.ok(demoService.selectList(new DemoDetailQryCmd(detailQry)));
    }


    @ApiOperationSupport(order = 35)
    @ApiOperation("批量删除")
    @PostMapping("delete")
    public boolean delete(@RequestParam(value = "ids") List<Long> ids) {
        return demoService.delete(new DemoDeleteCmd(ids));
    }


    @ApiOperationSupport(order = 40)
    @ApiOperation(value = "导出", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PostMapping("export")
    public void export(@RequestBody DemoPageQry pageQry, HttpServletResponse response) throws Exception {
        List<DemoImportExcel> list = demoService.export(new DemoPageQryCmd(pageQry));
        EasyExcelWriteUtil.exportExcel(response, "模板", "Excel导入演示",
                list, DemoImportExcel.class);
    }


    @ApiOperationSupport(order = 50)
    @ApiImplicitParam(name = "file", dataType = "__File", value = "文件")
    @ApiOperation("导入")
    @PostMapping("import")
    public SingleResponse<BatchResult> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return demoService.importExcel(new DemoImportExcelCmd(file));
    }
}
