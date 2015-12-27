package tw.openedu.www.discussion;


import java.util.Date;

public interface IAuthorData {

    String getAuthor();
    PinnedAuthor getAuthorLabel();
    Date getCreatedAt();
}
