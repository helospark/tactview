package com.helospark.tactview.core.timeline.proceduralclip.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function.impl.StepInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.StringProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;

public class TextProceduralClip extends ProceduralVisualClip {
    private StringProvider textProvider;
    private IntegerProvider sizeProvider;
    private ColorProvider colorProvider;
    private ValueListProvider<ValueListElement> fontProvider;
    private ValueListProvider<ValueListElement> alignmentProvider;
    private BooleanProvider italicProvider;
    private BooleanProvider boldProvider;

    private BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter;

    public TextProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval,
            BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter) {
        super(visualMediaMetadata, interval);
        this.bufferedImageToClipFrameResultConverter = bufferedImageToClipFrameResultConverter;
    }

    public TextProceduralClip(TextProceduralClip textProceduralClip) {
        super(textProceduralClip);
        this.bufferedImageToClipFrameResultConverter = textProceduralClip.bufferedImageToClipFrameResultConverter;
    }

    @Override
    public ClipFrameResult createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        int width = request.getExpectedWidth();
        int height = request.getExpectedHeight();

        String currentText = textProvider.getValueAt(relativePosition);
        double currentSize = (sizeProvider.getValueAt(relativePosition) * request.getScale());
        var color = colorProvider.getValueAt(relativePosition);
        ValueListElement fontElement = fontProvider.getValueAt(relativePosition);
        ValueListElement alignmentElement = alignmentProvider.getValueAt(relativePosition);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        graphics.setFont(new Font(fontElement.getId(), fontHitsAt(relativePosition), (int) currentSize).deriveFont((float) currentSize));
        graphics.setColor(new Color((float) color.red, (float) color.green, (float) color.blue));
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fontMetrics = graphics.getFontMetrics();

        List<String> lines = Arrays.asList(currentText.split("\n"));
        List<Integer> alignments = findXAlignmentsForLines(lines, alignmentElement.getId(), fontMetrics);

        fontMetrics.stringWidth(currentText);

        float yPosition = fontMetrics.getHeight();
        for (int i = 0; i < lines.size(); ++i) {
            graphics.drawString(lines.get(i), alignments.get(i), yPosition);
            yPosition += fontMetrics.getHeight();
        }

        ClipFrameResult frameResult = bufferedImageToClipFrameResultConverter.convertFromAbgr(bufferedImage);

        return applyEffects(relativePosition, frameResult, request);
    }

    private int fontHitsAt(TimelinePosition relativePosition) {
        Boolean italic = italicProvider.getValueAt(relativePosition);
        Boolean bold = boldProvider.getValueAt(relativePosition);

        int result = Font.PLAIN;

        if (italic) {
            result += Font.ITALIC;
        }
        if (bold) {
            result += Font.BOLD;
        }

        return result;
    }

    private List<Integer> findXAlignmentsForLines(List<String> lines, String alignmentId, FontMetrics fontMetrics) {
        if (alignmentId.equals("left")) {
            return lines.stream()
                    .map(line -> 0)
                    .collect(Collectors.toList());
        } else if (alignmentId.equals("center")) {
            List<Integer> lineLengths = findLineLengths(lines, fontMetrics);
            int longestLength = Collections.max(lineLengths);
            return lineLengths.stream()
                    .map(length -> (longestLength - length) / 2)
                    .collect(Collectors.toList());
        } else {
            List<Integer> lineLengths = findLineLengths(lines, fontMetrics);
            int longestLength = Collections.max(lineLengths);
            return lineLengths.stream()
                    .map(length -> (longestLength - length))
                    .collect(Collectors.toList());
        }
    }

    private List<Integer> findLineLengths(List<String> lines, FontMetrics fontMetrics) {
        return lines.stream()
                .map(line -> fontMetrics.stringWidth(line))
                .collect(Collectors.toList());
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        textProvider = new StringProvider(new StringInterpolator());
        sizeProvider = new IntegerProvider(0, 500, new MultiKeyframeBasedDoubleInterpolator(100.0));
        sizeProvider.setScaleDependent();
        colorProvider = new ColorProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(0.6)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(0.6)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(0.6)));
        fontProvider = new ValueListProvider<>(createFontList(), new StringInterpolator("Serif.plain"));
        alignmentProvider = new ValueListProvider<>(createAlignmentList(), new StringInterpolator("left"));
        italicProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(TimelinePosition.ofZero(), 0.0, new StepInterpolator()));
        boldProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(TimelinePosition.ofZero(), 0.0, new StepInterpolator()));

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
        ValueProviderDescriptor alignmentDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(alignmentProvider)
                .withName("alignment")
                .build();
        ValueProviderDescriptor boldDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(boldProvider)
                .withName("bold")
                .build();
        ValueProviderDescriptor italicDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(italicProvider)
                .withName("italic")
                .build();

        result.add(textDescriptor);
        result.add(sizeDescriptor);
        result.add(colorDescriptor);
        result.add(fontDescriptor);
        result.add(alignmentDescriptor);
        result.add(boldDescriptor);
        result.add(italicDescriptor);

        return result;
    }

    private List<ValueListElement> createAlignmentList() {
        ValueListElement left = new ValueListElement("left", "left");
        ValueListElement center = new ValueListElement("center", "center");
        ValueListElement right = new ValueListElement("right", "right");

        return List.of(left, right, center);
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
    public TimelineClip cloneClip() {
        return new TextProceduralClip(this);
    }
}
