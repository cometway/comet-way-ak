
package com.cometway.util;


/**
 * This interface defines a HeapItem.
 *
 */
public interface IHeapItem
{
    /**
     * Every IHeapItem must provide a way to determine its order
     * when compared to another IHeapItem.
     */
    public boolean greaterThan(IHeapItem item);
}

