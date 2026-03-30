package com.jemini.stayhost.channel.domain.component;

import com.jemini.stayhost.channel.domain.dto.ChannelSyncResult;
import com.jemini.stayhost.channel.domain.dto.InventoryUpdate;
import com.jemini.stayhost.channel.domain.dto.RateUpdate;
import com.jemini.stayhost.channel.domain.model.ChannelPropertyMapping;

import java.util.List;

public interface ChannelAdapter {

    String getChannelCode();

    ChannelSyncResult pushInventory(ChannelPropertyMapping mapping, List<InventoryUpdate> updates);

    ChannelSyncResult pushRates(ChannelPropertyMapping mapping, List<RateUpdate> updates);
}
