package com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail;

import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;

@Component
public class LocalizedDetailRepositoryChain {
    private List<LocalizedDetailRepositoryChainItem> chain;

    public LocalizedDetailRepositoryChain(List<LocalizedDetailRepositoryChainItem> chain) {
        this.chain = chain;
    }

    public Optional<LocalizedDetailDomain> queryData(String id) {
        return chain.stream()
                .filter(a -> a.supports(id))
                .findFirst()
                .map(a -> a.queryData(id));
    }

    public Optional<String> queryDetail(String id) {
        return queryData(id).map(a -> a.getDescription());
    }

    public Optional<String> queryDetailForClip(TimelineClip clip) {
        String id;
        if (clip instanceof ProceduralVisualClip) {
            id = ((ProceduralVisualClip) clip).getProceduralFactoryId();
        } else {
            id = clip.getCreatorFactoryId();
        }
        return queryData(id).map(a -> a.getDescription());
    }
}
