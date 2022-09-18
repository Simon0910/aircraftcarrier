package com.aircraftcarrier.marketing.store.client.demo.cmd;

import com.aircraftcarrier.framework.model.AbstractCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author lzp
 */
@Getter
@Setter
@AllArgsConstructor
public class DemoDeleteCmd extends AbstractCommand {

    private List<Long> ids;

}
