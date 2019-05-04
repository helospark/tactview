package com.helospark.tactview.core.timeline.proceduralclip.script;

import org.mdkt.compiler.InMemoryJavaCompiler;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Service
public class ScriptService {

    @Cacheable
    public ScriptInstance getScript(String className, String sourceCode) {
        try {
            Class<?> scriptClass = InMemoryJavaCompiler.newInstance()
                    .useOptions("-proc:none")
                    .compile(className, sourceCode);
            Object instance = scriptClass.newInstance();

            return new ScriptInstance(scriptClass, instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class ScriptInstance {
        public Class<?> clazz;
        public Object instance;

        public ScriptInstance(Class<?> clazz, Object instance) {
            this.clazz = clazz;
            this.instance = instance;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ScriptInstance other = (ScriptInstance) obj;
            if (clazz == null) {
                if (other.clazz != null)
                    return false;
            } else if (!clazz.equals(other.clazz))
                return false;
            return true;
        }

    }
}
