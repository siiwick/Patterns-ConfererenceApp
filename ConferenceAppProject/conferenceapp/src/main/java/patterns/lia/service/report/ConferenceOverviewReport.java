package patterns.lia.service.report;

import java.util.List;

import patterns.lia.dao.ReportDao;
import patterns.lia.model.enums.ReportInput;
import patterns.lia.model.report.ReportTable;
import patterns.lia.utilities.LanguageManager;

//report 1: every conference with its seats
public class ConferenceOverviewReport implements ReportStrategy {

    private final ReportDao reportDao;

    public ConferenceOverviewReport(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Override
    public String getNameKey() {
        return "report.conferenceOverview";
    }

    @Override
    public ReportInput getInput() {
        return ReportInput.NONE;
    }

    @Override
    public ReportTable generate(Long selectedId) {
        LanguageManager lang = LanguageManager.get();
        List<List<String>> rows = reportDao.findConferenceOverview().stream()
                .map(c -> List.of(c.getTitle(), c.getVenue() == null ? "" : c.getVenue(),
                        lang.formatDateTime(c.getStart()), lang.formatDateTime(c.getEnd()),
                        String.valueOf(c.getCapacity()), String.valueOf(c.getConfirmed()),
                        String.valueOf(c.getAvailableSeats())))
                .toList();
        return new ReportTable(columns("column.title", "column.venue", "column.start", "column.end", "column.capacity", "column.confirmed", "column.available"), rows);
    }
}
