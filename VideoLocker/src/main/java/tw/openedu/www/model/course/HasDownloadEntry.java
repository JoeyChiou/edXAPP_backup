package tw.openedu.www.model.course;

import tw.openedu.www.model.db.DownloadEntry;
import tw.openedu.www.module.storage.IStorage;

/**
 * Created by hanning on 5/20/15.
 */
public interface HasDownloadEntry {
    DownloadEntry getDownloadEntry(IStorage storage);
    long getSize();
}
