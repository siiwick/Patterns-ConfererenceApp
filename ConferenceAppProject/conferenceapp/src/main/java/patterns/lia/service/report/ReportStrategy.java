package patterns.lia.service.report;

import java.util.Arrays;
import java.util.List;

import patterns.lia.model.enums.ReportInput;
import patterns.lia.model.report.ReportTable;
import patterns.lia.utilities.LanguageManager;

/**
 * Strategy pattern: every report is its own class with the same methods. the
 * reports screen can run whichever one the admin picks without knowing which it
 * is.
 */
public interface ReportStrategy {

    //message key of the report name
    String getNameKey();

    //what the admin has to choose first (nothing or a conference)
    ReportInput getInput();

    //builds the report. selectedId is the chosen conference, or null for none
    ReportTable generate(Long selectedId);

    //translates column header keys
    default List<String> columns(String... keys) {
        LanguageManager lang = LanguageManager.get();
        return Arrays.stream(keys).map(lang::get).toList();
    }
}
