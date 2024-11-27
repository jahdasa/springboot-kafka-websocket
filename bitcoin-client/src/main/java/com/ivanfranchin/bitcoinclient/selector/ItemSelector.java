package com.ivanfranchin.bitcoinclient.selector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ItemSelector<T>
{
    private final Map<T, Map<String, SelectorFilter>> ITEM_SESSION_MAP = new ConcurrentHashMap<>();
    private final Map<String, SelectorFilter> SESSION_MAP = new ConcurrentHashMap<>();

    public ItemSelector(final T... items)
    {
        Arrays.stream(items).forEach(item -> ITEM_SESSION_MAP.put(item, new ConcurrentHashMap<>()));
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

    public Collection<SelectorFilter> getSessions(final T item)
    {
        return ITEM_SESSION_MAP.getOrDefault(item, Collections.emptyMap()).values();
    }

    public Set<String> getSessionIds(final T item)
    {
        return ITEM_SESSION_MAP.getOrDefault(item, Collections.emptyMap()).keySet();
    }

    public void select(final String sessionId, final SelectorFilter filter)
    {
        final String selectorKey = (String) filter.getMetadata("selectorKey");
        Set<T> fieldValues = filter.getFieldValues(selectorKey);

        for (final T item: fieldValues)
        {
            if(!exists(item))
            {
                addItem(item);
            }

            ITEM_SESSION_MAP.get(item).put(sessionId, filter);
        }
    }

    public void unselect(final String sessionId, final List<T> items)
    {
        if(items.isEmpty())
        {
            ITEM_SESSION_MAP.entrySet().parallelStream()
                    .forEach(entry -> entry.getValue().remove(sessionId));

            SESSION_MAP.remove(sessionId);
        }
        else
        {
            for(final T item : items)
            {
                if(exists(item))
                {
                    ITEM_SESSION_MAP.get(item).remove(sessionId);
                }
            }
        }
    }

    public void unselect(final String sessionId, final T... items)
    {
        unselect(sessionId, Arrays.asList(items));
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
}
