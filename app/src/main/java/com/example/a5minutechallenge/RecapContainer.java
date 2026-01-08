/** Content Container wrapper for recap sections that can contain other content containers.
 * Visually distinguishes recap content from regular learning content.
 **/
package com.example.a5minutechallenge;

public class RecapContainer extends ContentContainer {
    
    private ContentContainer wrappedContainer;
    private String recapTitle;
    
    public RecapContainer(int id) {
        super(id, Types.RECAP);
        this.recapTitle = "Recap";
    }
    
    public RecapContainer setWrappedContainer(ContentContainer wrappedContainer) {
        this.wrappedContainer = wrappedContainer;
        return this;
    }
    
    public ContentContainer getWrappedContainer() {
        return wrappedContainer;
    }
    
    public RecapContainer setRecapTitle(String recapTitle) {
        this.recapTitle = recapTitle;
        return this;
    }
    
    public String getRecapTitle() {
        return recapTitle;
    }
}
