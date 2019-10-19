package com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail;

public interface LocalizedDetailRepositoryChainItem {

    public LocalizedDetailDomain queryData(String id);

    public boolean supports(String id);
}
