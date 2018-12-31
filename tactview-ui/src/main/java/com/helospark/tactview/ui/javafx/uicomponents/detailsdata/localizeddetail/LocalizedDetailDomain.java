package com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail;

import java.util.Optional;

public class LocalizedDetailDomain {
    private String type;
    private String description;
    private String iconUrl;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Optional<String> getIconUrl() {
        return Optional.ofNullable(iconUrl);
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

}
