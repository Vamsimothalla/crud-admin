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


/**
 * The EntityAttributeMapper class provides functionality to map and retrieve attributes of
 * an entity class via the JPA metamodel. It facilitates sorting and fetching attribute values
 * and primary keys for JPA entities.
 *
 * @param <T>  The type of the entity class
 * @param <TD> The type for storing result data
 */
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

    private JpaSort getAllFieldsSort() {
        return JpaSort.of(entityType.getAttributes().toArray(Attribute[]::new));
    }



    /**
     * Retrieves a set of attributes sorted based on the defined sorting order.
     *
     * @return a linked hash set of sorted attributes
     */
    public Set<Attribute<?, ?>> getSortedAttributes() {
        return getAllFieldsSort().stream()
                .map(Sort.Order::getProperty)
                .map(entityType::getAttribute)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Retrieves the value of the specified attribute from the given entity.
     *
     * @param attributeName the name of the attribute whose value is to be retrieved
     * @param entity the entity from which the attribute value is to be retrieved
     * @return the value of the specified attribute from the given entity
     */
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


    /**
     * Retrieves the primary key value of an associated entity referenced by a given foreign key.
     *
     * @param foreignKeyName the name of the foreign key attribute in the current entity
     * @param entity the current entity object from which the foreign key value is to be retrieved
     * @return the primary key value of the associated entity referenced by the foreign key
     */
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


    /**
     * Finds the getter method for the specified singular attribute.
     *
     * @param attribute the singular attribute for which the getter method needs to be found
     * @return the getter method for the specified attribute, or null if no getter method is found
     */
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