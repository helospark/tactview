package com.helospark.tactview.core.timeline.proceduralclip.script;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.StringProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.timeline.proceduralclip.script.ScriptService.ScriptInstance;
import com.helospark.tactview.core.util.ClassPathResourceReader;

public class ScriptProceduralClip extends ProceduralVisualClip {
    private StringProvider methodBodyProvider;
    private ScriptService scriptService;

    private String template;

    public ScriptProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, ScriptService scriptService,
            ClassPathResourceReader resourceReader) {
        super(visualMediaMetadata, interval);
        this.template = resourceReader.readClasspathFile("template/procedural-clip-template.template");
        this.scriptService = scriptService;
    }

    public ScriptProceduralClip(ScriptProceduralClip checkerBoardProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(checkerBoardProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(checkerBoardProceduralClip, this);
    }

    public ScriptProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, ScriptService scriptService, ClassPathResourceReader resourceReader) {
        super(metadata, node, loadMetadata);
        this.template = resourceReader.readClasspathFile("template/procedural-clip-template.template");
        this.scriptService = scriptService;
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        String methodBody = methodBodyProvider.getValueAt(relativePosition);
        int bodyHash = methodBody.hashCode();
        String hashToName = bodyHash < 0 ? "1" + Math.abs(bodyHash) : "" + bodyHash;
        String className = "DynamicEffect_" + hashToName;

        String sourceCode = template.replaceFirst(Pattern.quote("{{BODY}}"), Matcher.quoteReplacement(methodBody))
                .replaceFirst(Pattern.quote("{{CLASS_NAME}}"), Matcher.quoteReplacement(className));

        try {
            ScriptInstance helloClass = scriptService.getScript("com.helospark.tactview.core.timeline.proceduralclip.script.dynamic." + className, sourceCode);
            Method method = helloClass.clazz.getMethod("execute", ScriptColorRequest.class);

            ScriptColorRequest scriptRequest = ScriptColorRequest.builder()
                    .withExpectedWidth(request.getExpectedWidth())
                    .withExpectedHeight(request.getExpectedHeight())
                    .withRelativePosition(relativePosition)
                    .withScale(request.getScale())
                    .build();

            ReadOnlyClipImage result = (ReadOnlyClipImage) method.invoke(helloClass.instance, scriptRequest);

            return result;
        } catch (Exception e) {
            System.out.println(sourceCode);
            e.printStackTrace();
        }

        return ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new ScriptProceduralClip(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        methodBodyProvider = new StringProvider(new StepStringInterpolator("ClipImage result = ClipImage.fromSize(request.expectedWidth, request.expectedHeight);\n" +
                "for (int y = 0; y < request.expectedHeight; ++y) {\n" +
                "  for (int x = 0; x < request.expectedWidth; ++x) {\n" +
                "     int r = (int)(((double)x / request.expectedWidth) * 255);\n" +
                "     int g = (int)(((double)y / request.expectedHeight) * 255);\n" +
                "     result.setRed(r, x, y);\n" +
                "     result.setGreen(g, x, y);\n" +
                "     result.setBlue(128, x, y);\n" +
                "     result.setAlpha(255, x, y);\n" +
                "  }\n" +
                "}\n" +
                "return result;"));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor methodBodyDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(methodBodyProvider)
                .withName("Method body")
                .build();

        result.add(methodBodyDescriptor);

        return result;
    }

}
