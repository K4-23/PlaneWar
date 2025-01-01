package obj;

import caihui.GameWin;
import utils.GameUtils;

import java.awt.*;

public class BossObj extends GameObj{
    int HP = 2000;// Boss当前血量
    int maxHP = HP;// Boss最大血量
    public BossObj() {
        super();
    }

    public BossObj(Image img, int x, int y, int width, int height, double speed, GameWin frame) {
        super(img, x, y, width, height, speed, frame);
    }

    @Override
    public Rectangle getRec() {
        return super.getRec();
    }

    @Override
    public void paintSelf(Graphics g) {
        super.paintSelf(g);
        x += speed;
        if (x >= 650 || x <= -10){
            speed = -speed;// 速度正负交替实现左右移动
        }
        if (y <= 100){
            y += 1;// Boss缓慢向下移动到固定y坐标
        }

        for (BulletObj bulletObj : GameUtils.bulletList1){
            if (this.getRec().intersects(bulletObj.getRec())){// 玩家子弹击中boss
                ExplodeObj explodeObj = new ExplodeObj(bulletObj.x-39, bulletObj.y-62);// 创建爆炸对象
                GameUtils.explodeObjList.add(explodeObj);
                GameUtils.removeList.add(explodeObj);

                bulletObj.setX(-200);
                bulletObj.setY(200);
                GameUtils.removeList.add(bulletObj);
                HP -= PlaneObj.attack;
            }
            if (HP <= 0){ // boss被击杀
                GameWin.state = 3;
            }
        }
        // 绘制boss血条背景
        g.setColor(Color.white);
        g.fillRect(200,45,300,5);
        // 绘制boss血条
        g.setColor(Color.red);
        g.fillRect(200,45,HP*300/maxHP,5);
    }
}
