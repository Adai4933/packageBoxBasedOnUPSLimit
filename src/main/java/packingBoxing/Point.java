package packingBoxing;

import lombok.Data;

/**
 * @author Robin Dai
 * (x,y,z)
 */
@Data
public class Point {

    private int x;
    private int y;
    private int z;

    public Point() {}

    public Point(int x, int y, int z) {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
    }

    /**
     * print current point
     *
     * 字体颜色： 黄色：33  绿色：32  青色：36  红色：31  黑色：30  蓝色：34  白色：37
     * 背景颜色： 黄色：43  绿色：42  青色：46  红色：41  黑色：40  蓝色：44  白色：47
     * 字体效果：删除线：9  下划线：4  斜体：3  默认：0  反显：7 或 8  粗体：1
     *
     * Java控制台无法输出背景色
     * Java彩色字体格式："\033[你的字体颜色;字体效果m你的字符（输出的字符）\033[0m"
     */
    public void print() {
        System.out.format("This point has the position x: %d, y: %d, z: %d. \n", this.getX(), this.getY(), this.getZ());
    }

    @Override
    public boolean equals (Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof Point) {
            return this.getX() == ((Point) anObject).getX() && this.getY() == ((Point) anObject).getY() && this.getZ() == ((Point) anObject).getZ();
        }
        return false;
    }
}
