package patterns.lia.utilities;

import java.util.ResourceBundle;

/**
 * Observer in the Observer pattern: anything that must react when the language changes.
 */
public interface LanguageListener {

    void onLanguageChanged(ResourceBundle bundle);
}
