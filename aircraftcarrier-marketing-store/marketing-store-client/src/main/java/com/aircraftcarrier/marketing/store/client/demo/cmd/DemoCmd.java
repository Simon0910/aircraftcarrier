package com.aircraftcarrier.marketing.store.client.demo.cmd;

import com.aircraftcarrier.framework.model.AbstractCommand;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoAdd;
import com.aircraftcarrier.marketing.store.client.demo.request.DemoUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author lzp
 */
@Data
@AllArgsConstructor
@Builder
public class DemoCmd extends AbstractCommand {
    DemoAdd demoAdd;
    DemoUpdate demoUpdate;
}
