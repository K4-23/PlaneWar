/**
 * 项目：飞机大战小游戏
 * 完成时间：2024.12.30
 * Author：~~~~
 */
package caihui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import obj.*;
import utils.GameUtils;

public class GameWin extends JFrame {
    // 游戏状态,初始化状态为0
    public static int state = 0;
    // 游戏得分
    public static int score = 0;
    // 敌机数量
    public static int enemyNum = 0;

    // 屏幕缓存解决闪烁问题
    Image offScreenImage = null;
    // 游戏重绘次数
    int count = 0;
    // 背景对象
    BgObj bgobj = new BgObj(GameUtils.backgroundImage,0,-380,1);
    // 玩家飞机对象
    public PlaneObj planeObj = new PlaneObj(GameUtils.player, 300, 600, 63, 48, 0, this);
    // Boss对象
    public BossObj bossObj = null;

    public void launch() {
        // 设置窗口属性
        this.setVisible(true);
        this.setSize(720, 900);
        this.setLocationRelativeTo(null);
        this.setTitle("飞机大战");
        //
        GameUtils.gameObjList.add(bgobj);
        GameUtils.gameObjList.add(planeObj);

        // 监听鼠标点击事件, 点击屏幕开始游戏
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == 1 && getState() == 0) {
                    state = 1; // 切换到游戏状态
                    repaint(); // 重绘窗口
                }
            }
        });
        // 键盘监听事件
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == 32) {// 空格键暂停
                    if (state == 1) {
                        state = 4;
                    } else if (state == 4) {
                        state = 1;
                    }
                }
            }
        });
        while (true) {// 游戏循环
            if (state == 1) { // 游戏状态为1时才不断绘制画面
                creatObj();
                repaint();//不断重绘窗口
            }
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    // 根据游戏状态重绘窗口
    public void paint(Graphics g) {
        if (offScreenImage == null) {
            offScreenImage = this.createImage(720, 900);//屏幕外的缓冲区
        }
        Graphics gOffScreen = offScreenImage.getGraphics();
        gOffScreen.clearRect(0, 0, 720, 900);// 清除旧内容，防止画面残留
        // 游戏开始界面
        if (state == 0) {
            gOffScreen.drawImage(utils.GameUtils.backgroundImage, 0, 0, this);
            gOffScreen.drawImage(utils.GameUtils.enemy3, 300, 180, this);
            GameUtils.drawString(gOffScreen, "点击开始游戏" , Color.GREEN , 245,390, 40);
        }
        // 游戏进行中
        if (state == 1) {
            GameUtils.gameObjList.addAll(GameUtils.explodeObjList);
            GameUtils.gameObjList.addAll(GameUtils.shotOBjList);
            GameUtils.gameObjList.addAll(GameUtils.explodeObj1List);
            for (int i = 0; i < GameUtils.gameObjList.size(); i++){
                GameUtils.gameObjList.get(i).paintSelf(gOffScreen);
            }
            GameUtils.gameObjList.removeAll(GameUtils.removeList);// 清空removeList
        }
        // 游戏失败
        if (state == 2) {
            GameUtils.drawString(gOffScreen,"游戏结束", Color.RED, 260,290, 45);
        }
        // 游戏通关
        if (state == 3) {
            GameUtils.drawString(gOffScreen, "游戏通关", Color.green, 260,290, 40);
        }
        // 游戏暂停
        if (state == 4) {// 此处有Bug，暂停无法显示提示信息，交给有缘人了~~~
            GameUtils.drawString(gOffScreen, "游戏暂停", Color.green, 260,290, 40);
        }
        GameUtils.drawString(gOffScreen, " 积分:   "+score, Color.green, 10, 60, 20);
        GameUtils.drawString(gOffScreen, " 生命值: " + PlaneObj.HP, Color.RED,10, 90, 20);
        GameUtils.drawString(gOffScreen, " 攻击力: " + PlaneObj.attack, Color.white,10, 120, 20);
        g.drawImage(offScreenImage, 0, 0, null);
        count++;
    }
    void creatObj() {
        // 添加玩家子弹对象
        if (count % 5 == 0) {// 每刷新5次发射一颗子弹
            GameUtils.bulletList1.add(new BulletObj(GameUtils.bullet1, planeObj.getX()+7, planeObj.getY()-38, 22,53, 5, this));
            GameUtils.gameObjList.add(GameUtils.bulletList1.get(GameUtils.bulletList1.size()-1));
        }
        // 添加Boss, 当被击落20架敌机时刷新Boss
        if (enemyNum > 20 && bossObj == null) {
            bossObj = new BossObj(GameUtils.enemy3, 350, -40, 125, 91, 2.5, this);
            GameUtils.gameObjList.add(bossObj);
        }
        // 添加Boss子弹对象
        if (count % 30 == 0 && bossObj != null) {
            GameUtils.bulletList2.add(new BossBulletObj(GameUtils.bullet2, bossObj.getX()+44, bossObj.getY()+80, 28,27, 3, this));
            GameUtils.gameObjList.add(GameUtils.bulletList2.get(GameUtils.bulletList2.size()-1));
        }
        // 添加敌机对象
        if (count % 10 == 0) { // 随机生成小飞机
            Random random = new Random(); // 创建Random对象
            Image enemyImage;
            int width;
            int height;
            int HP;
            double speed;
            int randomValue = random.nextInt(10); // 生成0到9之间的随机数
            if (randomValue < 7) { // 0到6生成enemy1
                enemyImage = GameUtils.enemy1;
                width = 34;
                height = 26;
                HP = 1;
                speed = 4;
            } else { // 7到9生成enemy2
                enemyImage = GameUtils.enemy2;
                width = 46;
                height = 59;
                HP = 10;
                speed = 2;
            }
            EnemyObj enemy = new EnemyObj(enemyImage, (int) (Math.random() * 14) * 50, 0, width, height, HP, speed,this);
            GameUtils.enemyObjList.add(enemy);
            GameUtils.gameObjList.add(enemy);
        }

    }
    public static void main(String[] args) {
        GameWin gameWin = new GameWin();
        gameWin.launch();
    }
}
