package com.jemini.stayhost.channel.domain.component;

import com.jemini.stayhost.channel.domain.model.Channel;

public interface ChannelReader {

    Channel getById(Long channelId);
}
