package com.aircraftcarrier.marketing.store.client.demo.cmd;

import com.aircraftcarrier.framework.model.AbstractCommand;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoPageQry;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author lzp
 */
@Data
@AllArgsConstructor
public class DemoPageQryCmd extends AbstractCommand {

    private DemoPageQry pageQry;
}
