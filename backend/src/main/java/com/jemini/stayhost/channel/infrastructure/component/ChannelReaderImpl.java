package com.jemini.stayhost.channel.infrastructure.component;

import com.jemini.stayhost.channel.domain.component.ChannelReader;
import com.jemini.stayhost.channel.domain.model.Channel;
import com.jemini.stayhost.channel.infrastructure.persistence.ChannelRepository;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChannelReaderImpl implements ChannelReader {

    private final ChannelRepository channelRepository;

    @Override
    public Channel getById(final Long channelId) {
        return channelRepository.findById(channelId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.CHANNEL_ADAPTER_NOT_FOUND));
    }
}
