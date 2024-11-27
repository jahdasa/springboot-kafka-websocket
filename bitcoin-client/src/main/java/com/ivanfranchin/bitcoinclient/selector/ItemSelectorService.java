package com.ivanfranchin.bitcoinclient.selector;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ItemSelectorService
{
    private final Map<String, ItemSelector> selectors = new ConcurrentHashMap<>();

    public ItemSelector findSelectorOrNew(final String selector, final String... items)
    {
        if(selectors.containsKey(selector))
        {
            return selectors.get(selector);
        }
        else
        {
            final ItemSelector itemSelector = new ItemSelector(items);
            selectors.put(selector, itemSelector);

            return itemSelector;
        }
    }

    public void unselect(final String sessionId)
    {
        selectors.values().parallelStream()
            .forEach(selector -> selector.unselect(sessionId));
    }

}
