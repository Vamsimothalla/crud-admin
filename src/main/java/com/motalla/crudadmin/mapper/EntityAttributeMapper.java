package com.motalla.crudadmin.mapper;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class EntityAttributeMapper<T, TD> {
    private final Class<T> aClass;
    private final EntityType<T> entityType;
    private final Metamodel metamodel;
    private final TD result;

    public EntityAttributeMapper(Class<T> aClass, Metamodel metamodel, TD result) {
        this.aClass = aClass;
        this.metamodel = metamodel;
        this.entityType = metamodel.entity(aClass);
        this.result = result;
    }

    public JpaSort getAllFieldsSort() {
        return JpaSort.of(entityType.getAttributes().toArray(Attribute[]::new));
    }

    public Set<Attribute<?, ?>> getSortedAttributes() {
        return getAllFieldsSort().stream()
                .map(Sort.Order::getProperty)
                .map(entityType::getAttribute)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Object getAttributeValue(String attributeName, Object entity) {
        SingularAttribute<?, ?> attribute = entityType.getSingularAttribute(attributeName);
        try {
            Method getter = findGetter(attribute);
            assert getter != null;
            return getter.invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    public Object getAssociatePrimaryKeyValue(String foreignKeyName, Object entity) {
        SingularAttribute<?, ?> foreignKeyAttribute = entityType.getSingularAttribute(foreignKeyName);
        try {
            Object foreignKeyEntity = getAttributeValue(foreignKeyName, entity);
            Class<?> foreignKeyClass = foreignKeyAttribute.getType().getJavaType();
            EntityType<?> foreignKeyEntityType = metamodel.entity(foreignKeyClass);
            SingularAttribute<?, ?> primaryKeyAttribute = foreignKeyEntityType.getId(foreignKeyClass);
            Method getter = findGetter(primaryKeyAttribute);
            assert getter != null;
            return getter.invoke(foreignKeyEntity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Method findGetter(SingularAttribute<?, ?> attribute) {
        String attributeName = attribute.getName();
        String capitalized = Character.toUpperCase(attributeName.charAt(0)) + attributeName.substring(1);
        Method[] methods = aClass.getDeclaredMethods();

        String prefix = "get";
        if (boolean.class.isAssignableFrom(attribute.getType().getJavaType())) {
            prefix = "is";
        }

        for (Method method : methods) {
            if (method.getName().equals(prefix + capitalized)) {
                return method;
            }
        }

        return null;
    }

}