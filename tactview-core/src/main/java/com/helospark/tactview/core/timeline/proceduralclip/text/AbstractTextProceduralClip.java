package com.helospark.tactview.core.timeline.proceduralclip.text;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier.BezierDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function.impl.StepInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;

public abstract class AbstractTextProceduralClip extends ProceduralVisualClip {
    private IntegerProvider sizeProvider;
    private DoubleProvider lineHeightMultiplierProvider;
    private ColorProvider colorProvider;
    private ColorProvider outlineColorProvider;
    private DoubleProvider outlineWidthProvider;
    private ValueListProvider<ValueListElement> fontProvider;
    private ValueListProvider<ValueListElement> alignmentProvider;
    private BooleanProvider italicProvider;
    private BooleanProvider boldProvider;

    private BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter;

    public AbstractTextProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval,
            BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter) {
        super(visualMediaMetadata, interval);
        this.bufferedImageToClipFrameResultConverter = bufferedImageToClipFrameResultConverter;
    }

    public AbstractTextProceduralClip(AbstractTextProceduralClip textProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(textProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(textProceduralClip, this, AbstractTextProceduralClip.class);
    }

    public AbstractTextProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter2) {
        super(metadata, node, loadMetadata);
        this.bufferedImageToClipFrameResultConverter = bufferedImageToClipFrameResultConverter2;
    }

    protected ReadOnlyClipImage drawText(GetFrameRequest request, TimelinePosition relativePosition, String currentText) {
        List<String> lines = Arrays.asList(currentText.split("\n"));
        double currentSize = (sizeProvider.getValueAt(relativePosition) * request.getScale());
        var color = colorProvider.getValueAt(relativePosition);
        ValueListElement fontElement = fontProvider.getValueAt(relativePosition);
        ValueListElement alignmentElement = alignmentProvider.getValueAt(relativePosition);

        Font font = new Font(fontElement.getId(), fontHitsAt(relativePosition), (int) currentSize).deriveFont((float) currentSize);
        FontMetrics fontMetrics = getFontMetrics(font);

        double maxWidth = 0;
        double totalHeight = 0;
        float lineHeightMultiplier = lineHeightMultiplierProvider.getValueAt(relativePosition).floatValue();
        for (var line : lines) {
            double lineWidth = fontMetrics.getStringBounds(line, null).getWidth();
            totalHeight += getLineHeight(font, fontMetrics, lineHeightMultiplier);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        if (maxWidth <= 0.00001 || totalHeight <= 0.00001) {
            return ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
        }

        BufferedImage bufferedImage = new BufferedImage((int) Math.ceil(maxWidth), (int) Math.ceil(totalHeight), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        graphics.setFont(font);
        graphics.setColor(new Color((float) color.red, (float) color.green, (float) color.blue));
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        List<Double> alignments = findXAlignmentsForLines(lines, alignmentElement.getId(), fontMetrics);

        fontMetrics.stringWidth(currentText);

        double outlineWidth = outlineWidthProvider.getValueAt(relativePosition) * request.getScale();
        var outlineColor = outlineColorProvider.getValueAt(relativePosition);

        float yPosition = fontMetrics.getHeight() * lineHeightMultiplier - fontMetrics.getDescent();
        for (int i = 0; i < lines.size(); ++i) {
            String stringToPrint = lines.get(i);
            float xPosition = alignments.get(i).floatValue();
            drawString(graphics, stringToPrint, xPosition, yPosition, outlineWidth, outlineColor, color);
            yPosition += getLineHeight(font, fontMetrics, lineHeightMultiplier);
        }

        return bufferedImageToClipFrameResultConverter.convertFromAbgr(bufferedImage);
    }

    private void drawString(Graphics2D graphics, String stringToPrint, float xPosition, float yPosition, double outlineWidth,
            com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color outlineColor, com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color textColor) {
        AffineTransform original = graphics.getTransform();
        AffineTransform transform = new AffineTransform();
        transform.translate(xPosition, yPosition);
        graphics.setTransform(transform);

        FontRenderContext frc = graphics.getFontRenderContext();
        TextLayout tl = new TextLayout(stringToPrint, graphics.getFont(), frc);
        Shape shape = tl.getOutline(null);

        if (outlineWidth > 0) {
            graphics.setColor(new Color((float) outlineColor.red, (float) outlineColor.green, (float) outlineColor.blue));
            graphics.setStroke(new BasicStroke((float) outlineWidth));
            graphics.draw(shape);
        }
        graphics.setColor(new Color((float) textColor.red, (float) textColor.green, (float) textColor.blue));
        graphics.setTransform(original);

        graphics.drawString(stringToPrint, xPosition, yPosition);
    }

    private float getLineHeight(Font font, FontMetrics fontMetrics, float lineHeightMultiplier) {
        //  return (fontMetrics.getLeading() + fontMetrics.getDescent() + font.getSize2D()) * lineHeightMultiplier;
        return fontMetrics.getHeight();
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

    private List<Double> findXAlignmentsForLines(List<String> lines, String alignmentId, FontMetrics fontMetrics) {
        if (alignmentId.equals("left")) {
            return lines.stream()
                    .map(line -> 0.0)
                    .collect(Collectors.toList());
        } else if (alignmentId.equals("center")) {
            List<Double> lineLengths = findLineLengths(lines, fontMetrics);
            double longestLength = Collections.max(lineLengths);
            return lineLengths.stream()
                    .map(length -> (longestLength - length) / 2)
                    .collect(Collectors.toList());
        } else {
            List<Double> lineLengths = findLineLengths(lines, fontMetrics);
            double longestLength = Collections.max(lineLengths);
            return lineLengths.stream()
                    .map(length -> (longestLength - length))
                    .collect(Collectors.toList());
        }
    }

    private List<Double> findLineLengths(List<String> lines, FontMetrics fontMetrics) {
        return lines.stream()
                .map(line -> fontMetrics.getStringBounds(line, null).getWidth())
                .collect(Collectors.toList());
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        sizeProvider = new IntegerProvider(0, 700, new BezierDoubleInterpolator(250.0));
        sizeProvider.setScaleDependent();
        colorProvider = new ColorProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(0.6)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(0.6)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(0.6)));
        fontProvider = new ValueListProvider<>(createFontList(), new StepStringInterpolator("Serif.plain"));
        alignmentProvider = new ValueListProvider<>(createAlignmentList(), new StepStringInterpolator("center"));
        italicProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0, new StepInterpolator()));
        boldProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0, new StepInterpolator()));
        lineHeightMultiplierProvider = new DoubleProvider(0.0, 10.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
        outlineWidthProvider = new DoubleProvider(0.0, 100.0, new MultiKeyframeBasedDoubleInterpolator(0.0));
        outlineColorProvider = ColorProvider.fromDefaultValue(0.0, 0.0, 0.0);
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor sizeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(sizeProvider)
                .withName("Size")
                .withGroup("font")
                .build();
        ValueProviderDescriptor colorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorProvider)
                .withName("color")
                .withGroup("font")
                .build();
        ValueProviderDescriptor fontDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fontProvider)
                .withName("font")
                .withGroup("font")
                .build();
        ValueProviderDescriptor lineHeightMultiplierDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineHeightMultiplierProvider)
                .withName("line height multiplier")
                .withGroup("multiline")
                .build();
        ValueProviderDescriptor alignmentDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(alignmentProvider)
                .withName("alignment")
                .withGroup("multiline")
                .build();
        ValueProviderDescriptor boldDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(boldProvider)
                .withName("bold")
                .withGroup("font")
                .build();
        ValueProviderDescriptor italicDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(italicProvider)
                .withName("italic")
                .withGroup("font")
                .build();
        ValueProviderDescriptor outlineWidthProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(outlineWidthProvider)
                .withName("outline width")
                .withGroup("outline")
                .build();
        ValueProviderDescriptor outlineColorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(outlineColorProvider)
                .withName("outline color")
                .withGroup("outline")
                .build();

        result.add(sizeDescriptor);
        result.add(colorDescriptor);
        result.add(fontDescriptor);
        result.add(boldDescriptor);
        result.add(italicDescriptor);
        result.add(lineHeightMultiplierDescriptor);
        result.add(alignmentDescriptor);
        result.add(outlineWidthProviderDescriptor);
        result.add(outlineColorProviderDescriptor);

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

}
