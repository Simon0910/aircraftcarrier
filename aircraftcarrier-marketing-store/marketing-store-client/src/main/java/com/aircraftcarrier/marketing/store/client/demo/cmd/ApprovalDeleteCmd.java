package com.aircraftcarrier.marketing.store.client.demo.cmd;

import com.aircraftcarrier.framework.model.AbstractCommand;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author lzp
 */
@Data
@AllArgsConstructor
public class ApprovalDeleteCmd extends AbstractCommand {

    private List<Long> ids;

}
