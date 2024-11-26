package com.ivanfranchin.bitcoinclient.kafka;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ItemSelector
{
    private final Map<String, Map<String, Map<String, String>>> ITEM_SESSION_MAP = new ConcurrentHashMap<>();

    public ItemSelector(final String... items)
    {
        Arrays.stream(items).forEach(item -> ITEM_SESSION_MAP.put(item, new ConcurrentHashMap<>()));
    }

    private boolean exists(final String item)
    {
        return ITEM_SESSION_MAP.containsKey(item);
    }

    public void addItem(final String item)
    {
        ITEM_SESSION_MAP.put(item, new ConcurrentHashMap<>());
    }

    public void removeItem(final String item)
    {
        ITEM_SESSION_MAP.remove(item);
    }

    public Collection<Map<String, String>> getSessions(final String item)
    {
        return ITEM_SESSION_MAP.getOrDefault(item, Collections.emptyMap()).values();
    }

    public Set<String> getSessionIds(final String item)
    {
        return ITEM_SESSION_MAP.getOrDefault(item, Collections.emptyMap()).keySet();
    }

    public void select(final String sessionId, final Map<String, String> session, final List<String> items)
    {
        select(sessionId, session, items.toArray(new String[0]));
    }

    public void select(final String sessionId, final Map<String, String> session, final String... items)
    {
        for (final String item: items)
        {
            if(!exists(item))
            {
                addItem(item);
            }

            ITEM_SESSION_MAP.get(item).put(sessionId, session);
        }
    }

    public void unselect(final String sessionId, final List<String> items)
    {
        unselect(sessionId, items.toArray(new String[0]));
    }

    public void unselect(final String sessionId, final String... items)
    {
        if(items.length == 0)
        {
            ITEM_SESSION_MAP.entrySet().parallelStream()
                .forEach(entry -> entry.getValue().remove(sessionId));
        }
        else
        {
            for(final String item : items)
            {
                if(exists(item))
                {
                    ITEM_SESSION_MAP.get(item).remove(sessionId);
                }
            }
        }
    }
}
