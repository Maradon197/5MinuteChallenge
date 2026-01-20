/** Single Storage item that is displayed on the list. This may become
 * unnecessary in the future
 * This is the displayed container, not the actual file!
 */
package com.example.a5minutechallenge.datawrapper.subject;

public class StorageListItem {
    private String title;

    private SubjectFile file;

    public StorageListItem(String title, SubjectFile file) {
        this.title = title;
        this.file = file;
    }

    public String getTitle() {
        return title;
    }
    public SubjectFile getFile() {
        return file;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setFile(SubjectFile file) {
        this.file = file;
    }
    //some data shit
    //no data here actually because we have datawrappers for that now
}
