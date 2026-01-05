/** Single Storage item that is displayed on the list. This may become
 * unnecessary in the future
 * This is the displayed container, not the actual file!
 */
package com.example.a5minutechallenge;

public class StorageListItem {
    private String title;

    public StorageListItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    //some data shit
}
