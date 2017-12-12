package io.jansyk13.proxy.events;

public class UpstreamChannelWritabilityChangedEvent extends ChannelWritabilityChangedEvent implements UpstreamChannelEvent {

    public UpstreamChannelWritabilityChangedEvent(boolean writable) {
        super(writable);
    }

}
