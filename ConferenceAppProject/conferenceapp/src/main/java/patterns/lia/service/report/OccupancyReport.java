package patterns.lia.service.report;

import java.util.List;

import patterns.lia.dao.ReportDao;
import patterns.lia.model.enums.ReportInput;
import patterns.lia.model.report.ReportTable;
import patterns.lia.utilities.LanguageManager;

//report 5: conferences from most to least full
public class OccupancyReport implements ReportStrategy {

    private final ReportDao reportDao;

    public OccupancyReport(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Override
    public String getNameKey() {
        return "report.occupancy";
    }

    @Override
    public ReportInput getInput() {
        return ReportInput.NONE;
    }

    @Override
    public ReportTable generate(Long selectedId) {
        LanguageManager lang = LanguageManager.get();
        List<List<String>> rows = reportDao.findConferenceOccupancy().stream()
                .map(c -> List.of(c.getTitle(), String.valueOf(c.getCapacity()), String.valueOf(c.getConfirmed()), lang.formatPercent(c.getOccupancyRate())))
                .toList();
        return new ReportTable(columns("column.title", "column.capacity", "column.confirmed", "column.occupancy"), rows);
    }
}
