# 🛩️ PlaneWar

用 **Java 21 + Java2D** 手写的纵版弹幕射击游戏，零第三方运行依赖。

## 🚀 运行

需要 JDK 17+ 和 Maven 3.8+：

```bash
mvn clean package
java -jar target/plane-war-2.0.0.jar
```

IDEA 直接打开根目录跑 `com.k423.planewar.Main` 也可以。

## 🎮 操作

| 操作 | 按键 |
|---|---|
| 移动 | `W` `A` `S` `D` / 方向键 / 鼠标 |
| 精确移动 | 按住 `Shift` |
| 射击 | 自动开火 |
| 炸弹 | `X` 或 `K` |
| 暂停 | `Esc` 或 `P` |

## ✨ 内容

- 三关三个 Boss，血量降到 2/3、1/3 时换阶段
- 五种敌机、五种道具、连击倍率
- 粒子、震屏、顿帧、扫描线、程序化音效
