package utils;

import obj.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// 工具类
public class GameUtils {
    // 背景图片
    public static Image backgroundImage = Toolkit.getDefaultToolkit().getImage("src/image/background.jpg");
    // 敌机图片1
    public static Image enemy1 = Toolkit.getDefaultToolkit().getImage("src/image/enemy/enemy1.png");
    // 敌机图片2
    public static Image enemy2 = Toolkit.getDefaultToolkit().getImage("src/image/enemy/enemy2.png");
    // 敌机图片3
    public static Image enemy3 = Toolkit.getDefaultToolkit().getImage("src/image/enemy/enemy3.png");
    // 玩家飞机图片
    public static Image player = Toolkit.getDefaultToolkit().getImage("src/image/plane1.png");
    // 玩家子弹图片
    public static Image bullet1 = Toolkit.getDefaultToolkit().getImage("src/image/bullet/bullet1-d.png");
    // boss子弹图片
    public static Image bullet2 = Toolkit.getDefaultToolkit().getImage("src/image/bullet/bullet2.png");

    // 所有游戏物体集合
    public static List<GameObj> gameObjList = new ArrayList<>();
    // 删除元素集合
    public static List<GameObj> removeList = new ArrayList<>();
    // 玩家子弹集合
    public static List<BulletObj> bulletList1 = new ArrayList<>();
    // boss子弹集合
    public static List<BossBulletObj> bulletList2 = new ArrayList<>();
    // 敌方飞机集合
    public static List<EnemyObj> enemyObjList = new ArrayList<>();
    // 子弹爆炸效果集合
    public static List<ExplodeObj> explodeObjList = new ArrayList<>();
    // 敌机爆炸效果集合
    public static List<ExplodeObj1> explodeObj1List = new ArrayList<>();
    // 玩家飞机中弹效果集合
    public static List<ShotOBj> shotOBjList = new ArrayList<>();

    // 绘制字符串的工具类
    public static void drawString(Graphics g, String str,Color color ,int x, int y, int size){
        // 绘制字符串
        g.setColor(color);
        g.setFont(new Font("微软雅黑", Font.BOLD, size));
        g.drawString(str, x, y);

    }

}
