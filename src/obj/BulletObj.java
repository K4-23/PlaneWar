package obj;

import caihui.GameWin;
import utils.GameUtils;

import java.awt.*;
// 玩家飞机子弹
public class BulletObj extends GameObj{
    public BulletObj() {
        super();
    }

    public BulletObj(Image img, int x, int y, int width, int height, double speed, GameWin frame) {
        super(img, x, y, width, height, speed, frame);
    }

    public BulletObj(Image img, int x, int y, double speed) {
        super(img, x, y, speed);
    }

    @Override
    public void paintSelf(Graphics g) {
        super.paintSelf(g);
        y -= speed;// 子弹向上移动
        if(y < 0){//子弹越界，移除子弹
            this.setX(-999);
            this.setY(-999);
            GameUtils.removeList.add(this);
        }
    }

    @Override
    public Rectangle getRec() {
        return super.getRec();
    }

    @Override
    public Image getImg() {
        return super.getImg();
    }
}
