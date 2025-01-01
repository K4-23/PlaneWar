/**
 * 游戏父类
 */
package obj;

import java.awt.*;
import caihui.GameWin;

public class GameObj {
    Image img;
    int x;
    int y;
    int width;
    int height;
    int HP;
    double speed;
    GameWin frame; // 引用当前的游戏窗口




    public GameObj() {
    }

    public GameObj(Image img, int x, int y, int width, int height, double speed, GameWin frame) {
        this.img = img;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.frame = frame;
    }

    public GameObj(Image img, int x, int y, int width, int height, int HP, double speed, GameWin frame) {
        this.img = img;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.HP = HP;
        this.speed = speed;
        this.frame = frame;
    }

    public GameObj(Image img, int x, int y, double speed) {
        this.img = img;
        this.x = x;
        this.y = y;
        this.speed = speed;
    }
    public GameObj(int x, int y){
        this.x = x;
        this.y = y;
    }
    public int getHP() {
        return HP;
    }

    public void setHP(int HP) {
        this.HP = HP;
    }

    public void paintSelf(Graphics g) {
        // 绘制图像，使用当前对象的属性来确定图像的位置和大小
        g.drawImage(img, x, y,null);
    }

    public Rectangle getRec() {
        //获取矩形对象，用于碰撞检测
        return new Rectangle(x, y, width, height);
    }

    public Image getImg() {
        return img;
    }

    public void setImg(Image img) {
        this.img = img;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public GameWin getFrame() {
        return frame;
    }

    public void setFrame(GameWin frame) {
        this.frame = frame;
    }

}
