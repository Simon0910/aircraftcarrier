package com.aircraftcarrier.marketing.store.domain.event;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEvent;

/**
 * @author lzp
 */
@Log4j2
@Getter
public class AccountEvent<T> extends ApplicationEvent {
    private final T data;

    public AccountEvent(T data) {
        super(data);
        this.data = data;
    }
}