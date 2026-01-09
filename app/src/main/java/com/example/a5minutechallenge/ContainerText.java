/** Content Container for a simple text box in the 5-minute-challenge screen **/
package com.example.a5minutechallenge;

public class ContainerText extends ContentContainer {
    private String text;

    public ContainerText(int id) {
        super(id, Types.TEXT);
    }

    public ContainerText setText(String text) {
        this.text = text;
        return this;
    }

    public String getText() {
        return text;
    }
}
