package io.jansyk13.proxy.events;

public class DownstreamChannelWritabilityChangedEvent extends ChannelWritabilityChangedEvent implements DownstreamChannelEvent {

    public DownstreamChannelWritabilityChangedEvent(boolean writable) {
        super(writable);
    }

}
