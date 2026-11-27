package patterns.lia.service.report;

import java.util.List;

import patterns.lia.dao.ReportDao;
import patterns.lia.model.enums.ReportInput;
import patterns.lia.model.report.ReportTable;
import patterns.lia.utilities.LanguageManager;

//report 3: waiting list of one conference, in order
public class WaitingListReport implements ReportStrategy {

    private final ReportDao reportDao;

    public WaitingListReport(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Override
    public String getNameKey() {
        return "report.waitingList";
    }

    @Override
    public ReportInput getInput() {
        return ReportInput.CONFERENCE;
    }

    @Override
    public ReportTable generate(Long conferenceId) {
        LanguageManager lang = LanguageManager.get();
        List<List<String>> rows = reportDao.findWaitingList(conferenceId).stream()
                .map(p -> List.of(String.valueOf(p.getWaitlistPosition()), p.getFullName(), p.getEmail(), lang.formatDateTime(p.getRegisteredAt())))
                .toList();
        return new ReportTable(columns("column.position", "column.name", "column.email", "column.registeredAt"), rows);
    }
}
