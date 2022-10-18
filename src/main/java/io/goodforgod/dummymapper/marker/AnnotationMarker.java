package io.goodforgod.dummymapper.marker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Annotation marker
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public class AnnotationMarker {

    private final String name;
    private final boolean isInternal;
    private final boolean isFieldMarked;
    private final boolean isGetterMarked;
    private final boolean isSetterMarked;
    private final Map<String, Object> attributes;

    AnnotationMarker(@NotNull String name,
                     boolean isInternal,
                     boolean isFieldMarked,
                     boolean isGetterMarked,
                     boolean isSetterMarked,
                     @NotNull Map<String, Object> attributes) {
        this.isInternal = isInternal;
        this.name = name;
        this.isFieldMarked = isFieldMarked;
        this.isGetterMarked = isGetterMarked;
        this.isSetterMarked = isSetterMarked;
        this.attributes = attributes;
    }

    public @NotNull String getName() {
        return name;
    }

    public boolean isFieldMarked() {
        return isFieldMarked;
    }

    public boolean isGetterMarked() {
        return isGetterMarked;
    }

    public boolean isSetterMarked() {
        return isSetterMarked;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public @NotNull Map<String, Object> getAttributes() {
        return attributes;
    }

    public boolean haveAttribute(String key) {
        return attributes.containsKey(key);
    }

    public boolean named(@NotNull Class<?> annotation) {
        return named(annotation.getName());
    }

    public boolean named(@NotNull String annotation) {
        return name.equals(annotation);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private boolean isInternal = false;
        private String name;
        private boolean isFieldMarked = false;
        private boolean isGetterMarked = false;
        private boolean isSetterMarked = false;
        private Map<String, Object> attributes = Collections.emptyMap();

        private Builder() {}

        public Builder withName(@NotNull String name) {
            this.name = name;
            return this;
        }

        public Builder withName(@NotNull Class<?> annotation) {
            this.name = annotation.getName();
            return this;
        }

        public Builder ofField() {
            this.isFieldMarked = true;
            return this;
        }

        public Builder ofGetter() {
            this.isGetterMarked = true;
            return this;
        }

        public Builder ofSetter() {
            this.isSetterMarked = true;
            return this;
        }

        public Builder ofInternal() {
            this.isInternal = true;
            return this;
        }

        public Builder withAttribute(@NotNull String name, Object value) {
            if (this.attributes.isEmpty())
                this.attributes = new HashMap<>(2);
            this.attributes.put(name, value);
            return this;
        }

        public Builder withAttributes(@NotNull Map<String, Object> attributes) {
            this.attributes = attributes.isEmpty()
                    ? Collections.emptyMap()
                    : attributes;
            return this;
        }

        public AnnotationMarker build() {
            return new AnnotationMarker(name, isInternal, isFieldMarked, isGetterMarked, isSetterMarked, attributes);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AnnotationMarker that = (AnnotationMarker) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
