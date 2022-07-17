package com.aircraftcarrier.marketing.store.app.demo.convert;

import com.aircraftcarrier.marketing.store.client.demo.excel.template.DemoImportExcel;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;

/**
 * @author lzp
 */
public class DemoDoToImportExcelConvert {

    private DemoDoToImportExcelConvert() {
    }

    /**
     * convertDifference
     *
     * @param demoDo      demoDo
     * @param importExcel importExcel
     * @return DemoImportExcel
     */
    public static DemoImportExcel convertDifference(DemoDo demoDo, DemoImportExcel importExcel) {
        // 举个栗子
        importExcel.setDescription(demoDo.getSellerName());
        return importExcel;
    }
}
