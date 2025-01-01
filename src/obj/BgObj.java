package obj;

import java.awt.*;
// 背景类
public class BgObj extends GameObj{

    private Image bgImage1;
    private Image bgImage2;
    private int y1;
    private int y2;

    public BgObj(Image img, int x, int y, double speed) {
        super(img, x, y, speed);
        this.bgImage1 = img;
        this.bgImage2 = img;
        this.y1 = y;
        this.y2 = y - 1280; // 第二个背景的初始位置
    }

    @Override
    public void paintSelf(Graphics g) {
        super.paintSelf(g);
        g.drawImage(bgImage1, x, y1, null);
        g.drawImage(bgImage2, x, y2, null);

        y1 += speed;
        y2 += speed;

        if (y1 >= 900) {
            y1 = y2-1280;
        }
        if (y2 >= 900) {
            y2 = y1-1280;
        }
    }
}
