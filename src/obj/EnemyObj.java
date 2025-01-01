package obj;

import caihui.GameWin;
import utils.*;

import java.awt.*;

// 敌方飞机
public class EnemyObj extends GameObj{
    int maxHP ;
    public EnemyObj(Image img, int x, int y, int width, int height,int HP, double speed, GameWin frame) {
        super(img, x, y, width, height, HP, speed, frame);
        this.maxHP = HP;
    }

    public EnemyObj() {
        super();
    }

    @Override
    public void paintSelf(Graphics g) {
        super.paintSelf(g);
        y += speed;// 敌机向下移动
        // 敌机与我方飞机碰撞检测
        if(this.getRec().intersects(this.frame.planeObj.getRec())){
            PlaneObj.HP -= HP;// 玩家血量减少当前敌机剩余的血量
            ExplodeObj1 explodeObj1 = new ExplodeObj1(this.x-40,this.y-6);// 敌机爆炸
            GameUtils.explodeObj1List.add(explodeObj1);
            GameUtils.removeList.add(explodeObj1);
            this.x = -1700;// 将飞机移出屏幕外
            this.y = -1700;
            GameUtils.removeList.add(this);
        }
        // 玩家子弹击中敌方飞机
        for(BulletObj bulletObj : GameUtils.bulletList1){
            if(this.getRec().intersects(bulletObj.getRec())){
                ExplodeObj explodeObj = new ExplodeObj(this.x-39,this.y-20);// 创建爆炸对象
                GameUtils.explodeObjList.add(explodeObj);
                GameUtils.removeList.add(explodeObj);

                bulletObj.setX(-333);
                bulletObj.setY(333);
                GameUtils.removeList.add(bulletObj);

                HP -= PlaneObj.attack;
                if(HP <= 0){// 敌机被击毁
                    GameWin.score++;// 累加积分
                    GameWin.enemyNum++;// 累加被击毁敌机数量
                    ExplodeObj1 explodeObj1 = new ExplodeObj1(this.x-30,this.y-6);// 敌机爆炸
                    GameUtils.explodeObj1List.add(explodeObj1);
                    GameUtils.removeList.add(explodeObj1);
                    this.x = -1100;
                    this.y = -1100;
                    GameUtils.removeList.add(this);
                }

            }
        }
        if(y > 900){// 敌机超出屏幕，删除敌机
            this.setX(-500);
            this.setY(-500);
            GameUtils.removeList.add(this);
        }
        // 绘制敌方飞机血条
        g.setColor(Color.white);// 绘制白色背景
        g.fillRect(this.x, this.y,this.width,2);
        g.setColor(Color.red);// 血量
        g.fillRect(this.x, this.y,HP*this.width/maxHP ,2);
    }

    @Override
    public Rectangle getRec() {
        return super.getRec();
    }
}
