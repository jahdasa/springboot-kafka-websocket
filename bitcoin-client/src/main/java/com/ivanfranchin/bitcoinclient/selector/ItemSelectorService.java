package com.ivanfranchin.bitcoinclient.selector;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
public class ItemSelectorService
{
    private final Map<String, ItemSelector> selectors = new ConcurrentHashMap<>();

    public <T,U> ItemSelector<T,U> findSelectorOrNew(final String selector, final Function<U, T> mapper)
    {
        if(selectors.containsKey(selector))
        {
            return selectors.get(selector);
        }
        else
        {
            final ItemSelector<T,U> itemSelector = new ItemSelector<>(mapper);
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
