package io.opentimeline;

import io.opentimeline.opentime.RationalTime;
import io.opentimeline.opentime.TimeRange;
import io.opentimeline.opentimelineio.*;
import io.opentimeline.util.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrackTest {

    @Test
    public void testSerialize() {
        Track track = new Track.TrackBuilder()
                .setName("foo")
                .build();
        ErrorStatus errorStatus = new ErrorStatus();
        String encoded = track.toJSONString(errorStatus);
        SerializableObject decoded = SerializableObject.fromJSONString(encoded, errorStatus);
        assertEquals(decoded, track);
    }

    @Test
    public void testInstancing() {
        RationalTime length = new RationalTime(5, 1);
        TimeRange tr = new TimeRange(new RationalTime(), length);
        Item it = new Item.ItemBuilder()
                .setSourceRange(tr)
                .build();
        Track sq = new Track.TrackBuilder().build();
        ErrorStatus errorStatus = new ErrorStatus();
        assertTrue(sq.appendChild(it, errorStatus));
        assertEquals(sq.rangeOfChildAtIndex(0, errorStatus), tr);

        assertFalse(sq.appendChild(it, errorStatus));
        assertEquals(errorStatus.getOutcome(), ErrorStatus.Outcome.CHILD_ALREADY_PARENTED);
//        sq.clearChildren();
        try {
            errorStatus.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        errorStatus = new ErrorStatus();
//        List<Composable> children = new ArrayList<>();
//        children.add(it);
//        children.add(it);
//        children.add(it);
//        sq.setChildren(children, errorStatus);
//        assertEquals(errorStatus.getOutcome(), ErrorStatus.Outcome.CHILD_ALREADY_PARENTED);
    }

    @Test
    public void testDeleteParentContainer() {
        Item it = new Item.ItemBuilder().build();
        Track sq = new Track.TrackBuilder().build();
        ErrorStatus errorStatus = new ErrorStatus();
        assertTrue(sq.appendChild(it, errorStatus));
        try {
            sq.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNull(it.parent());
    }

    @Test
    public void testTransactional() {
        Item it = new Item.ItemBuilder().build();
        Track trackA = new Track.TrackBuilder().build();
        Track trackB = new Track.TrackBuilder().build();

        ErrorStatus errorStatus = new ErrorStatus();
        assertTrue(trackA.appendChild(new Composable(it.clone(errorStatus)), errorStatus));
        assertTrue(trackA.appendChild(new Composable(it.clone(errorStatus)), errorStatus));
        assertTrue(trackA.appendChild(new Composable(it.clone(errorStatus)), errorStatus));
        assertEquals(trackA.getChildren().size(), 3);

        assertTrue(trackB.appendChild(new Composable(it.clone(errorStatus)), errorStatus));
        assertTrue(trackB.appendChild(new Composable(it.clone(errorStatus)), errorStatus));
        assertTrue(trackB.appendChild(new Composable(it.clone(errorStatus)), errorStatus));
        assertEquals(trackB.getChildren().size(), 3);

        List<Composable> children = new ArrayList<>();
        children.add(new Composable(it.clone(errorStatus)));
        children.add(new Composable(it.clone(errorStatus)));
        children.add(new Composable(it.clone(errorStatus)));
        children.add(new Composable(it.clone(errorStatus)));
        children.add(trackB.getChildren().get(0));
        trackA.setChildren(children, errorStatus);
        assertEquals(errorStatus.getOutcome(), ErrorStatus.Outcome.CHILD_ALREADY_PARENTED);
        assertEquals(trackA.getChildren().size(), 3);
    }

    @Test
    public void testRange() {
        RationalTime length = new RationalTime(5, 1);
        TimeRange tr = new TimeRange(new RationalTime(), length);
        Item it = new Item.ItemBuilder()
                .setSourceRange(tr)
                .build();
        Track sq = new Track.TrackBuilder().build();
        ErrorStatus errorStatus = new ErrorStatus();
        assertTrue(sq.appendChild(it, errorStatus));
        assertEquals(sq.rangeOfChildAtIndex(0, errorStatus), tr);
        // It is an error to add an item to composition if it is already in
        // another composition.  This clears out the old test composition
        // (and also clears out its parent pointers).
        sq.clearChildren();
        assertTrue(sq.appendChild(it, errorStatus));
        assertTrue(sq.appendChild(new Composable(it.clone(errorStatus)), errorStatus));
        assertTrue(sq.appendChild(new Composable(it.clone(errorStatus)), errorStatus));
        assertTrue(sq.appendChild(new Composable(it.clone(errorStatus)), errorStatus));

        assertEquals(sq.rangeOfChildAtIndex(1, errorStatus),
                new TimeRange(
                        new RationalTime(5, 1),
                        new RationalTime(5, 1)));
        assertEquals(sq.rangeOfChildAtIndex(0, errorStatus),
                new TimeRange(
                        new RationalTime(0, 1),
                        new RationalTime(5, 1)));
        assertEquals(sq.rangeOfChildAtIndex(-1, errorStatus),
                new TimeRange(
                        new RationalTime(15, 1),
                        new RationalTime(5, 1)));
        sq.rangeOfChildAtIndex(11, errorStatus);
        assertEquals(errorStatus.getOutcome(), ErrorStatus.Outcome.ILLEGAL_INDEX);
        try {
            errorStatus.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        errorStatus = new ErrorStatus();
        assertEquals(sq.getDuration(errorStatus), length.add(length).add(length).add(length));

        // add a transition to either side
        RationalTime inOffset = new RationalTime(10, 24);
        RationalTime outOffset = new RationalTime(12, 24);
        TimeRange rangeOfItem = sq.rangeOfChildAtIndex(3, errorStatus);
        Transition trx = new Transition.TransitionBuilder()
                .setInOffset(inOffset)
                .setOutOffset(outOffset)
                .build();
        assertTrue(sq.insertChild(0, new Composable(trx.clone(errorStatus)), errorStatus));
        assertTrue(sq.insertChild(3, new Composable(trx.clone(errorStatus)), errorStatus));
        assertTrue(sq.appendChild(new Composable(trx.clone(errorStatus)), errorStatus));

        // range of Transition
        assertEquals(sq.rangeOfChildAtIndex(3, errorStatus),
                new TimeRange(
                        new RationalTime(230, 24),
                        new RationalTime(22, 24)));
        assertEquals(sq.rangeOfChildAtIndex(-1, errorStatus),
                new TimeRange(
                        new RationalTime(470, 24),
                        new RationalTime(22, 24)));

        // range of item is not altered by insertion of transitions
        assertEquals(sq.rangeOfChildAtIndex(5, errorStatus), rangeOfItem);

        // inOffset and outOffset for the beginning and ending
        assertEquals(sq.getDuration(errorStatus),
                inOffset.add(length).add(length).add(length).add(length).add(outOffset));
    }

    @Test
    public void testRangeOfChild() {
        Clip clip1 = new Clip.ClipBuilder()
                .setName("clip1")
                .setSourceRange(
                        new TimeRange(
                                new RationalTime(100, 24),
                                new RationalTime(50, 24)
                        ))
                .build();
        Clip clip2 = new Clip.ClipBuilder()
                .setName("clip2")
                .setSourceRange(
                        new TimeRange(
                                new RationalTime(101, 24),
                                new RationalTime(50, 24)
                        ))
                .build();
        Clip clip3 = new Clip.ClipBuilder()
                .setName("clip3")
                .setSourceRange(
                        new TimeRange(
                                new RationalTime(102, 24),
                                new RationalTime(50, 24)
                        ))
                .build();
        Track sq = new Track.TrackBuilder()
                .setName("foo")
                .build();
        ErrorStatus errorStatus = new ErrorStatus();
        assertTrue(sq.appendChild(clip1, errorStatus));
        assertTrue(sq.appendChild(clip2, errorStatus));
        assertTrue(sq.appendChild(clip3, errorStatus));

        // track should be as long as the children summed up
        assertEquals(sq.getDuration(errorStatus), new RationalTime(150, 24));

        // sequenced items should all land end to end
        assertEquals(sq.rangeOfChildAtIndex(0, errorStatus).getStartTime()
                , new RationalTime());
        assertEquals(sq.rangeOfChildAtIndex(1, errorStatus).getStartTime()
                , new RationalTime(50, 24));
        assertEquals(sq.rangeOfChildAtIndex(2, errorStatus).getStartTime()
                , new RationalTime(100, 24));
        assertEquals(sq.getRangeOfChild(sq.getChildren().get(2), errorStatus),
                sq.rangeOfChildAtIndex(2, errorStatus));

        assertEquals(sq.rangeOfChildAtIndex(0, errorStatus).getDuration()
                , new RationalTime(50, 24));
        assertEquals(sq.rangeOfChildAtIndex(1, errorStatus).getDuration()
                , new RationalTime(50, 24));
        assertEquals(sq.rangeOfChildAtIndex(2, errorStatus).getDuration()
                , new RationalTime(50, 24));

        // should trim 5 frames off the front and 5 frames off the back
        TimeRange sqSourceRange = new TimeRange(
                new RationalTime(5, 24),
                new RationalTime(140, 24));
        sq.setSourceRange(sqSourceRange);
        assertEquals(sq.trimmedRangeOfChildAtIndex(0, errorStatus),
                new TimeRange(
                        new RationalTime(5, 24),
                        new RationalTime(45, 24)));
        assertEquals(sq.trimmedRangeOfChildAtIndex(1, errorStatus),
                sq.rangeOfChildAtIndex(1, errorStatus));
        assertEquals(sq.trimmedRangeOfChildAtIndex(2, errorStatus),
                new TimeRange(
                        new RationalTime(100, 24),
                        new RationalTime(45, 24)));

        // get the trimmed range in parent
        assertEquals(new Clip(sq.getChildren().get(0)).getTrimmedRangeInParent(errorStatus),
                sq.getTrimmedRangeOfChild(sq.getChildren().get(0), errorStatus));

        // same test but via iteration
        for (int i = 0; i < sq.getChildren().size(); i++) {
            assertEquals(new Clip(sq.getChildren().get(i)).getTrimmedRangeInParent(errorStatus),
                    sq.getTrimmedRangeOfChild(sq.getChildren().get(i), errorStatus));
        }
    }

    @Test
    public void testRangeTrimmedOut() {
        Clip clip1 = new Clip.ClipBuilder()
                .setName("clip1")
                .setSourceRange(
                        new TimeRange(
                                new RationalTime(100, 24),
                                new RationalTime(50, 24)
                        ))
                .build();
        Clip clip2 = new Clip.ClipBuilder()
                .setName("clip2")
                .setSourceRange(
                        new TimeRange(
                                new RationalTime(101, 24),
                                new RationalTime(50, 24)
                        ))
                .build();

        Track track = new Track.TrackBuilder()
                .setName("foo")
                .build();
        ErrorStatus errorStatus = new ErrorStatus();
        assertTrue(track.appendChild(clip1, errorStatus));
        assertTrue(track.appendChild(clip2, errorStatus));
        // should trim out clip 1
        track.setSourceRange(new TimeRange(
                new RationalTime(60, 24),
                new RationalTime(10, 24)));
        track.trimmedRangeOfChildAtIndex(0, errorStatus);
        assertEquals(errorStatus.getOutcome(), ErrorStatus.Outcome.INVALID_TIME_RANGE);
        try {
            errorStatus.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        errorStatus = new ErrorStatus();
        TimeRange notNothing = track.trimmedRangeOfChildAtIndex(1, errorStatus);
        assertEquals(notNothing, track.getSourceRange());

        // should trim out second clip
        track.setSourceRange(new TimeRange(
                new RationalTime(0, 24),
                new RationalTime(10, 24)));
        track.trimmedRangeOfChildAtIndex(1, errorStatus);
        assertEquals(errorStatus.getOutcome(), ErrorStatus.Outcome.INVALID_TIME_RANGE);
        try {
            errorStatus.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        errorStatus = new ErrorStatus();
        notNothing = track.trimmedRangeOfChildAtIndex(0, errorStatus);
        assertEquals(notNothing, track.getSourceRange());
    }

    @Test
    public void testRangeNested() {
        Clip clip1 = new Clip.ClipBuilder()
                .setName("clip1")
                .setSourceRange(
                        new TimeRange(
                                new RationalTime(100, 24),
                                new RationalTime(50, 24)
                        ))
                .build();
        Clip clip2 = new Clip.ClipBuilder()
                .setName("clip2")
                .setSourceRange(
                        new TimeRange(
                                new RationalTime(101, 24),
                                new RationalTime(50, 24)
                        ))
                .build();
        Clip clip3 = new Clip.ClipBuilder()
                .setName("clip3")
                .setSourceRange(
                        new TimeRange(
                                new RationalTime(102, 24),
                                new RationalTime(50, 24)
                        ))
                .build();
        Track track = new Track.TrackBuilder()
                .setName("inner")
                .build();
        ErrorStatus errorStatus = new ErrorStatus();
        assertTrue(track.appendChild(clip1, errorStatus));
        assertTrue(track.appendChild(clip2, errorStatus));
        assertTrue(track.appendChild(clip3, errorStatus));

        assertEquals(track.getChildren().size(), 3);

        // make a nested track with 3 sub-tracks, each with 3 clips
        Track outerTrack = new Track.TrackBuilder()
                .setName("outer")
                .build();
        assertTrue(outerTrack.appendChild(new Composable(track.clone(errorStatus)), errorStatus));
        assertTrue(outerTrack.appendChild(new Composable(track.clone(errorStatus)), errorStatus));
        assertTrue(outerTrack.appendChild(new Composable(track.clone(errorStatus)), errorStatus));

        // make one long track with 9 clips
        Track longTrack = new Track.TrackBuilder()
                .setName("long")
                .build();
        for (int i = 0; i < 3; i++) {
            List<Composable> children = track.getChildren();
            for (int j = 0; j < children.size(); j++) {
                assertTrue(longTrack.appendChild(
                        new Composable(children.get(j).clone(errorStatus)), errorStatus));
            }
        }

        // the original track's children should have been copied
        outerTrack.getRangeOfChild(track.getChildren().get(1), errorStatus);
        assertEquals(errorStatus.getOutcome(), ErrorStatus.Outcome.NOT_DESCENDED_FROM);
        try {
            errorStatus.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        errorStatus = new ErrorStatus();
        longTrack.getRangeOfChild(track.getChildren().get(1), errorStatus);
        assertEquals(errorStatus.getOutcome(), ErrorStatus.Outcome.NOT_DESCENDED_FROM);
        try {
            errorStatus.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        errorStatus = new ErrorStatus();

        // the nested and long tracks should be the same length
        assertEquals(outerTrack.getDuration(errorStatus), longTrack.getDuration(errorStatus));

        // the 9 clips within both compositions should have the same
        // overall timing, even though the nesting is different.
        List<Composable> outerTrackClips = new ArrayList<>();
        List<Composable> outerTrackChildren = outerTrack.getChildren();
        Track outerTrackChild1 = new Track(outerTrackChildren.get(0));
        Track outerTrackChild2 = new Track(outerTrackChildren.get(1));
        Track outerTrackChild3 = new Track(outerTrackChildren.get(2));
        outerTrackClips.addAll(outerTrackChild1.getChildren());
        outerTrackClips.addAll(outerTrackChild2.getChildren());
        outerTrackClips.addAll(outerTrackChild3.getChildren());
        List<Composable> longTrackClips = longTrack.getChildren();
        assertEquals(longTrackClips.size(), outerTrackClips.size());
        for (int i = 0; i < longTrackClips.size(); i++) {
            assertEquals(outerTrack.getRangeOfChild(outerTrackClips.get(i), errorStatus),
                    longTrack.getRangeOfChild(longTrackClips.get(i), errorStatus));
        }
    }

    @Test
    public void testSetItem() {
        Track seq = new Track.TrackBuilder().build();
        Clip it = new Clip.ClipBuilder().build();
        Clip it2 = new Clip.ClipBuilder().build();
        ErrorStatus errorStatus = new ErrorStatus();
        assertTrue(seq.appendChild(it, errorStatus));
        assertEquals(seq.getChildren().size(), 1);
        assertTrue(seq.setChild(0, it2, errorStatus));
        assertEquals(seq.getChildren().size(), 1);
    }

    @Test
    public void testTransformedTime() {
        Clip clip1 = new Clip.ClipBuilder()
                .setName("clip1")
                .setSourceRange(
                        new TimeRange(
                                new RationalTime(100, 24),
                                new RationalTime(50, 24)
                        ))
                .build();
        Clip clip2 = new Clip.ClipBuilder()
                .setName("clip2")
                .setSourceRange(
                        new TimeRange(
                                new RationalTime(101, 24),
                                new RationalTime(50, 24)
                        ))
                .build();
        Clip clip3 = new Clip.ClipBuilder()
                .setName("clip3")
                .setSourceRange(
                        new TimeRange(
                                new RationalTime(102, 24),
                                new RationalTime(50, 24)
                        ))
                .build();
        Track sq = new Track.TrackBuilder()
                .setName("foo")
                .build();
        ErrorStatus errorStatus = new ErrorStatus();
        assertTrue(sq.appendChild(clip1, errorStatus));
        assertTrue(sq.appendChild(clip2, errorStatus));
        assertTrue(sq.appendChild(clip3, errorStatus));

        Gap fl = new Gap.GapBuilder()
                .setName("GAP")
                .setSourceRange(new TimeRange(
                        new RationalTime(0, 24),
                        new RationalTime(50, 24)))
                .build();
        assertFalse(fl.isVisible());
        List<Composable> sqChildren = sq.getChildren();
        clip1 = new Clip(sqChildren.get(0));
        clip2 = new Clip(sqChildren.get(1));
        clip3 = new Clip(sqChildren.get(2));
        assertEquals(clip1.getName(), "clip1");
        assertEquals(clip2.getName(), "clip2");
        assertEquals(clip3.getName(), "clip3");

        assertEquals(sq.getTransformedTime(new RationalTime(0, 24), clip1, errorStatus),
                new RationalTime(100, 24));
        assertEquals(sq.getTransformedTime(new RationalTime(0, 24), clip2, errorStatus),
                new RationalTime(51, 24));
        assertEquals(sq.getTransformedTime(new RationalTime(0, 24), clip3, errorStatus),
                new RationalTime(2, 24));

        assertEquals(sq.getTransformedTime(new RationalTime(50, 24), clip1, errorStatus),
                new RationalTime(150, 24));
        assertEquals(sq.getTransformedTime(new RationalTime(50, 24), clip2, errorStatus),
                new RationalTime(101, 24));
        assertEquals(sq.getTransformedTime(new RationalTime(50, 24), clip3, errorStatus),
                new RationalTime(52, 24));

        assertEquals(clip1.getTransformedTime(new RationalTime(100, 24), sq, errorStatus),
                new RationalTime(0, 24));
        assertEquals(clip2.getTransformedTime(new RationalTime(101, 24), sq, errorStatus),
                new RationalTime(50, 24));
        assertEquals(clip3.getTransformedTime(new RationalTime(102, 24), sq, errorStatus),
                new RationalTime(100, 24));

        assertEquals(clip1.getTransformedTime(new RationalTime(150, 24), sq, errorStatus),
                new RationalTime(50, 24));
        assertEquals(clip2.getTransformedTime(new RationalTime(151, 24), sq, errorStatus),
                new RationalTime(100, 24));
        assertEquals(clip3.getTransformedTime(new RationalTime(152, 24), sq, errorStatus),
                new RationalTime(150, 24));
    }

    @Test
    public void testNeighborsOfSimple() {
        Track seq = new Track.TrackBuilder().build();
        Transition trans = new Transition.TransitionBuilder()
                .setInOffset(new RationalTime(10, 24))
                .setOutOffset(new RationalTime(10, 24))
                .build();
        ErrorStatus errorStatus = new ErrorStatus();
        assertTrue(seq.appendChild(trans, errorStatus));
//        Pair<Composable, Composable> neighbors = seq.getNeighborsOf(
//                seq.getChildren().get(0), errorStatus, Track.NeighborGapPolicy.never);
//        assertEquals(neighbors, new Pair<Composable, Composable>(null, null));
    }
}
