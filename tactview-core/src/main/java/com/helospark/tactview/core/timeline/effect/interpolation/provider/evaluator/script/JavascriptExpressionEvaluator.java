package com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.script;

import java.util.HashMap;
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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContextProviderData;

@Component
public class JavascriptExpressionEvaluator implements ExpressionScriptEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavascriptExpressionEvaluator.class);
    private volatile boolean systemPropertiesInitialized = false;
    private Queue<ScriptEngine> scriptEngineCache = new ConcurrentLinkedQueue<>();

    @Override
    public <T> T evaluate(String expression, TimelinePosition position, Class<T> toClass, Map<String, EvaluationContextProviderData> userSetData) {
        T functionResult = null;
        try {
            ScriptEngine graalEngine = createScriptEngine();

            setVariables(graalEngine, userSetData, position);

            Object result1 = graalEngine.eval(expression);
            Object result2 = graalEngine.get("result");

            Object result = (result1 != null ? result1 : result2);

            if (result != null) {
                if (toClass.equals(Integer.class) || toClass.equals(String.class) || toClass.equals(Double.class) || toClass.equals(Boolean.class)) {
                    functionResult = (T) result;
                } else if (toClass.equals(Color.class)) {
                    Map map = (Map) result;
                    functionResult = (T) new Color((double) map.get("r"), (double) map.get("g"), (double) map.get("b"));
                } else if (toClass.equals(Point.class)) {
                    Map map = (Map) result;
                    functionResult = (T) new Point((double) map.get("x"), (double) map.get("y"));
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

    public String replacePlaceholder(String expression, Map<String, String> internalIdMapping) {
        // TODO: This should actually read the code to decide whether to replace a reference
        for (var entry : internalIdMapping.entrySet()) {
            expression = expression.replace(entry.getKey(), entry.getValue());
        }
        return expression;
    }

    private void setVariables(ScriptEngine graalEngine, Map<String, EvaluationContextProviderData> userSetData, TimelinePosition position) {
        ProxyObject actualResult = getDataMap(userSetData, position);
        graalEngine.put("data", actualResult);
    }

    private ProxyObject getDataMap(Map<String, EvaluationContextProviderData> userSetData, TimelinePosition position) {
        Map<String, Object> result = new HashMap<>();
        for (var entry : userSetData.entrySet()) {
            Map<String, Object> clipElements = new HashMap<>();
            for (var element : entry.getValue().data.entrySet()) {
                Object currentValue = element.getValue().getValueAt(position);
                String name = element.getKey();
                Class<? extends Object> currentClass = currentValue.getClass();

                if (currentClass.equals(Integer.class) || currentClass.equals(String.class) || currentClass.equals(Double.class) || currentClass.equals(Boolean.class)) {
                    clipElements.put(name, currentValue);
                } else if (currentClass.equals(Color.class)) {
                    Color color = (Color) currentValue;
                    clipElements.put(name, ProxyObject.fromMap(Map.of(
                            "r", color.getRed(),
                            "g", color.getGreen(),
                            "b", color.getBlue())));
                } else if (currentClass.equals(Point.class)) {
                    Point point = (Point) currentValue;
                    clipElements.put(name, ProxyObject.fromMap(Map.of(
                            "x", point.x,
                            "y", point.y)));
                }
            }
            result.put(entry.getKey(), ProxyObject.fromMap(clipElements));
        }
        ProxyObject actualResult = ProxyObject.fromMap(result);
        return actualResult;
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