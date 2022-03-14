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
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderError;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier.BezierDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function.impl.StepInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.FileProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;
import com.helospark.tactview.core.util.ReflectionUtil;

public abstract class AbstractTextProceduralClip extends ProceduralVisualClip {
    private IntegerProvider sizeProvider;
    private DoubleProvider lineHeightMultiplierProvider;
    private ColorProvider colorProvider;
    private ColorProvider outlineColorProvider;
    private DoubleProvider outlineWidthProvider;
    private BooleanProvider isCustomFontProvider;
    private FileProvider customFontProvider;
    private ValueListProvider<ValueListElement> fontProvider;
    private ValueListProvider<ValueListElement> alignmentProvider;
    private BooleanProvider italicProvider;
    private BooleanProvider boldProvider;
    private BooleanProvider strikethroughProvider;
    private DoubleProvider spacingProvider;

    private BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter;
    private FontLoader fontLoader;

    public AbstractTextProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval,
            BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter, FontLoader fontLoader) {
        super(visualMediaMetadata, interval);
        this.bufferedImageToClipFrameResultConverter = bufferedImageToClipFrameResultConverter;
        this.fontLoader = fontLoader;
    }

    public AbstractTextProceduralClip(AbstractTextProceduralClip textProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(textProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(textProceduralClip, this, AbstractTextProceduralClip.class, cloneRequestMetadata);
    }

    public AbstractTextProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter2,
            FontLoader fontLoader) {
        super(metadata, node, loadMetadata);
        this.bufferedImageToClipFrameResultConverter = bufferedImageToClipFrameResultConverter2;
        this.fontLoader = fontLoader;
    }

    protected ReadOnlyClipImage drawText(GetFrameRequest request, TimelinePosition relativePosition, String currentText) {
        List<String> lines = Arrays.asList(currentText.split("\n"));
        double currentSize = (sizeProvider.getValueAt(relativePosition) * request.getScale());
        var color = colorProvider.getValueAt(relativePosition);
        ValueListElement fontElement = fontProvider.getValueAt(relativePosition);
        ValueListElement alignmentElement = alignmentProvider.getValueAt(relativePosition);

        Font font = null;

        if (isCustomFontProvider.getValueAt(relativePosition)) {
            File customFontFile = customFontProvider.getValueAt(relativePosition);
            if (customFontFile != null && customFontFile.exists()) {
                font = fontLoader.loadFont(customFontFile);
            }
            if (font == null) {
                font = new Font(fontElement.getId(), Font.PLAIN, 12);
            }
        } else {
            font = new Font(fontElement.getId(), Font.PLAIN, 12);
        }
        font = font.deriveFont((float) currentSize).deriveFont(fontHitsAt(relativePosition));

        double spacing = spacingProvider.getValueAt(relativePosition);
        Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
        attributes.put(TextAttribute.TRACKING, spacing);
        if (strikethroughProvider.getValueAt(relativePosition)) {
            attributes.put(TextAttribute.STRIKETHROUGH, true);
        }

        font = font.deriveFont(attributes);

        BufferedImage dummyBufferedImage = new BufferedImage((int) Math.ceil(10), (int) Math.ceil(10), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D dummyGraphics = (Graphics2D) dummyBufferedImage.getGraphics();
        dummyGraphics.setFont(font);

        FontMetrics fontMetrics = getFontMetrics(font);

        double maxWidth = 0;
        double totalHeight = 0;
        float lineHeightMultiplier = lineHeightMultiplierProvider.getValueAt(relativePosition).floatValue();
        for (var line : lines) {
            double lineWidth = dummyGraphics.getFontMetrics().stringWidth(line);
            if (spacing < 0.0) {
                lineWidth *= ((1.0 + Math.abs(spacing)) * 0.97); // TODO: seems like a Java bug with spacing < 0.0, the width will be incorrect
            }
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

        isCustomFontProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
        customFontProvider = new FileProvider(List.of("*.ttf", "*.woff", "*.otf"), new StepStringInterpolator());

        alignmentProvider = new ValueListProvider<>(createAlignmentList(), new StepStringInterpolator("center"));
        italicProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0, new StepInterpolator()));
        boldProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0, new StepInterpolator()));
        strikethroughProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0, new StepInterpolator()));

        lineHeightMultiplierProvider = new DoubleProvider(0.0, 10.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
        outlineWidthProvider = new DoubleProvider(0.0, 100.0, new MultiKeyframeBasedDoubleInterpolator(0.0));
        outlineColorProvider = ColorProvider.fromDefaultValue(0.0, 0.0, 0.0);
        spacingProvider = new DoubleProvider(-0.2, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.0));
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
        ValueProviderDescriptor isCustomFontProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(isCustomFontProvider)
                .withName("custom font")
                .withGroup("font")
                .build();
        ValueProviderDescriptor fontDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fontProvider)
                .withShowPredicate(position -> !isCustomFontProvider.getValueAt(position))
                .withName("font")
                .withGroup("font")
                .build();
        ValueProviderDescriptor customFontProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(customFontProvider)
                .withShowPredicate(position -> isCustomFontProvider.getValueAt(position))
                .withName("font file")
                .withGroup("font")
                .withValidator(position -> validateCustomFont(position))
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
        ValueProviderDescriptor strikethroughDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(strikethroughProvider)
                .withName("strikethrough")
                .withGroup("font")
                .build();
        ValueProviderDescriptor spacingDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(spacingProvider)
                .withName("spacing")
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
        result.add(isCustomFontProviderDescriptor);
        result.add(fontDescriptor);
        result.add(customFontProviderDescriptor);
        result.add(boldDescriptor);
        result.add(italicDescriptor);
        result.add(strikethroughDescriptor);
        result.add(spacingDescriptor);
        result.add(lineHeightMultiplierDescriptor);
        result.add(alignmentDescriptor);
        result.add(outlineWidthProviderDescriptor);
        result.add(outlineColorProviderDescriptor);

        return result;
    }

    private List<ValueProviderError> validateCustomFont(TimelinePosition position) {
        List<ValueProviderError> errors = new ArrayList<>();
        if (isCustomFontProvider.getValueAt(position)) {
            File customFont = customFontProvider.getValueAt(position);
            if (!customFont.exists()) {
                errors.add(new ValueProviderError("File does not exist"));
            } else if (fontLoader.loadFont(customFont) == null) {
                errors.add(new ValueProviderError("Font file is not supported"));
            }
        }
        return errors;
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
