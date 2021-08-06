package com.helospark.tactview.core.save;

import java.util.Collections;
import java.util.Set;

import com.helospark.tactview.core.timeline.subtimeline.ExposedDescriptorDescriptor;

public class SaveTemplateRequest {
    private String fileName;
    private boolean packageAllContent;
    private Set<ExposedDescriptorDescriptor> exposedDescriptors;

    private SaveTemplateRequest(Builder builder) {
        this.fileName = builder.fileName;
        this.packageAllContent = builder.packageAllContent;
        this.exposedDescriptors = builder.exposedDescriptors;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isPackageAllContent() {
        return packageAllContent;
    }

    public Set<ExposedDescriptorDescriptor> getExposedDescriptors() {
        return exposedDescriptors;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builderFrom(SaveTemplateRequest saveTemplateRequest) {
        return new Builder(saveTemplateRequest);
    }
    public static final class Builder {
        private String fileName;
        private boolean packageAllContent;
        private Set<ExposedDescriptorDescriptor> exposedDescriptors = Collections.emptySet();
        private Builder() {
        }

        private Builder(SaveTemplateRequest saveTemplateRequest) {
            this.fileName = saveTemplateRequest.fileName;
            this.packageAllContent = saveTemplateRequest.packageAllContent;
            this.exposedDescriptors = saveTemplateRequest.exposedDescriptors;
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder withPackageAllContent(boolean packageAllContent) {
            this.packageAllContent = packageAllContent;
            return this;
        }

        public Builder withExposedDescriptors(Set<ExposedDescriptorDescriptor> exposedDescriptors) {
            this.exposedDescriptors = exposedDescriptors;
            return this;
        }

        public SaveTemplateRequest build() {
            return new SaveTemplateRequest(this);
        }
    }

}
