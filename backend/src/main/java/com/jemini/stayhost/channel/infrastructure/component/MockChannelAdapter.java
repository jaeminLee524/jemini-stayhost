package com.jemini.stayhost.channel.infrastructure.component;

import com.jemini.stayhost.channel.domain.component.ChannelAdapter;
import com.jemini.stayhost.channel.domain.dto.ChannelSyncResult;
import com.jemini.stayhost.channel.domain.dto.InventoryUpdate;
import com.jemini.stayhost.channel.domain.dto.RateUpdate;
import com.jemini.stayhost.channel.domain.model.ChannelPropertyMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MockChannelAdapter implements ChannelAdapter {

    private static final String CHANNEL_CODE = "MOCK";

    @Override
    public String getChannelCode() {
        return CHANNEL_CODE;
    }

    @Override
    public ChannelSyncResult pushInventory(final ChannelPropertyMapping mapping, final List<InventoryUpdate> updates) {
        log.info("[MockChannelAdapter] pushInventory: externalPropertyId={}, updates={}", mapping.getExternalPropertyId(), updates.size());
        return ChannelSyncResult.success(CHANNEL_CODE);
    }

    @Override
    public ChannelSyncResult pushRates(final ChannelPropertyMapping mapping, final List<RateUpdate> updates) {
        log.info("[MockChannelAdapter] pushRates: externalPropertyId={}, updates={}", mapping.getExternalPropertyId(), updates.size());
        return ChannelSyncResult.success(CHANNEL_CODE);
    }
}
