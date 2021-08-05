package com.helospark.tactview.core.timeline.subtimeline;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = ExposedDescriptorDescriptor.Builder.class)
public class ExposedDescriptorDescriptor {
    private String id;
    private String name;
    private String group;

    private ExposedDescriptorDescriptor(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.group = builder.group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "ExposedDescriptorDescriptor [id=" + id + ", name=" + name + ", group=" + group + "]";
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ExposedDescriptorDescriptor)) {
            return false;
        }
        ExposedDescriptorDescriptor castOther = (ExposedDescriptorDescriptor) other;
        return Objects.equals(id, castOther.id) && Objects.equals(name, castOther.name) && Objects.equals(group, castOther.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, group);
    }

    public ExposedDescriptorDescriptor butWithId(String id) {
        return ExposedDescriptorDescriptor.builder()
                .withName(name)
                .withGroup(group)
                .withId(id)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String name;
        private String group;
        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withGroup(String group) {
            this.group = group;
            return this;
        }

        public ExposedDescriptorDescriptor build() {
            return new ExposedDescriptorDescriptor(this);
        }
    }
}
