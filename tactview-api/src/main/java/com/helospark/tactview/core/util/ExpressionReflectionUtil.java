package com.helospark.tactview.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;

public class ExpressionReflectionUtil {

    public static Set<String> getScriptDependencies(Object instance, String excludedId) {
        Map<String, Object> fields = new HashMap<>();
        ReflectionUtil.collectSaveableFields(instance, fields);

        Set<String> result = fields.values()
                .stream()
                .filter(field -> field instanceof KeyframeableEffect)
                .map(field -> (KeyframeableEffect) field)
                .flatMap(provider -> resolveCompositeProviders(provider).stream())
                .filter(provider -> provider.getExpression() != null)
                .flatMap(provider -> collectClipDependencies(provider.getExpression()).stream())
                .filter(id -> !id.equals(excludedId))
                .collect(Collectors.toSet());

        return result;

    }

    private static List<KeyframeableEffect<?>> resolveCompositeProviders(KeyframeableEffect<?> provider) {
        List<KeyframeableEffect<?>> result = new ArrayList<>();

        result.add(provider);
        for (var child : provider.getChildren()) {
            result.addAll(resolveCompositeProviders(child));
        }

        return result;
    }

    private static List<String> collectClipDependencies(String expression) {
        String uuidRegexPattern = "([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})";
        Pattern pattern = Pattern.compile(uuidRegexPattern);

        Matcher matcher = pattern.matcher(expression);
        return matcher.results()
                .map(MatchResult::group)
                .collect(Collectors.toList());
    }

}
