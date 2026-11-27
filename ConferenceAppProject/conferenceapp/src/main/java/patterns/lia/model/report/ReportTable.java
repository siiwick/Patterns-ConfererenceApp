package patterns.lia.model.report;

import java.util.List;

public class ReportTable {

    private final List<String> columns;
    private final List<List<String>> rows;

    public ReportTable(List<String> columns, List<List<String>> rows) {
        this.columns = List.copyOf(columns);
        this.rows = List.copyOf(rows);
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<List<String>> getRows() {
        return rows;
    }
}
