package com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.script;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.graalvm.polyglot.proxy.ProxyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.DoubleRange;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Rectangle;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContextProviderData;

@Component
public class JavascriptExpressionEvaluator implements ExpressionScriptEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavascriptExpressionEvaluator.class);
    private volatile boolean systemPropertiesInitialized = false;
    private Queue<ScriptEngine> scriptEngineCache = new ConcurrentLinkedQueue<>();

    @Override
    public <T> T evaluate(String expression, TimelinePosition position, Class<T> toClass, EvaluationContext evaluationContext) {
        T functionResult = null;
        try {
            ScriptEngine graalEngine = createScriptEngine();

            Map<String, Map<String, Object>> variables = getVariables(position, evaluationContext);

            for (var entry : variables.entrySet()) {
                graalEngine.put(entry.getKey(), wrapWithProxyObject(entry.getValue()));
            }

            Object result1 = graalEngine.eval(expression);
            Object result2 = graalEngine.get("result");

            Object result = (result1 != null ? result1 : result2);

            if (result != null) {
                if (toClass.equals(String.class)) {
                    functionResult = (T) String.valueOf(result);
                } else if (toClass.equals(Integer.class) || toClass.equals(Double.class) || toClass.equals(Boolean.class)) {
                    functionResult = (T) result;
                } else if (toClass.equals(Color.class)) {
                    Map map = (Map) result;
                    functionResult = (T) new Color((double) map.get("r"), (double) map.get("g"), (double) map.get("b"));
                } else if (toClass.equals(Point.class)) {
                    Map map = (Map) result;
                    functionResult = (T) new Point((double) map.get("x"), (double) map.get("y"));
                } else if (toClass.equals(InterpolationLine.class)) {
                    Map map = (Map) result;
                    Point start = new Point((double) map.get("x1"), (double) map.get("y1"));
                    Point end = new Point((double) map.get("x2"), (double) map.get("y2"));
                    functionResult = (T) new InterpolationLine(start, end);
                } else if (toClass.equals(Rectangle.class)) {
                    Map map = (Map) result;
                    Point p0 = new Point((double) map.get("x1"), (double) map.get("y1"));
                    Point p1 = new Point((double) map.get("x2"), (double) map.get("y2"));
                    Point p2 = new Point((double) map.get("x3"), (double) map.get("y3"));
                    Point p3 = new Point((double) map.get("x4"), (double) map.get("y4"));
                    functionResult = (T) new Rectangle(List.of(p0, p1, p2, p3));
                } else if (toClass.equals(DoubleRange.class)) {
                    Map map = (Map) result;
                    functionResult = (T) new DoubleRange((double) map.get("low"), (double) map.get("high"));
                } else if (toClass.equals(Polygon.class)) {
                    Map map = (Map) result;

                    int numberOfPoints = (int) map.get("numberOfPoints");

                    Object points = map.get("points");

                    List<Point> polygonPoints = new ArrayList<>();
                    if (points instanceof List) {
                        for (int i = 0; i < numberOfPoints; ++i) {
                            Map currentPointMap = (Map) ((List) points).get(i);
                            polygonPoints.add(new Point((double) currentPointMap.get("x"), (double) currentPointMap.get("y")));
                        }
                    } else {
                        for (int i = 0; i < numberOfPoints; ++i) {
                            Map currentPointMap = (Map) ((Map) points).get(String.valueOf(i));
                            polygonPoints.add(new Point((double) currentPointMap.get("x"), (double) currentPointMap.get("y")));
                        }
                    }

                    functionResult = (T) new Polygon(polygonPoints);
                } else {
                    LOGGER.error("Unsupported type " + toClass);
                }
            }
            if (scriptEngineCache.size() < 5) {
                scriptEngineCache.offer(graalEngine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return functionResult;
    }

    private Object wrapWithProxyObject(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        for (var entry : map.entrySet()) {
            result.put(entry.getKey(), wrapWithProxyObjectRecursively(entry.getValue()));
        }
        return ProxyObject.fromMap(result);
    }

    private Object wrapWithProxyObjectRecursively(Object value) {
        if (value instanceof Map) {
            Map<String, Object> result = new HashMap<>();
            Map<?, ?> map = (Map) value;

            for (var entry : map.entrySet()) {
                result.put((String) entry.getKey(), wrapWithProxyObjectRecursively(entry.getValue()));
            }

            return ProxyObject.fromMap(result);
        } else {
            return value;
        }
    }

    public String replacePlaceholder(String expression, Map<String, String> internalIdMapping) {
        // TODO: This should actually read the code to decide whether to replace a reference
        for (var entry : internalIdMapping.entrySet()) {
            expression = expression.replace(entry.getKey(), entry.getValue());
        }
        return expression;
    }

    public Map<String, Map<String, Object>> getVariables(TimelinePosition position, EvaluationContext evaluationContext) {
        Map<String, Object> providerMap = getProviderDataMap(evaluationContext.getProviderData(), position);
        Map<String, Object> globalsMap = getGlobalsMap(evaluationContext.getGlobals());

        return Map.of(
                "providers", providerMap,
                "globals", globalsMap);
    }

    private Map<String, Object> getGlobalsMap(Map<String, Object> globals) {
        Map<String, Object> result = new HashMap<>();
        for (var element : globals.entrySet()) {
            String name = element.getKey();

            Object converted = convertToScriptType(element.getValue());

            if (converted != null) {
                result.put(name, converted);
            }
        }
        return result;
    }

    private Map<String, Object> getProviderDataMap(Map<String, EvaluationContextProviderData> userSetData, TimelinePosition position) {
        Map<String, Object> result = new HashMap<>();
        for (var entry : userSetData.entrySet()) {
            Map<String, Object> clipElements = new HashMap<>();
            for (var element : entry.getValue().data.entrySet()) {
                String name = element.getKey();
                Object currentValue = element.getValue().getValueAt(position);

                Object converted = convertToScriptType(currentValue);

                if (converted != null) {
                    clipElements.put(name, converted);
                }
            }
            result.put(entry.getKey(), clipElements);
        }
        return result;
    }

    public Object convertToScriptType(Object currentValue) {

        Class<? extends Object> currentClass = currentValue.getClass();

        if (currentClass.equals(Integer.class) || currentClass.equals(String.class) || currentClass.equals(Double.class) || currentClass.equals(Boolean.class)) {
            return currentValue;
        } else if (currentClass.equals(Color.class)) {
            Color color = (Color) currentValue;
            return Map.of(
                    "r", color.getRed(),
                    "g", color.getGreen(),
                    "b", color.getBlue());
        } else if (currentClass.equals(Point.class)) {
            Point point = (Point) currentValue;
            return Map.of(
                    "x", point.x,
                    "y", point.y);
        } else if (currentClass.equals(InterpolationLine.class)) {
            InterpolationLine line = (InterpolationLine) currentValue;
            return Map.of(
                    "x1", line.start.x, "y1", line.start.y,
                    "x2", line.end.x, "y2", line.end.y);
        } else if (currentClass.equals(Rectangle.class)) {
            Rectangle line = (Rectangle) currentValue;
            return Map.of(
                    "x1", line.points.get(0).x, "y1", line.points.get(0).y,
                    "x2", line.points.get(1).x, "y2", line.points.get(1).y,
                    "x3", line.points.get(2).x, "y3", line.points.get(2).y,
                    "x4", line.points.get(3).x, "y4", line.points.get(3).y);
        } else if (currentClass.equals(DoubleRange.class)) {
            DoubleRange point = (DoubleRange) currentValue;
            return Map.of(
                    "low", point.lowEnd,
                    "high", point.highEnd);
        } else if (currentClass.equals(Polygon.class)) {
            Polygon polygon = (Polygon) currentValue;

            Map<String, Object> points = new HashMap<>();
            for (int i = 0; i < polygon.getPoints().size(); ++i) {
                Point currentPoint = polygon.getPoints().get(i);
                points.put(String.valueOf(i), Map.of("x", currentPoint.x, "y", currentPoint.y));
            }

            return Map.of("numberOfPoints", polygon.getPoints().size(),
                    "points", ProxyObject.fromMap(points));
        } else if (currentValue instanceof ValueListElement) {
            return ((ValueListElement) currentValue).getId();
        } else if (currentValue instanceof File) {
            return ((File) currentValue).getAbsolutePath();
        } else {
            LOGGER.error("Unsupported type " + currentClass);
        }
        return null;
    }

    private ScriptEngine createScriptEngine() {
        ScriptEngine graalEngine = scriptEngineCache.poll();
        if (graalEngine != null) {
            return graalEngine;
        } else {
            if (!systemPropertiesInitialized) {
                System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
                systemPropertiesInitialized = true;
            }

            graalEngine = new ScriptEngineManager().getEngineByName("graal.js");
            Bindings bindings = graalEngine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("polyglot.js.allowHostAccess", false);
            bindings.put("polyglot.js.allowAllAccess", false);
            bindings.put("polyglot.js.allowIO", false);
            bindings.put("polyglot.js.allowCreateThread", false);
            bindings.put("polyglot.js.allowHostClassLookup", false);
            bindings.put("polyglot.js.allowHostClassLoading", false);
            return graalEngine;
        }
    }

}
