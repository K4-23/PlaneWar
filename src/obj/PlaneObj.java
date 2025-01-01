package obj;

import caihui.GameWin;
import utils.GameUtils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// 玩家飞机
public class PlaneObj extends GameObj{

    public static int attack = 4;
    public static int HP = 1000;


    public PlaneObj(Image img, int x, int y, int width, int height, double speed, GameWin frame) {
        super(img, x, y, width, height, speed, frame);
        // 鼠标监听
        this.frame.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                PlaneObj.super.x = e.getX() - width/2;
                PlaneObj.super.y = e.getY() - height/2;
            }
        });
    }

    @Override
    public void paintSelf(Graphics g) {
        super.paintSelf(g);
        // 判断是否与boss相撞
        if(this.frame.bossObj != null && this.getRec().intersects(this.frame.bossObj.getRec())){
            GameWin.state = 2;
        }
        // 判断boss子弹击中玩家
        for (BossBulletObj bossbulletObj : GameUtils.bulletList2){
            if (this.getRec().intersects(bossbulletObj.getRec())){
                // 子弹命中特效
                ShotOBj shotOBj = new ShotOBj(bossbulletObj.x-130,bossbulletObj.y-100);
                GameUtils.shotOBjList.add(shotOBj);
                GameUtils.removeList.add(shotOBj);

                bossbulletObj.setX(-200);
                bossbulletObj.setY(200);
                GameUtils.removeList.add(bossbulletObj);
                PlaneObj.HP--;// 血量减一
            }
        }
        if (HP <= 0){// 生命值为0时，游戏结束
            GameWin.state = 2;
        }
    }

    @Override
    public Image getImg() {
        return img;
    }


}
