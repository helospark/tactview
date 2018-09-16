package com.helospark.lightdi.it.plugintest.crudrepository;

import com.helospark.lightdi.conditional.ConditionalAnnotationsExtractor;
import com.helospark.lightdi.internal.InternalDi;
import com.helospark.lightdi.internal.LightDiPlugin;

/**
 * An example plugin, that uses preconfigureInteralDi to add additional dependencies to the LightDi.
 * @author helospark
 */
public class CrudRepositoryPlugin implements LightDiPlugin {

    @Override
    public void preconfigureInternalDi(InternalDi internalDi) {
        internalDi.addDependency(new CrudRepositoryBeanDefinitionCollectorChainItem());
        internalDi.addDependency(new CrudRepositoryBeanFactoryChainItem());
    }

    @Override
    public void postconfigureInternalDi(InternalDi internalDi) {
        // since this dependency is added by core DI, we have to set separately
        ConditionalAnnotationsExtractor conditionalExtractor = internalDi.getDependency(ConditionalAnnotationsExtractor.class);
        internalDi.getDependency(CrudRepositoryBeanDefinitionCollectorChainItem.class).setConditionalExtractor(conditionalExtractor);
    }

}
