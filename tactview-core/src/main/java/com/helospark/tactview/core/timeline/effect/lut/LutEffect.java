package com.helospark.tactview.core.timeline.effect.lut;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.FileProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.lut.AbstractLut;

public class LutEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;
    private LutProviderService lutProviderService;
    private DoubleProvider intensityProvider;
    private BooleanProvider customFileProvider;
    private ValueListProvider<ValueListElement> buildInFilesProvider;

    private FileProvider lutFileProvider;

    private List<String> dropinLocations;

    public LutEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation, LutProviderService lutProviderService, List<String> dropinLocations) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
        this.lutProviderService = lutProviderService;
        this.dropinLocations = dropinLocations;
    }

    public LutEffect(LutEffect lutEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(lutEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(lutEffect, this, cloneRequestMetadata);
    }

    public LutEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation, LutProviderService lutProviderService, List<String> dropinLocations) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
        this.lutProviderService = lutProviderService;
        this.dropinLocations = dropinLocations;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        File lutFile;
        if (customFileProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext())) {
            lutFile = lutFileProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext());
        } else {
            ValueListElement element = buildInFilesProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext());
            String fileName = Optional.ofNullable(element).map(a -> a.getId()).orElse("");
            lutFile = new File(fileName);
        }
        double intensity = intensityProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext());

        if (lutFile.exists()) {
            AbstractLut lut = lutProviderService.provideLutFromFile(lutFile.getAbsolutePath());

            return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelRequest -> {
                Color color = Color.of(pixelRequest.input[0] / 255.0, pixelRequest.input[1] / 255.0, pixelRequest.input[2] / 255.0);
                Color result = lut.apply(color);
                pixelRequest.output[0] = (int) ((result.red * intensity * 255.0 + pixelRequest.input[0] * (1.0 - intensity)));
                pixelRequest.output[1] = (int) ((result.green * intensity * 255.0 + pixelRequest.input[1] * (1.0 - intensity)));
                pixelRequest.output[2] = (int) ((result.blue * intensity * 255.0 + pixelRequest.input[2] * (1.0 - intensity)));
                pixelRequest.output[3] = pixelRequest.input[3];
            });
        } else {
            ClipImage result = ClipImage.sameSizeAs(request.getCurrentFrame());
            result.copyFrom(request.getCurrentFrame());
            return result;
        }

    }

    @Override
    protected void initializeValueProviderInternal() {
        lutFileProvider = new FileProvider("*.cube", new StepStringInterpolator(""));
        intensityProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(1.0));

        List<ValueListElement> droppedInFiles = getDroppedInFiles();
        String defaultFile = "";
        if (droppedInFiles.size() > 0) {
            defaultFile = droppedInFiles.get(0).getId();
        }

        buildInFilesProvider = new ValueListProvider<>(droppedInFiles, new StepStringInterpolator(defaultFile));

        customFileProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(droppedInFiles.size() > 0 ? 0.0 : 1.0));
    }

    private List<ValueListElement> getDroppedInFiles() {
        return dropinLocations.stream()
                .map(folderPath -> new File(folderPath))
                .filter(a -> a.exists())
                .flatMap(a -> listAllFilesUnder(a).stream())
                .filter(a -> a.getName().endsWith(".cube") || a.getName().endsWith(".3dl"))
                .map(a -> new ValueListElement(a.getAbsolutePath(), a.getName()))
                .collect(Collectors.toList());
    }

    private List<File> listAllFilesUnder(File folder) {
        List<File> result = new ArrayList<>();

        listAllFilesUnder(folder, result);

        return result;
    }

    private void listAllFilesUnder(File folder, List<File> result) {
        for (var file : folder.listFiles()) {
            if (file.isDirectory()) {
                listAllFilesUnder(file, result);
            } else {
                result.add(file);
            }
        }
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor customFileDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(customFileProvider)
                .withName("Browse file")
                .build();

        ValueProviderDescriptor lutFileProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lutFileProvider)
                .withName("file")
                .withShowPredicate(a -> customFileProvider.getValueWithoutScriptAt(a))
                .build();

        ValueProviderDescriptor droppedInLutProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(buildInFilesProvider)
                .withName("LUT")
                .withShowPredicate(a -> !customFileProvider.getValueWithoutScriptAt(a))
                .build();

        ValueProviderDescriptor intensityProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(intensityProvider)
                .withName("intensity")
                .build();

        return List.of(intensityProviderDescriptor, customFileDescriptor, droppedInLutProviderDescriptor, lutFileProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new LutEffect(this, cloneRequestMetadata);
    }

}
