package patterns.lia.utilities;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Subject in the Observer pattern: holds the current language and notifies
 * listeners when it changes. There is one shared instance so every screen uses
 * the same language.
 */
public final class LanguageManager {

    public static final Locale ENGLISH = Locale.CANADA;
    public static final Locale FRENCH = Locale.CANADA_FRENCH;

    private static final String BUNDLE_NAME = "i18n.messages";

    private static final LanguageManager INSTANCE = new LanguageManager();

    private final List<LanguageListener> listeners = new ArrayList<>();
    private Locale locale;
    private ResourceBundle bundle;

    private LanguageManager() {
        setLocaleWithoutNotifying(ENGLISH);
    }

    public static LanguageManager get() {
        return INSTANCE;
    }

    public Locale getLocale() {
        return locale;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public void addListener(LanguageListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(LanguageListener listener) {
        listeners.remove(listener);
    }

    //changes the language and notifies every listener
    public void setLocale(Locale newLocale) {
        setLocaleWithoutNotifying(newLocale);
        new ArrayList<>(listeners).forEach(listener -> listener.onLanguageChanged(bundle));
    }

    //switches between English and French
    public void toggle() {
        setLocale(locale.equals(ENGLISH) ? FRENCH : ENGLISH);
    }

    //text for a key in the current language
    public String get(String key) {
        return bundle.getString(key);
    }

    //text for a key with values filled in
    public String format(String key, Object... args) {
        return new MessageFormat(bundle.getString(key), locale).format(args);
    }

    public String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(locale));
    }

    public String formatNumber(double value) {
        return NumberFormat.getNumberInstance(locale).format(value);
    }

    //0.75 becomes "75 %" in French and "75%" in English
    public String formatPercent(double ratio) {
        return NumberFormat.getPercentInstance(locale).format(ratio);
    }

    private void setLocaleWithoutNotifying(Locale newLocale) {
        this.locale = newLocale;
        this.bundle = ResourceBundle.getBundle(BUNDLE_NAME, newLocale);
    }
}
