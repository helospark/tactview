package com.helospark.tactview.core.timeline.subtimeline;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.TemplateSaveAndLoadHandler;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.TimelineChannelsState;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.script.JavascriptExpressionEvaluator;
import com.helospark.tactview.core.util.StaticObjectMapper;

@Component
public class SubtimelineHelper {
    private JavascriptExpressionEvaluator javascriptExpressionEvaluator;

    public SubtimelineHelper(JavascriptExpressionEvaluator javascriptExpressionEvaluator) {
        this.javascriptExpressionEvaluator = javascriptExpressionEvaluator;
    }

    public static <T> T readMetadata(JsonNode savedClip, LoadMetadata loadMetadata, Class<T> clazz) {
        try {
            var reader = loadMetadata.getObjectMapperUsed().readerFor(clazz);
            return reader.readValue(savedClip.get("metadata"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Set<ExposedDescriptorDescriptor> copyExposedDescriptors(CloneRequestMetadata cloneRequestMetadata, Set<ExposedDescriptorDescriptor> toCopy) {
        Set<ExposedDescriptorDescriptor> cloned;
        if (!cloneRequestMetadata.isDeepCloneId()) {
            cloned = toCopy.stream()
                    .map(a -> a.butWithId(cloneRequestMetadata.getPreviousId(a.getId())))
                    .collect(Collectors.toSet());
        } else {
            cloned = new HashSet<>(toCopy);
        }

        return cloned;
    }

    public void addDescriptorsFromTimeline(List<ValueProviderDescriptor> result, TimelineManagerAccessor timelineManagerAccessor, Set<ExposedDescriptorDescriptor> enabledDescriptors) {
        for (var channel : timelineManagerAccessor.getChannels()) {
            for (var clip : channel.getAllClips()) {
                for (var descriptor : clip.getDescriptors()) {
                    Optional<ExposedDescriptorDescriptor> enabledDescriptorProperty = findDescriptorById(enabledDescriptors, descriptor.getKeyframeableEffect().getId());
                    if (enabledDescriptorProperty.isPresent()) {
                        ValueProviderDescriptor newDescriptor = ValueProviderDescriptor.builderFrom(descriptor)
                                .withName(Optional.ofNullable(enabledDescriptorProperty.get().getName()).orElse(descriptor.getName()))
                                .withGroup(Optional.ofNullable(enabledDescriptorProperty.get().getGroup()).orElse(clip.getClass().getSimpleName() + " " + descriptor.getGroup().orElse(" properties")))
                                .build();
                        result.add(newDescriptor);
                    }
                }
            }
        }
    }

    private Optional<ExposedDescriptorDescriptor> findDescriptorById(Set<ExposedDescriptorDescriptor> enabledDescriptors, String id) {
        return enabledDescriptors
                .stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    public boolean containsSubtimeline(AddClipRequest request, String node) {
        boolean result = false;

        if (!request.containsFile() || !request.getFilePath().endsWith("." + TemplateSaveAndLoadHandler.TEMPLATE_FILE_EXTENSION)) {
            return false;
        }

        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File rootDirectory = new File(tmpDir, "tactview_save_" + System.currentTimeMillis() + "_" + new Random().nextInt());
        try {
            ZipUtil.unpack(new File(request.getFile().getAbsolutePath()), rootDirectory);

            File fileName = new File(rootDirectory.getAbsolutePath(), TemplateSaveAndLoadHandler.TEMPLATE_FILE_NAME);
            if (fileName.exists()) {
                ObjectMapper mapper = StaticObjectMapper.objectMapper;
                JsonNode tree = mapper.readTree(fileName);
                result = (tree.get(node) != null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        deleteDirectory(rootDirectory);
        return result;
    }

    protected void deleteDirectory(File rootDirectory) {
        try {
            FileUtils.deleteDirectory(rootDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateFieldReferences(TimelineChannelsState timelineState, CloneRequestMetadata cloneMetadata) {
        var allKeyframeableEffects = timelineState.getChannels()
                .stream()
                .flatMap(channel -> channel.getAllClips().stream())
                .flatMap(clip -> {
                    Stream<KeyframeableEffect> list1 = clip.getDescriptors()
                            .stream()
                            .map(a -> a.getKeyframeableEffect());
                    Stream<KeyframeableEffect> list2 = clip.getEffects()
                            .stream()
                            .flatMap(a -> a.getValueProviders().stream())
                            .map(a -> a.getKeyframeableEffect());

                    return Stream.concat(list1, list2);
                })
                .collect(Collectors.toList());
        allKeyframeableEffects.stream()
                .forEach(keyframeable -> {
                    if (keyframeable.getExpression() != null) {
                        String newExpression = javascriptExpressionEvaluator.replacePlaceholder(keyframeable.getExpression(), cloneMetadata.getIdMapping());
                        keyframeable.setExpression(newExpression);
                    }
                });
    }

}
