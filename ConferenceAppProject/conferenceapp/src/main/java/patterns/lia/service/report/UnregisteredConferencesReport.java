package patterns.lia.service.report;

import java.util.List;

import patterns.lia.dao.ReportDao;
import patterns.lia.model.enums.ReportInput;
import patterns.lia.model.report.ReportTable;
import patterns.lia.utilities.LanguageManager;

//report 6: conferences nobody has registered for
public class UnregisteredConferencesReport implements ReportStrategy {

    private final ReportDao reportDao;

    public UnregisteredConferencesReport(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Override
    public String getNameKey() {
        return "report.unregisteredConferences";
    }

    @Override
    public ReportInput getInput() {
        return ReportInput.NONE;
    }

    @Override
    public ReportTable generate(Long selectedId) {
        LanguageManager lang = LanguageManager.get();
        List<List<String>> rows = reportDao.findUnregisteredConferences().stream()
                .map(c -> List.of(c.getTitle(), c.getVenue() == null ? "" : c.getVenue(), lang.formatDateTime(c.getStart()), String.valueOf(c.getCapacity())))
                .toList();
        return new ReportTable(columns("column.title", "column.venue", "column.start", "column.capacity"), rows);
    }
}
