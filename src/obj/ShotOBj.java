// boss子弹命中特效
package obj;

import java.awt.*;

public class ShotOBj extends GameObj{

    static Image[] shotImage = new Image[10];
    int shotNum = 0;
    static {
        for (int i = 0; i < shotImage.length; i++) {
            shotImage[i] = Toolkit.getDefaultToolkit().getImage("src/image/shot/" + (i+1) + ".png");
        }
    }

    public ShotOBj(int x, int y) {
        super(x, y);
    }

    @Override
    public void paintSelf(Graphics g) {
        if(shotNum < shotImage.length){
            img = shotImage[shotNum];
            super.paintSelf(g);
            shotNum++;
        }
    }
}
