package com.helospark.tactview.core.timeline;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Generated;

public class AddClipRequest {
    private String channelId;
    private TimelinePosition position;
    private String filePath;
    private String proceduralClipId;
    private Map<AddClipRequestMetaDataKey, Object> addClipRequestMetadataKey;

    @Generated("SparkTools")
    private AddClipRequest(Builder builder) {
        this.channelId = builder.channelId;
        this.position = builder.position;
        this.filePath = builder.filePath;
        this.proceduralClipId = builder.proceduralClipId;
        this.addClipRequestMetadataKey = builder.addClipRequestMetadataKey;
    }

    public String getChannelId() {
        return channelId;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public String getFilePath() {
        return filePath;
    }

    public File getFile() {
        return new File(filePath);
    }

    public boolean containsFile() {
        return this.filePath != null && getFile().exists();
    }

    public String getProceduralClipId() {
        return proceduralClipId;
    }

    public Map<AddClipRequestMetaDataKey, Object> getAddClipRequestMetadataKey() {
        return addClipRequestMetadataKey;
    }

    @Override
    public String toString() {
        return "AddClipRequest [channelId=" + channelId + ", position=" + position + ", filePath=" + filePath + ", proceduralClipId=" + proceduralClipId + "]";
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof AddClipRequest)) {
            return false;
        }
        AddClipRequest castOther = (AddClipRequest) other;
        return Objects.equals(channelId, castOther.channelId) && Objects.equals(position, castOther.position) && Objects.equals(filePath, castOther.filePath)
                && Objects.equals(proceduralClipId, castOther.proceduralClipId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, position, filePath, proceduralClipId);
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String channelId;
        private TimelinePosition position;
        private String filePath;
        private String proceduralClipId;
        private Map<AddClipRequestMetaDataKey, Object> addClipRequestMetadataKey = Collections.emptyMap();

        private Builder() {
        }

        public Builder withChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder withProceduralClipId(String proceduralClipId) {
            this.proceduralClipId = proceduralClipId;
            return this;
        }

        public Builder withAddClipRequestMetadataKey(Map<AddClipRequestMetaDataKey, Object> addClipRequestMetadataKey) {
            this.addClipRequestMetadataKey = addClipRequestMetadataKey;
            return this;
        }

        public AddClipRequest build() {
            return new AddClipRequest(this);
        }
    }

}
