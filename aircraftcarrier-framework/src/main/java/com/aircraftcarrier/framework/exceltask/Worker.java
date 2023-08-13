package com.aircraftcarrier.framework.exceltask;

import java.io.IOException;

/**
 * Worker
 *
 * @author zhipengliu
 * @date 2023/8/13
 * @since 1.0
 */
interface Worker<T extends AbstractUploadData> {

    String start();

    String stop();

    String reset();

    String resetSuccessSheetRow(String maxSuccessSheetRow) throws IOException;

    String settingFromWithEnd(String fromSheetRow, String endSheetRow);

}
