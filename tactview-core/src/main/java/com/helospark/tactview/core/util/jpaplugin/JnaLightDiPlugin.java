package com.helospark.tactview.core.util.jpaplugin;

import com.helospark.lightdi.conditional.ConditionalAnnotationsExtractor;
import com.helospark.lightdi.internal.InternalDi;
import com.helospark.lightdi.internal.LightDiPlugin;

/**
 * Plugin for JNA.
 * You can use by annotating an interface with @NativeImplementation("libraryName"). LightDi context will contain a singleton implementing that interface 
 * from the native interface, which can be injected in the context like any other beans.
 * @author helospark
 */
public class JnaLightDiPlugin implements LightDiPlugin {

    @Override
    public void preconfigureInternalDi(InternalDi internalDi) {
        internalDi.addDependency(new JnaBeanDefinitionCollectorChainItem());
        internalDi.addDependency(new JnaBeanFactoryChainItem());
    }

    @Override
    public void postconfigureInternalDi(InternalDi internalDi) {
        // since this dependency is added by core DI, we have to set separately
        ConditionalAnnotationsExtractor conditionalExtractor = internalDi.getDependency(ConditionalAnnotationsExtractor.class);
        internalDi.getDependency(JnaBeanDefinitionCollectorChainItem.class).setConditionalExtractor(conditionalExtractor);
    }

}
