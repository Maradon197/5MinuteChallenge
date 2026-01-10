/** Content Container wrapper for recap sections that can contain other content containers.
 * Visually distinguishes recap content from regular learning content.
 **/
package com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes;

import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;

public class ContainerRecap extends ContentContainer {
    
    private ContentContainer wrappedContainer;
    private String recapTitle;
    
    public ContainerRecap(int id) {
        super(id, Types.RECAP);
        this.recapTitle = "Recap";
    }
    
    public ContainerRecap setWrappedContainer(ContentContainer wrappedContainer) {
        this.wrappedContainer = wrappedContainer;
        return this;
    }
    
    public ContentContainer getWrappedContainer() {
        return wrappedContainer;
    }
    
    public ContainerRecap setRecapTitle(String recapTitle) {
        this.recapTitle = recapTitle;
        return this;
    }
    
    public String getRecapTitle() {
        return recapTitle;
    }
}
