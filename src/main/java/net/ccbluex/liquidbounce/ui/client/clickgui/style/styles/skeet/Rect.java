package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Rect {
    private float x;
    private float y;
    private float width;
    private float height;

    public Rect(){

    }

    public Rect(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
