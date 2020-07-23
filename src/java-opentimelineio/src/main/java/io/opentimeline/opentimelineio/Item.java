package io.opentimeline.opentimelineio;

import io.opentimeline.opentime.RationalTime;
import io.opentimeline.opentime.TimeRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Item extends Composable {

    protected Item() {
    }

    public Item(
            String name,
            TimeRange sourceRange,
            AnyDictionary metadata,
            List<Effect> effects,
            List<Marker> markers) {
        this.initObject(
                name,
                sourceRange,
                metadata,
                effects,
                markers);
    }

    public Item(Item.ItemBuilder builder) {
        this.initObject(
                builder.name,
                builder.sourceRange,
                builder.metadata,
                builder.effects,
                builder.markers);
    }

    private void initObject(String name,
                            TimeRange sourceRange,
                            AnyDictionary metadata,
                            List<Effect> effects,
                            List<Marker> markers) {
        this.className = this.getClass().getCanonicalName();
        this.initialize(
                name,
                sourceRange,
                metadata,
                (Effect[]) effects.toArray(),
                (Marker[]) markers.toArray());
    }

    private native void initialize(String name,
                                   TimeRange sourceRange,
                                   AnyDictionary metadata,
                                   Effect[] effects,
                                   Marker[] markers);

    public static class ItemBuilder {
        private String name = "";
        private TimeRange sourceRange = null;
        private AnyDictionary metadata = new AnyDictionary();
        private List<Effect> effects = new ArrayList<>();
        private List<Marker> markers = new ArrayList<>();

        public ItemBuilder() {
        }

        public Item.ItemBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public Item.ItemBuilder setSourceRange(TimeRange sourceRange) {
            this.sourceRange = sourceRange;
            return this;
        }

        public Item.ItemBuilder setMetadata(AnyDictionary metadata) {
            this.metadata = metadata;
            return this;
        }

        public Item.ItemBuilder setEffects(List<Effect> effects) {
            this.effects = effects;
            return this;
        }

        public Item.ItemBuilder setMarkers(List<Marker> markers) {
            this.markers = markers;
            return this;
        }

        public Item build() {
            return new Item(this);
        }
    }

    public native boolean isVisible();

    public native boolean isOverlapping();

    public native TimeRange getSourceRange();

    public native void setSourceRange(TimeRange sourceRange);

    public List<Retainer<Effect>> getEffects() {
        return Arrays.asList(getEffectsNative());
    }

    private native Retainer<Effect>[] getEffectsNative();

    public List<Retainer<Marker>> getMarkers() {
        return Arrays.asList(getMarkersNative());
    }

    private native Retainer<Marker>[] getMarkersNative();

    public native RationalTime getDuration(ErrorStatus errorStatus);

    public native TimeRange getAvailableRange(ErrorStatus errorStatus);

    public native TimeRange getTrimmedRange(ErrorStatus errorStatus);

    public native TimeRange getVisibleRange(ErrorStatus errorStatus);

    public native TimeRange getTrimmedRangeInParent(ErrorStatus errorStatus);

    public native TimeRange getRangeRangeInParent(ErrorStatus errorStatus);

    public native RationalTime getTransformedTime(
            RationalTime time,
            Item toItem,
            ErrorStatus errorStatus);

    public native TimeRange getTransformedTimeRange(
            TimeRange timeRange,
            Item toItem,
            ErrorStatus errorStatus);
}
