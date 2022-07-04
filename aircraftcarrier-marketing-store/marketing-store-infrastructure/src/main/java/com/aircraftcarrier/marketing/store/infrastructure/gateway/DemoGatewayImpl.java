package com.aircraftcarrier.marketing.store.infrastructure.gateway;

import com.aircraftcarrier.marketing.store.domain.gateway.DemoGateway;
import com.aircraftcarrier.marketing.store.infrastructure.repository.DemoRepository;
import com.aircraftcarrier.marketing.store.infrastructure.rpc.XxxRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author lzp
 */
@Slf4j
@Component
public class DemoGatewayImpl implements DemoGateway {

    /**
     * DemoRepository
     */
    @Resource
    DemoRepository demoRepository;

    /**
     * XxxRpc
     */
    @Resource
    XxxRpc xxxRpc;


}
