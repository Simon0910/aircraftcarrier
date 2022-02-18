package com.aircraftcarrier.marketing.store.client.demo.cmd;

import com.aircraftcarrier.framework.model.AbstractCommand;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoDetailQry;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author lzp
 */
@Data
@AllArgsConstructor
public class DemoDetailQryCmd extends AbstractCommand {

    private DemoDetailQry detailQry;
}
