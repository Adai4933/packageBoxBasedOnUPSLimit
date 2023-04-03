package packingBoxing;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Dai
 */
@Data
public class Production {

    private int length;
    private int width;
    private int height;
    private int quantity;
    private int size;
    private int volume;
    private List<Point> points = new ArrayList<>(8);
    private int index;

    public Production() {
    }
    public Production(int length, int width, int height) {
        this.length = length;
        this.width = width;
        this.height = height;
        setVolume();
        setSize();
    }
    public Production(int length, int width, int height, int index) {
        this.length = length;
        this.width = width;
        this.height = height;
        this.index = index;
        setVolume();
        setSize();
    }

    public void setSize() {
        this.size = this.length + this.width * 2 + this.height * 2;
    }

    public void setVolume() {
        this.volume = this.length * this.width * this.height;
    }
}
