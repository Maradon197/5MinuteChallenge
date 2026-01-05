/** Content Container for a video in the 5-minute-challenge screen **/
package com.example.a5minutechallenge;

public class VideoContainer extends ContentContainer {
    private String url;

    public VideoContainer(int id) {
        super(id, Types.VIDEO);
    }

    public VideoContainer setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUrl() {
        return url;
    }
}
