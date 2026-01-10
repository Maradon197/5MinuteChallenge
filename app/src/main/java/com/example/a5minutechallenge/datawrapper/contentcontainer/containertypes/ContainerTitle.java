/** Content Container for the title of a 5-minute-challenge screen **/
package com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes;

import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;

public class ContainerTitle extends ContentContainer {
    private String title;

    public ContainerTitle(int id) {
        super(id, Types.TITLE);
    }

    public ContainerTitle setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }
}
