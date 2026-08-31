package com.hmg.role.util.serdeutils;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.hmg.role.util.objectmapper.ObjectMapperFactory;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnapshotWriters {
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    // Attach this via mix-in so we can enable the filter only on the snapshot writer
    @JsonFilter("skipNested")
    private static class SkipNestedMixin {}

    private static final Set<Class<?>> SIMPLE_TYPES =
            Set.of(
                    String.class,
                    Boolean.class,
                    Byte.class,
                    Short.class,
                    Integer.class,
                    Long.class,
                    Float.class,
                    Double.class,
                    BigDecimal.class,
                    BigInteger.class,
                    UUID.class,
                    Date.class);

    private static boolean isSimple(JavaType t) {
        Class<?> raw = t.getRawClass();
        return raw.isPrimitive()
                || SIMPLE_TYPES.contains(raw)
                || Enum.class.isAssignableFrom(raw)
                || Temporal.class.isAssignableFrom(raw); // LocalDate/Time, Instant, etc.
    }

    private static boolean isSimpleContainer(JavaType t) {
        if (t.isCollectionLikeType() || t.isArrayType()) {
            return isSimple(t.getContentType());
        }
        if (t.isMapLikeType()) {
            return isSimple(t.getKeyType()) && isSimple(t.getContentType());
        }
        return false;
    }

    private static final PropertyFilter SKIP_NESTED_FILTER =
            new PropertyFilter() {
                @Override
                public void serializeAsField(
                        Object pojo,
                        JsonGenerator gen,
                        SerializerProvider prov,
                        PropertyWriter writer)
                        throws Exception {
                    if (!(writer instanceof BeanPropertyWriter bpw)) {
                        // this dataType contains a possible collection (Map, List, etc)
                        // use its native writer
                        writer.serializeAsField(pojo, gen, prov);
                        return;
                    }

                    JavaType t = writer.getType();
                    if (isSimple(t) || isSimpleContainer(t)) {
                        writer.serializeAsField(pojo, gen, prov);
                    }

                    // Try to serialize nested as its String id
                    // beware: accesses getter; for JPA proxies this usually
                    // works for getId()
                    Object value = bpw.get(pojo);
                    if (value != null) {
                        try {
                            var m = value.getClass().getMethod("getId");
                            Object id = m.invoke(value);
                            if (id != null) {
                                gen.writeFieldName(writer.getName() + "Id");
                                gen.writeObject(id); // writes string id
                                return;
                            }
                        } catch (NoSuchMethodException ignored) {
                            // no getId -> drop
                        }
                    }
                    // else drop the field
                }

                @Override
                public void serializeAsElement(
                        Object o,
                        JsonGenerator jsonGenerator,
                        SerializerProvider serializerProvider,
                        PropertyWriter propertyWriter)
                        throws Exception {}

                @Override
                public void depositSchemaProperty(
                        PropertyWriter writer,
                        ObjectNode propertiesNode,
                        SerializerProvider provider) {}

                @Override
                public void depositSchemaProperty(
                        PropertyWriter propertyWriter,
                        JsonObjectFormatVisitor jsonObjectFormatVisitor,
                        SerializerProvider serializerProvider)
                        throws JsonMappingException {}
            };

    /**
     * Build a writer that serializes only simple scalars and simple containers. Nested POJOs are
     * omitted and replaced with IDs. Uses built-in base mapper
     */
    public static ObjectWriter shallowObjectWriter() {
        return shallowObjectWriter(OBJECT_MAPPER);
    }

    /**
     * Build a writer that serializes only simple scalars and simple containers. Nested POJOs are
     * omitted and replaced with IDs.
     */
    public static ObjectWriter shallowObjectWriter(ObjectMapper baseMapper) {
        // Copy keeps all registered modules and settings
        // (JavaTime, Hibernate, custom serializers, etc.)
        ObjectMapper m = baseMapper.copy();

        // Activate the filter via mix-in only for this mapper
        m.addMixIn(Object.class, SkipNestedMixin.class);

        SimpleFilterProvider filters =
                new SimpleFilterProvider()
                        .addFilter("skipNested", SKIP_NESTED_FILTER)
                        .setFailOnUnknownId(false);

        return m.writer(filters);
    }
}
