package com.helospark.tactview.core.it.util.ui;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.AddClipRequestMetaDataKey;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.clipfactory.sequence.FileNamePatternToFileResolverService;

public class FakeImageSequenceChooserDialog {
    private TimelineManagerAccessor timelineManagerAccessor;

    private String path;
    private String pattern;
    private BigDecimal fps;

    private int channel;
    private TimelinePosition position;

    public FakeImageSequenceChooserDialog(TimelineManagerAccessor timelineManagerAccessor) {
        this.timelineManagerAccessor = timelineManagerAccessor;
    }

    public FakeImageSequenceChooserDialog setPath(String path) {
        this.path = path;
        return this;
    }

    public FakeImageSequenceChooserDialog setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public FakeImageSequenceChooserDialog setFps(BigDecimal fps) {
        this.fps = fps;
        return this;
    }

    public FakeImageSequenceChooserDialog setChannel(int channel) {
        this.channel = channel;
        return this;
    }

    public FakeImageSequenceChooserDialog setPosition(TimelinePosition position) {
        this.position = position;
        return this;
    }

    public void clickOkButton() {
        Map<AddClipRequestMetaDataKey, Object> metadata = new HashMap<>();
        metadata.put(AddClipRequestMetaDataKey.FPS, new BigDecimal(fps.toString()));

        String filePath = path + FileNamePatternToFileResolverService.PATH_FILENAME_SEPARATOR + pattern;

        AddClipRequest clipRequest = AddClipRequest.builder()
                .withChannelId(timelineManagerAccessor.getChannels().get(channel).getId())
                .withPosition(position)
                .withAddClipRequestMetadataKey(metadata)
                .withFilePath(filePath)
                .build();
        timelineManagerAccessor.addClip(clipRequest);
    }

}
