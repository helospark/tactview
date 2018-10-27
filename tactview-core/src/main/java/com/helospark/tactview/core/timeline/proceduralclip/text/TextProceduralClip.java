package com.helospark.tactview.core.timeline.proceduralclip.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.StringProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.proceduralclip.singlecolor.SingleColorProceduralClip;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;

public class TextProceduralClip extends VisualTimelineClip {
    private StringProvider textProvider;
    private IntegerProvider sizeProvider;
    private ColorProvider colorProvider;
    private ValueListProvider<ValueListElement> fontProvider;

    private BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter;

    public TextProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval,
            BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter) {
        super(visualMediaMetadata, interval, TimelineClipType.VIDEO);
        this.bufferedImageToClipFrameResultConverter = bufferedImageToClipFrameResultConverter;
    }

    @Override
    public ClipFrameResult getFrame(GetFrameRequest request) {
        TimelinePosition relativePosition = request.calculateRelativePositionFrom(this);

        int width = request.getExpectedWidth();
        int height = request.getExpectedHeight();

        String currentText = textProvider.getValueAt(relativePosition);
        int currentSize = sizeProvider.getValueAt(relativePosition);
        var color = colorProvider.getValueAt(relativePosition);
        ValueListElement fontElement = fontProvider.getValueAt(relativePosition);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        graphics.setFont(new Font(fontElement.getId(), Font.PLAIN, currentSize));
        graphics.setColor(new Color((float) color.red, (float) color.green, (float) color.blue));

        FontMetrics fontMetrics = graphics.getFontMetrics();

        int yPosition = 40;
        for (var line : currentText.split("\n")) {
            graphics.drawString(line, 40, yPosition);
            yPosition += fontMetrics.getHeight();
        }

        ClipFrameResult frameResult = bufferedImageToClipFrameResultConverter.convertFromAbgr(bufferedImage);

        return applyEffects(relativePosition, frameResult, request);
    }

    @Override
    public ByteBuffer requestFrame(TimelinePosition position, int width, int height) {
        // TODO something is very wrong here
        throw new IllegalStateException();
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptors() {
        List<ValueProviderDescriptor> result = super.getDescriptors();

        textProvider = new StringProvider(new StringInterpolator());
        sizeProvider = new IntegerProvider(0, 255, new DoubleInterpolator(20.0));
        colorProvider = new ColorProvider(new DoubleProvider(new DoubleInterpolator(0.6)),
                new DoubleProvider(new DoubleInterpolator(0.6)),
                new DoubleProvider(new DoubleInterpolator(0.6)));
        fontProvider = new ValueListProvider<>(createFontList(), new StringInterpolator("Serif.plain"));

        ValueProviderDescriptor textDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(textProvider)
                .withName("Text")
                .build();
        ValueProviderDescriptor sizeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(sizeProvider)
                .withName("Size")
                .build();
        ValueProviderDescriptor colorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorProvider)
                .withName("color")
                .build();
        ValueProviderDescriptor fontDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fontProvider)
                .withName("font")
                .build();

        result.add(textDescriptor);
        result.add(sizeDescriptor);
        result.add(colorDescriptor);
        result.add(fontDescriptor);

        return result;
    }

    private List<ValueListElement> createFontList() {
        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();

        return Arrays.asList(ge.getAllFonts())
                .stream()
                .map(f -> new ValueListElement(f.getName(), f.getFontName(Locale.US)))
                .collect(Collectors.toList());
    }

    @Override
    public VisualMediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    protected TimelineClip cloneClip() {
        return new SingleColorProceduralClip(mediaMetadata, interval);
    }
}
