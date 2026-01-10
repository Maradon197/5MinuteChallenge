/** Content Container for a video in the 5-minute-challenge screen **/
package com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes;

import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;

//not in use
public class ContainerVideo extends ContentContainer {
    private String url;

    public ContainerVideo(int id) {
        super(id, Types.VIDEO);
    }

    public ContainerVideo setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUrl() {
        return url;
    }
}
