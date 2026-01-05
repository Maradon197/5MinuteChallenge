/** Abstract container class for single display containers in the 5-minute-screen.
 * Different types (c. enum Types) can be created to display different single containers.
 **/

package com.example.a5minutechallenge;

public abstract class ContentContainer {
    private int id;
    public enum Types {
        TEXT,
        VIDEO,
        QUIZ,
        TITLE
    }
    private Types type;

    public ContentContainer(int id, Types type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }
    public Types getType() {return type;}
}
