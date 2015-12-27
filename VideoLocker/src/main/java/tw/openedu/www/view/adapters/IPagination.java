package tw.openedu.www.view.adapters;

/**
 *
 */
public interface IPagination {
    int DEFAULT_PAGE_SIZE = 20;
    int pageSize();
    int numOfPagesLoaded();
    int numOfRecordsLoaded();
    boolean mayHasMorePages();
    void setHasMorePages(boolean hasMorePages);
    void clear();
    void addPage(int numOfRecordInPage);
}
