package io.opentimeline.opentimelineio;

import io.opentimeline.OTIONative;
import io.opentimeline.opentime.RationalTime;
import io.opentimeline.opentime.TimeRange;
import io.opentimeline.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Composition extends Item {

    protected Composition() {
    }

    Composition(OTIONative otioNative) {
        this.nativeManager = otioNative;
    }

    public Composition(
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

    public Composition(Composition.CompositionBuilder builder) {
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
        Effect[] effectsArray = new Effect[effects.size()];
        effectsArray = effects.toArray(effectsArray);
        Marker[] markersArray = new Marker[markers.size()];
        markersArray = markers.toArray(markersArray);
        this.initialize(
                name,
                sourceRange,
                metadata,
                effectsArray,
                markersArray);
        this.nativeManager.className = this.getClass().getCanonicalName();
    }

    private native void initialize(String name,
                                   TimeRange sourceRange,
                                   AnyDictionary metadata,
                                   Effect[] effects,
                                   Marker[] markers);

    public static class CompositionBuilder {
        private String name = "";
        private TimeRange sourceRange = null;
        private AnyDictionary metadata = new AnyDictionary();
        private List<Effect> effects = new ArrayList<>();
        private List<Marker> markers = new ArrayList<>();

        public CompositionBuilder() {
        }

        public Composition.CompositionBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public Composition.CompositionBuilder setSourceRange(TimeRange sourceRange) {
            this.sourceRange = sourceRange;
            return this;
        }

        public Composition.CompositionBuilder setMetadata(AnyDictionary metadata) {
            this.metadata = metadata;
            return this;
        }

        public Composition.CompositionBuilder setEffects(List<Effect> effects) {
            this.effects = effects;
            return this;
        }

        public Composition.CompositionBuilder setMarkers(List<Marker> markers) {
            this.markers = markers;
            return this;
        }

        public Composition build() {
            return new Composition(this);
        }
    }

    public native String getCompositionKind();

    public List<Composable> getChildren() {
        return Arrays.asList(getChildrenNative());
    }

    private native Composable[] getChildrenNative();

    public native void clearChildren();

    public void setChildren(List<Composable> children, ErrorStatus errorStatus) {
        Composable[] childrenArray = new Composable[children.size()];
        childrenArray = children.toArray(childrenArray);
        setChildrenNative(childrenArray, errorStatus);
    }

    private native void setChildrenNative(Composable[] children, ErrorStatus errorStatus);

    public native boolean insertChild(int index, Composable child, ErrorStatus errorStatus);

    public native boolean setChild(int index, Composable child, ErrorStatus errorStatus);

    public native boolean removeChild(int index, ErrorStatus errorStatus);

    public native boolean appendChild(Composable child, ErrorStatus errorStatus);

    public native boolean isParentOf(Composable composable);

    public native Pair<RationalTime, RationalTime> getHandlesOfChild(
            Composable child, ErrorStatus errorStatus);

    public native TimeRange getRangeOfChildAtIndex(int index, ErrorStatus errorStatus);

    public native TimeRange getTrimmedRangeOfChildAtIndex(int index, ErrorStatus errorStatus);

    public native TimeRange getRangeOfChild(Composable child, ErrorStatus errorStatus);

    public native TimeRange getTrimmedRangeOfChild(Composable child, ErrorStatus errorStatus);

    public native TimeRange trimChildRange(TimeRange childRange);

    public native boolean hasChild(Composable child);

    public native HashMap<Composable, TimeRange> getRangeOfAllChildren(ErrorStatus errorStatus);

    public <T extends Composable> Stream<T> eachChild(
            TimeRange searchRange, Class<T> descendedFrom, boolean shallowSearch, ErrorStatus errorStatus) {
        List<Composable> children;
        if (searchRange != null) {
            children = this.getChildren();
        } else {
            children = this.getChildren();
        }

        return children.stream()
                .flatMap(element -> {
                            Stream<T> currentElementStream = Stream.empty();
                            if (element.getClass().isAssignableFrom(descendedFrom))
                                currentElementStream = Stream.concat(Stream.of(descendedFrom.cast(element)), currentElementStream);
                            Stream<T> nestedStream = Stream.empty();
                            if (!shallowSearch && element instanceof Composition) {
                                nestedStream = ((Composition) element).eachChild(
                                        searchRange == null ? null : this.getTransformedTimeRange(searchRange, ((Composition) element), errorStatus),
                                        descendedFrom,
                                        shallowSearch,
                                        errorStatus);
                            }
                            return Stream.concat(currentElementStream, nestedStream);
                        }
                );
    }

    public Stream<Composable> eachChild(TimeRange searchRange, boolean shallowSearch, ErrorStatus errorStatus) {
        return eachChild(searchRange, Composable.class, shallowSearch, errorStatus);
    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() +
                "(" +
                "name=" + this.getName() +
                ", children=[" + this.getChildren()
                .stream().map(Objects::toString).collect(Collectors.joining(", ")) + "]" +
                ", sourceRange=" + this.getSourceRange() +
                ", metadata=" + this.getMetadata() +
                ")";
    }
}
