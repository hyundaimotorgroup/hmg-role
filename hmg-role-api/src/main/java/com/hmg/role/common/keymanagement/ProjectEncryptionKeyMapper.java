package com.hmg.role.common.keymanagement;

import com.hmg.role.common.config.CommonMapperConfig;
import com.hmg.role.sdk.fetcher.dto.ProjectEncryptionKeyDto;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(config = CommonMapperConfig.class)
public abstract class ProjectEncryptionKeyMapper {
    public static final ProjectEncryptionKeyMapper INSTANCE =
            Mappers.getMapper(ProjectEncryptionKeyMapper.class);

    public Map<String, String> fromDto(ProjectEncryptionKeyDto o) {
        if (o == null) {
            throw new IllegalArgumentException("Instance cannot be null");
        }

        Class<?> clazz = o.getClass();
        Map<String, String> result = new LinkedHashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // allow access to private fields
            try {
                result.put(field.getName(), String.valueOf(field.get(o)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + field.getName(), e);
            }
        }

        return result;
    }

    public abstract ProjectEncryptionKeyDto fromMap(Map<String, String> o);
}
