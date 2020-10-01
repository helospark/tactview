package com.helospark.tactview.core.save;

import javax.annotation.Generated;

public class SaveRequest {
    private String fileName;
    private boolean packageAllContent;

    @Generated("SparkTools")
    private SaveRequest(Builder builder) {
        this.fileName = builder.fileName;
        this.packageAllContent = builder.packageAllContent;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isPackageAllContent() {
        return packageAllContent;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String fileName;
        private boolean packageAllContent;

        private Builder() {
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder withPackageAllContent(boolean packageAllContent) {
            this.packageAllContent = packageAllContent;
            return this;
        }

        public SaveRequest build() {
            return new SaveRequest(this);
        }
    }

}
