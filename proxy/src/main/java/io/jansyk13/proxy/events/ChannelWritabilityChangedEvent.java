package io.jansyk13.proxy.events;

public class ChannelWritabilityChangedEvent {
    protected final boolean writable;

    public ChannelWritabilityChangedEvent(boolean writable) {
        this.writable = writable;
    }

    public boolean isWritable() {
        return writable;
    }
}
