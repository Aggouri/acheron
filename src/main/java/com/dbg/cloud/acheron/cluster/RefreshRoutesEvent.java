package com.dbg.cloud.acheron.cluster;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
final class RefreshRoutesEvent extends RemoteApplicationEvent {

    public RefreshRoutesEvent(Object source, String originService) {
        super(source, originService);
    }
}
