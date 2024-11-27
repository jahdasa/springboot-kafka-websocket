package com.ivanfranchin.bitcoinclient.selector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ItemSelector<T>
{
    private final Map<T, Map<String, Map<String, Object>>> ITEM_SESSION_MAP = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> SESSION_MAP = new ConcurrentHashMap<>();

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

    public Collection<Map<String, Object>> getSessions(final T item)
    {
        return ITEM_SESSION_MAP.getOrDefault(item, Collections.emptyMap()).values();
    }

    public Set<String> getSessionIds(final T item)
    {
        return ITEM_SESSION_MAP.getOrDefault(item, Collections.emptyMap()).keySet();
    }

    public void select(final String sessionId, final Map<String, Object> session, final List<T> items)
    {
        for (final T item: items)
        {
            if(!exists(item))
            {
                addItem(item);
            }

            ITEM_SESSION_MAP.get(item).put(sessionId, session);
        }
    }

    public void select(final String sessionId, final Map<String, Object> session, final T... items)
    {
        select(sessionId, session, Arrays.asList(items));
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

    public  Map<String, Object> getSessionOrNew(final String sessionId) {

        if(SESSION_MAP.containsKey(sessionId))
        {
            return SESSION_MAP.get(sessionId);
        }
        else
        {
            final Map<String, Object> session = new ConcurrentHashMap<>();
            session.put("sessionId", sessionId);

            SESSION_MAP.put(sessionId, session);

            return session;
        }
    }
}
