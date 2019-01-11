package com.helospark.tactview.core.timeline.proceduralclip.text;

import java.awt.Canvas;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function.impl.StepInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.StringProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;
import com.helospark.tactview.core.util.ReflectionUtil;

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

    public TextProceduralClip(TextProceduralClip textProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(textProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(textProceduralClip, this);
    }

    public TextProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter2) {
        super(metadata, node, loadMetadata);
        this.bufferedImageToClipFrameResultConverter = bufferedImageToClipFrameResultConverter2;
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        String currentText = textProvider.getValueAt(relativePosition);
        List<String> lines = Arrays.asList(currentText.split("\n"));
        double currentSize = (sizeProvider.getValueAt(relativePosition) * request.getScale());
        var color = colorProvider.getValueAt(relativePosition);
        ValueListElement fontElement = fontProvider.getValueAt(relativePosition);
        ValueListElement alignmentElement = alignmentProvider.getValueAt(relativePosition);

        Font font = new Font(fontElement.getId(), fontHitsAt(relativePosition), (int) currentSize).deriveFont((float) currentSize);
        FontMetrics fontMetrics = getFontMetrics(font);

        int maxWidth = 0;
        int totalHeight = 0;
        for (var line : lines) {
            int lineWidth = fontMetrics.stringWidth(line);
            totalHeight += fontMetrics.getHeight();
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        BufferedImage bufferedImage = new BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        graphics.setFont(font);
        graphics.setColor(new Color((float) color.red, (float) color.green, (float) color.blue));
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        List<Integer> alignments = findXAlignmentsForLines(lines, alignmentElement.getId(), fontMetrics);

        fontMetrics.stringWidth(currentText);

        float yPosition = fontMetrics.getHeight();
        for (int i = 0; i < lines.size(); ++i) {
            graphics.drawString(lines.get(i), alignments.get(i), yPosition);
            yPosition += fontMetrics.getHeight();
        }

        return bufferedImageToClipFrameResultConverter.convertFromAbgr(bufferedImage);
    }

    private FontMetrics getFontMetrics(Font font) {
        Canvas c = new Canvas();
        FontMetrics fontMetrics = c.getFontMetrics(font);
        return fontMetrics;
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
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        textProvider = new StringProvider(new StepStringInterpolator());
        sizeProvider = new IntegerProvider(0, 500, new MultiKeyframeBasedDoubleInterpolator(100.0));
        sizeProvider.setScaleDependent();
        colorProvider = new ColorProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(0.6)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(0.6)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(0.6)));
        fontProvider = new ValueListProvider<>(createFontList(), new StepStringInterpolator("Serif.plain"));
        alignmentProvider = new ValueListProvider<>(createAlignmentList(), new StepStringInterpolator("center"));
        italicProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0, new StepInterpolator()));
        boldProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0, new StepInterpolator()));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

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
        result.add(alignmentDescriptor);
        result.add(colorDescriptor);
        result.add(fontDescriptor);
        result.add(boldDescriptor);
        result.add(italicDescriptor);

        return result;
    }

    private List<ValueListElement> createAlignmentList() {
        ValueListElement left = new ValueListElement("left", "left");
        ValueListElement center = new ValueListElement("center", "center");
        ValueListElement right = new ValueListElement("right", "right");

        return List.of(left, center, right);
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
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new TextProceduralClip(this, cloneRequestMetadata);
    }
}
