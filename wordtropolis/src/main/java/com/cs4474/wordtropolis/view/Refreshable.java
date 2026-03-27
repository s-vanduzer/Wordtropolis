package com.cs4474.wordtropolis.view;

/**
 * Panels that need to re-render when navigated to should implement this interface.
 * MainApp calls refresh() automatically after every showScreen() call.
 */
public interface Refreshable {
    void refresh();
}
