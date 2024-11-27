package com.ivanfranchin.bitcoinclient.selector;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SelectorFilter
{
    private String sessionId;

    private Map<String, Object> metadata = new ConcurrentHashMap<>();
    private Map<String, Object> fields = new ConcurrentHashMap<>();
    private Map<String, Object> fieldMappers = new ConcurrentHashMap<>();

    private Object keyValues = new ConcurrentHashMap<>();
    private Object keyMapper = Function.identity();

    private Object latestRemovedKeyValues = List.of();


    public SelectorFilter(final String sessionId)
    {
        metadata.put("sessionId", sessionId);
        this.sessionId = sessionId;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void putMetadata(final String key, final Object value)
    {
        metadata.put(key, value);
    }

    public Object getMetadata(final String key)
    {
        return metadata.get(key);
    }

    public <T, U> void putKeyValue(final List<T> values, final Function<U, T> mapper)
    {
        keyMapper = mapper;

        if(keyValues == null)
        {
            keyValues = new ConcurrentHashMap<T,T>();
        }

        if(values != null && !values.isEmpty())
        {
            values.forEach(value -> ((Map<T,T>)keyValues).put(value, value));
        }
    }

    public <T, U> void putFieldValue(final String field, final List<T> values, final Function<U, T> mapper)
    {
        final Map<T,T> fieldValues = (Map<T,T>) fields.getOrDefault(field, new ConcurrentHashMap<>());

        if(fieldValues.isEmpty())
        {
            fields.put(field, fieldValues);
            fieldMappers.put(field, mapper);
        }

        if(values != null && !values.isEmpty())
        {
            values.forEach(value -> fieldValues.put(value, value));
        }
    }

    public <T> Set<T> getFieldValues(final String field)
    {
        return ((Map<T, T>) fields.getOrDefault(field, new ConcurrentHashMap<>())).keySet();
    }

    public <T> void removeFieldValue(final String field, final List<T> values)
    {
        final Map<T, T> fieldValues = (Map<T, T>) fields.getOrDefault(field, new ConcurrentHashMap<>());

        if(fieldValues.isEmpty())
        {
            fields.put(field, fieldValues);
            return;
        }

        if(values != null && !values.isEmpty())
        {
            values.forEach(value -> fieldValues.remove(value));
        }
    }

    public <T> void removeKeyValue(final List<T> values)
    {
        if(values != null && !values.isEmpty())
        {
            values.forEach(value -> ((Map<T, T>)keyValues).remove(value));
        }

        latestRemovedKeyValues = values;
    }

    public <T> Set<T> getKeyValues()
    {
        return ((Map<T, T>) keyValues).keySet();
    }

    public <T> List<T> getLatestRemovedKeyValues()
    {
        return (List<T> ) latestRemovedKeyValues;
    }

    public <U, T> Function<U, T> getKeyMapper()
    {
        return (Function<U, T>) keyMapper;
    }

    public <T, U> boolean apply(U message)
    {
        return fields.entrySet().stream()
                .allMatch(field ->
                {
                    final Map<T,T> fieldValues = (Map<T,T>) field.getValue();
                    if(fieldValues.isEmpty())
                    {
                        return true;
                    }

                    final Function<U, T> fieldMapper = (Function<U, T>) fieldMappers.get(field.getKey());

                    T value = fieldMapper.apply(message);
                    return fieldValues.containsKey(value);
                });
    }
}
