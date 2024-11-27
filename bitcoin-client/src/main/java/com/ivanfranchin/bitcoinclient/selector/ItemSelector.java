package com.ivanfranchin.bitcoinclient.selector;

import com.ivanfranchin.bitcoinclient.kafka.transaction.TransactionMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ItemSelector<T, U>
{
    private final Map<T, Map<String, SelectorFilter>> ITEM_SESSION_MAP = new ConcurrentHashMap<>();
    private final Map<String, SelectorFilter> SESSION_MAP = new ConcurrentHashMap<>();

    private Function<U, T> keyMapper;

    public final Map<T, U> data = new ConcurrentHashMap<>();

    public ItemSelector(final T... items)
    {
        Arrays.stream(items).forEach(item -> ITEM_SESSION_MAP.put(item, new ConcurrentHashMap<>()));
    }

    public void setKeyMapper(final Function<U, T> mapper)
    {
        keyMapper = mapper;
    }

    public Function<U, T> getKeyMapper()
    {
        return keyMapper;
    }

    private boolean exists(final T item)
    {
        return ITEM_SESSION_MAP.containsKey(item);
    }

    public void addItem(final T item)
    {
        ITEM_SESSION_MAP.put(item, new ConcurrentHashMap<>());
    }

    public void removeItem(final T item)
    {
        ITEM_SESSION_MAP.remove(item);
    }

    public Collection<SelectorFilter> getFilters(final T item)
    {
        return ITEM_SESSION_MAP.getOrDefault(item, Collections.emptyMap()).values();
    }

    public Set<String> getSessionIds(final T item)
    {
        return ITEM_SESSION_MAP.getOrDefault(item, Collections.emptyMap()).keySet();
    }

    public void select(final String sessionId, final SelectorFilter filter)
    {
        Set<T> keyValues = filter.getKeyValues();

        for (final T item: keyValues)
        {
            if(!exists(item))
            {
                addItem(item);
            }

            ITEM_SESSION_MAP.get(item).put(sessionId, filter);
        }
    }

    public void unselect(final String sessionId, final SelectorFilter filter)
    {
        final Set<T> items = filter.getKeyValues();
        final List<T> removedItems = filter.getLatestRemovedKeyValues();

        if(items.isEmpty())
        {
            ITEM_SESSION_MAP.entrySet().parallelStream()
                    .forEach(entry -> entry.getValue().remove(sessionId));

            SESSION_MAP.remove(sessionId);
        }
        else
        {
            for(final T item : removedItems)
            {
                if(exists(item))
                {
                    ITEM_SESSION_MAP.get(item).remove(sessionId);
                }
            }
        }
    }

    public void unselect(final String sessionId)
    {
        ITEM_SESSION_MAP.entrySet().parallelStream()
                .forEach(entry -> entry.getValue().remove(sessionId));

        SESSION_MAP.remove(sessionId);
    }

    public SelectorFilter getFilterOrNew(final String sessionId) {

        if(SESSION_MAP.containsKey(sessionId))
        {
            return SESSION_MAP.get(sessionId);
        }
        else
        {
            final SelectorFilter selectorFilter = new SelectorFilter(sessionId);

            SESSION_MAP.put(sessionId, selectorFilter);

            return selectorFilter;
        }
    }

    public void putData(final T key, final U value)
    {
        data.put(key, value);
    }

    public U getData(final T key)
    {
        return data.get(key);
    }

    public U getData(final T key, final U defaultValue)
    {
        return data.getOrDefault(key, defaultValue);
    }

    public List<U> getData(final SelectorFilter filter) {
        return filter.getKeyValues().stream()
                .filter(keyValue -> data.get(keyValue) != null)
                .map(keyValue -> data.get(keyValue))
                .filter(data -> filter.apply(data))
                .toList();
    }
}
