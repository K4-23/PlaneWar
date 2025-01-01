// 敌方飞机爆炸特效
package obj;

import java.awt.*;

public class ExplodeObj1 extends GameObj{

    static Image[] explode1Image = new Image[40];
    int explode1Num = 0;// 每个爆炸图片只绘制一次
    static {
        for (int i = 0; i < explode1Image.length; i++) {
            explode1Image[i] = Toolkit.getDefaultToolkit().getImage("src/image/explode1/1 (" + (i+1) + ").png");
        }
    }

    public ExplodeObj1(int x, int y) {
        super(x, y);
    }

    @Override
    public void paintSelf(Graphics g) {
        if(explode1Num < explode1Image.length){
            img = explode1Image[explode1Num];
            super.paintSelf(g);
            explode1Num++;
        }
    }

}
