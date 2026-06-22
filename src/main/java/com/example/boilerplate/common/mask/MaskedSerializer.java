package com.example.boilerplate.common.mask;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class MaskedSerializer extends ValueSerializer<String> {

    private final MaskType type;

    public MaskedSerializer() {
        this.type = null;
    }

    private MaskedSerializer(MaskType type) {
        this.type = type;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (type == null) {
            gen.writeString(value);
            return;
        }
        String masked = switch (type) {
            case RRN -> Mask.rrn(value);
            case PHONE -> Mask.phone(value);
            case EMAIL -> Mask.email(value);
        };
        gen.writeString(masked);
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext ctxt, BeanProperty property) {
        if (property == null) {
            return this;
        }
        Masked annotation = property.getAnnotation(Masked.class);
        if (annotation == null) {
            annotation = property.getContextAnnotation(Masked.class);
        }
        if (annotation == null) {
            return this;
        }
        return new MaskedSerializer(annotation.value());
    }
}
