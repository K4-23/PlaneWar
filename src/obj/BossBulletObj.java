package obj;

import caihui.GameWin;
import utils.GameUtils;

import java.awt.*;

public class BossBulletObj extends GameObj{
    public BossBulletObj(Image img, int x, int y, int width, int height, double speed, GameWin frame) {
        super(img, x, y, width, height, speed, frame);
    }


    @Override
    public void paintSelf(Graphics g) {
        super.paintSelf(g);
        y += speed;// 子弹向下移动
        if(y > 900){// 子弹超出屏幕，删除子弹
            this.setX(-700);
            this.setY(700);
            GameUtils.removeList.add(this);
        }
    }

    @Override
    public Rectangle getRec() {
        return super.getRec();
    }
}
