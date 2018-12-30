package com.helospark.tactview.ui.javafx.uicomponents.pattern;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.scene.image.Image;

@Service
public class TimelineEffectPatternService {
    private static final int RECTANGLE_HEIGHT = 30;
    private static final int CORNER_RADIUS = 14;
    private ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter;
    private NameToIdRepository nameToIdRepository;

    public TimelineEffectPatternService(ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter, NameToIdRepository nameToIdRepository) {
        this.byteBufferToJavaFxImageConverter = byteBufferToJavaFxImageConverter;
        this.nameToIdRepository = nameToIdRepository;
    }

    public Image createImagePatternFor(StatelessEffect clipToUpdate, int width, double zoom) {
        BufferedImage result = new BufferedImage(width, RECTANGLE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) result.getGraphics();

        String name = nameToIdRepository.getNameForId(clipToUpdate.getId());

        if (name == null) {
            name = clipToUpdate.getId();
        }
        name += " (" + clipToUpdate.getClass().getSimpleName() + ")";

        graphics.setPaint(new Color(0, 0, 0, 0));
        graphics.fillRect(0, 0, width, RECTANGLE_HEIGHT);

        GradientPaint gradientPaint = new GradientPaint(0, 0, new Color(51, 204, 255), 0, RECTANGLE_HEIGHT, new Color(0, 102, 204));
        graphics.setPaint(gradientPaint);
        graphics.fill(new RoundRectangle2D.Double(0, 0, width, RECTANGLE_HEIGHT, CORNER_RADIUS, CORNER_RADIUS));

        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        Font font = new Font("TimesRoman", Font.PLAIN | Font.BOLD, 10);
        graphics.setColor(Color.DARK_GRAY);
        graphics.setFont(font);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int fontHeight = fontMetrics.getHeight();

        graphics.drawString(name, 5, fontHeight + (RECTANGLE_HEIGHT - fontHeight) / 2 - 2);

        return byteBufferToJavaFxImageConverter.convertToJavafxImage(result);
    }

}
