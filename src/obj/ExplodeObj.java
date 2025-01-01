// 玩家飞机子弹爆炸效果
package obj;

import java.awt.*;

public class ExplodeObj extends GameObj{

    static Image[] explodeImage = new Image[32];
    int explodeNum = 0;// 每个爆炸图片只绘制一次
    static {
        for (int i = 0; i < explodeImage.length; i++) {
            explodeImage[i] = Toolkit.getDefaultToolkit().getImage("src/image/explode/" + i + ".png");
        }
    }

    public ExplodeObj(int x, int y) {
        super(x, y);
    }

    @Override
    public void paintSelf(Graphics g) {
        if(explodeNum < explodeImage.length){
            img = explodeImage[explodeNum];
            super.paintSelf(g);
            explodeNum++;
        }
    }
}
