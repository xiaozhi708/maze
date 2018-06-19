/**
 * Created by Administrator on 2018/5/13/013.
 */

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Stack;

public class Maze {
    final int X_OF_JFRAME = 150;                                //窗口横坐标
    final int Y_OF_JFRAME = 150;                                //窗口纵坐标
    private JFrame f;                                           //窗口
    private JPanel pf, pm;                                      //pf：文件选择面板，pm：迷宫展示面板
    private JMenuBar bar;                                       //菜单条
    private JMenu fileMenu;                                     //菜单
    private JMenuItem openItem, saveItem;                       //菜单选项：打开文件，保存文件
    private JTextArea ta;                                       //设置文本区域来显示打开的数据
    private JButton run;                                        //开始运行按钮
    private File file;                                          //读取的文件
    private boolean setStart = false;                           //出入口节点一旦设置鼠标点击事件失效
    private boolean setEnd = false;
    private int[][] maze;                                       //迷宫，0代码普通格子，1代表石块，2代表入口，3代表出口
    private JButton[][] jbtn;                                   //代表迷宫格子的按钮组
    private int hang = 0;                                       //文本逐行同步到maze和jbtn的行数;
    private boolean readOver = false;                           //文件是否读取完毕
    /**
     * 算法用到的变量
     */
    private Stack<MazeNode> stack = new Stack<MazeNode>();      // 栈
    private int[][] mark;                                       // 标记路径是否已走过,1代表走过，0代表未走过
    private boolean hasPath =false;                             //是否有从起点到终点的路径
    private int mazeSizeX = 8;                                  //迷宫的行数
    private int mazeSizeY = 8;                                  //迷宫的列数
    private int startX = 0;                                     //起点横坐标
    private int startY = 0;                                     //起点纵坐标
    private int endX = 7;                                       //终点横坐标
    private int endY = 7;                                       //终点纵坐标

    /**
     * 构造函数
     */
    Maze() {
        maze = new int[8][8];
        jbtn = new JButton[8][8];
        pageInit();
    }

    Maze(int[][] maze, JButton[][] jbtn) {
        this.maze = maze;
        this.jbtn = jbtn;
        pageInit();
    }

    /**
     * 方法
     */
    //页面初始化
    public void pageInit() {
        //得到屏幕的大小
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screensize.getWidth();
        int height = (int) screensize.getHeight();

        //窗口起始位置
        int xPosition = X_OF_JFRAME;
        int yPosition = Y_OF_JFRAME;

        //窗口构建
        f = new JFrame("迷宫");
        f.setLayout(new GridLayout(1, 2));
        f.setBounds(xPosition, yPosition, width / 2, height / 2);

        //文件选择面板构建
        pf = new JPanel(new BorderLayout());
        pf.setSize(width / 6, height / 2);

        //菜单栏构建
        bar = new JMenuBar();
        fileMenu = new JMenu("文件");
        openItem = new JMenuItem("打开");
        saveItem = new JMenuItem("保存");
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        bar.add(fileMenu);

        //文本显示域构建
        ta = new JTextArea();

        //运行按钮构建
        run = new JButton("运行");

        pf.add(bar, BorderLayout.NORTH);
        pf.add(ta, BorderLayout.CENTER);
        pf.add(run, BorderLayout.SOUTH);

        //迷宫显示面板设置
        pm = new JPanel(new GridLayout(8, 8));
        pm.setSize(width / 3, height / 2 - 300);
        //初始化迷宫
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                jbtn[i][j] = new JButton();
                if (Math.random() > 0.80) {                 //设置石块
                    jbtn[i][j].setBackground(Color.gray);
                    maze[i][j] = 1;
                } else {
                    jbtn[i][j].setBackground(Color.green);
                }
                pm.add(jbtn[i][j]);
            }
        }

        f.add(pm);
        f.add(pf);
        f.setVisible(true);

        //启动监听
        myEvent();
    }

    //将文本中的内容同步到jbtn和maze中(逐行)
    private void ds(String line) {
        String[] temp = new String[8];
        temp = line.split("\\s+");
        for (int i = 0; i < 8; i++) {
            System.out.print("读取到的数据：" + temp[i]);
            maze[hang][i] = Integer.parseInt(temp[i]);
            if (Integer.parseInt(temp[i]) == 0) {
                jbtn[hang][i].setBackground(Color.green);
            } else if (Integer.parseInt(temp[i]) == 1) {
                jbtn[hang][i].setBackground(Color.gray);
            } else if (Integer.parseInt(temp[i]) == 2) {
                setStart = true;//设置起点
                jbtn[hang][i].setBackground(Color.blue);
            } else if (Integer.parseInt(temp[i]) == 3) {
                setEnd = true;//设置终点
                jbtn[hang][i].setBackground(Color.red);
            }
        }
        hang++;
        System.out.println();

    }

    //将文本中的内容同步到jbtn和maze中(整个文本)
    private void dsall(String text) {
        String[] h = new String[8];
        String[] temp = new String[8];
        h = text.split("\r\n+");
        for (int i = 0; i < 8; i++) {
            temp = h[i].split("\\s+");
            for (int j = 0; j < 8; j++) {
                maze[i][j] = Integer.parseInt(temp[j]);
                if (Integer.parseInt(temp[j]) == 0) {
                    jbtn[i][j].setBackground(Color.green);
                } else if (Integer.parseInt(temp[j]) == 1) {
                    jbtn[i][j].setBackground(Color.gray);
                } else if (Integer.parseInt(temp[j]) == 2) {
                    jbtn[i][j].setBackground(Color.blue);
                } else if (Integer.parseInt(temp[j]) == 3) {
                    jbtn[i][j].setBackground(Color.red);
                }
            }
        }

    }

    //监听事件
    public void myEvent() {
        //设置保存文件的功能
        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BufferedWriter bufw = new BufferedWriter(new FileWriter(file));

                    String text = ta.getText();

                    bufw.write(text);

                    bufw.close();
                } catch (IOException ex) {
                    throw new RuntimeException("文件保存失败！");
                }
            }
        });

        //设置打开文件功能
        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                jfc.showDialog(new JLabel(), "选择");
                file = jfc.getSelectedFile();
                if (file.isDirectory()) {
                    System.out.println("文件夹:" + file.getAbsolutePath());
                } else if (file.isFile()) {
                    System.out.println("文件:" + file.getAbsolutePath());
                }
                System.out.println(jfc.getSelectedFile().getName());

                //如果打开路径 或 目录为空 则返回空
                if (file == null)
                    return;

                ta.setText("");//清空文本

                try {
                    BufferedReader bufr = new BufferedReader(new FileReader(file));

                    String line = null;

                    while ((line = bufr.readLine()) != null) {
                        ta.append(line + "\r\n");
                        ds(line);//同步到迷宫
                    }
                    bufr.close();
                    readOver = true;
                } catch (IOException ex) {
                    throw new RuntimeException("文件读取失败！");
                }


            }
        });

        //给文本域设置监听器，一旦发生改变同步到迷宫
        ta.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (readOver) {
                    //hang=0;
                    dsall(ta.getText());
                    //System.out.println("Text Inserted:"+ta.getText());
                }

            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                //System.out.println("Text Removed:"+ta.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // System.out.println("Attribute Changed"+e);
            }
        });

        //给64个按钮绑定点击事件
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                jbtn[i][j].addMouseListener(new MouseAdapter() {

                    private int ini;//接受从外面传进来的i和j
                    private int inj;

                    public MouseAdapter accept(int i, int j) {
                        this.ini = i;
                        this.inj = j;
                        return this;
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == e.BUTTON1) {//点击鼠标左键
                            JButton source = (JButton) e.getSource();
                            if (!setStart) {
                                if(source.getBackground()==Color.green){//非石头节点才可以设置为起始节点
                                    source.setBackground(Color.blue);//起点为蓝色
                                    setStart = true;
                                    maze[ini][inj] = 2;
                                }
                            }
                        } else if (e.getButton() == e.BUTTON3) {//点击鼠标右键
                            JButton source = (JButton) e.getSource();
                            if (!setEnd) {
                                if(source.getBackground()==Color.green){//非石头节点才可以设置为结束节点
                                    source.setBackground(Color.red);//终点为红色
                                    setEnd = true;
                                    maze[ini][inj] = 3;
                                }
                            }
                        }
                    }
                }.accept(i, j));
            }
        }

        //运行按钮监听事件
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prepareForArithmetic();     //为算法准备数据
                //检查开始节点和结束节点和合理性
                MazeNode startMazeNode = new MazeNode(startX, startY, 1);//开始节点
                MazeNode endMazeNode =new MazeNode(endX,endY,1);//结束节点
                if(checkLegalityOfStartAndEnd(startMazeNode)&&checkLegalityOfStartAndEnd(endMazeNode)){//如果开始节点和结束节点都合理
                    process();                  //开始运行算法
                    System.out.println("是否有从起点到终点的路径："+hasPath);
                    if(!hasPath){              //没有从开始节点到结束节点的路径
                        JOptionPane.showMessageDialog(null, "没有从开始节点到结束节点的路径！", "结果提示", JOptionPane.ERROR_MESSAGE);
                    }
                }else{//开始节点或结束节点不合理（无路可走，四周都是石头或边界）
                    setStart=false;
                    setEnd=false;
                    maze[startX][startY]=0;
                    maze[endX][endY]=0;
                    jbtn[startX][startY].setBackground(Color.green);
                    jbtn[endX][endY].setBackground(Color.green);
                    JOptionPane.showMessageDialog(null, "开始节点或结束节点不合理！", "出错提示", JOptionPane.ERROR_MESSAGE);
                }

            }
        });


        //窗口关闭功能
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                hang = 0;//退出之前将hang置0，为接下来再一次导入做准备
                System.exit(0);
            }
        });
    }

    /**
     * 跟算法有关的方法
     */
    // 栈中的结点
    class MazeNode {
        int x;
        int y;
        int direction;

        public MazeNode(int x, int y, int dir) {
            this.x = x;
            this.y = y;
            this.direction = dir;
        }
    }

    //得到开始节点和结束节点的位置
    public void getStartAndEndPosition() {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                if (maze[i][j] == 2) {//开始节点
                    startX = i;
                    startY = j;
                } else if (maze[i][j] == 3) {//结束节点
                    endX = i;
                    endY = j;
                }
            }
        }
    }

    //得到迷宫的行数和列数
    public void getSizeOfMaze() {
        mazeSizeX = maze.length;
        mazeSizeY = maze[0].length;
    }

    //为算法运行做准备
    public void prepareForArithmetic() {
        getStartAndEndPosition();
        getSizeOfMaze();
        mark = new int[mazeSizeX][mazeSizeY];
    }

    //初始化标志函数（1表示位置已经走过，0表示位置还未走过）
    private void initMark() {
        for (int i = 0; i < mazeSizeX; i++) {
            for (int j = 0; j < mazeSizeY; j++) {
                mark[i][j] = 0;
            }
        }
    }

    //检查走的迷宫节点的合法性(防止下标越界，当前节点不是石块节点且未走过)
    public boolean checkLegalityOfMazeNode(MazeNode mazeNode) {
        if (mazeNode.x >= 0 && mazeNode.x < mazeSizeX && mazeNode.y >= 0 && mazeNode.y < mazeSizeY
                && (maze[mazeNode.x][mazeNode.y] != 1) && mark[mazeNode.x][mazeNode.y] == 0) {
            return true;
        } else {
            return false;
        }

    }


    //检查开始节点和结束节点是否合理
    public boolean checkLegalityOfStartAndEnd(MazeNode mazeNode) {
        boolean legality =false;
        if (mazeNode.x >= 0 && mazeNode.x < mazeSizeX && mazeNode.y >= 0 && mazeNode.y < mazeSizeY
                && (maze[mazeNode.x][mazeNode.y] != 1)) {
            if((mazeNode.x-1)>=0&&maze[mazeNode.x-1][mazeNode.y]!=1){
                legality=true;
            }
            if(mazeNode.x+1<mazeSizeX&&maze[mazeNode.x+1][mazeNode.y]!=1){
                legality=true;
            }
            if(mazeNode.y-1>=0&&maze[mazeNode.x][mazeNode.y-1]!=1){
                legality=true;
            }
            if(mazeNode.y+1<mazeSizeX&&maze[mazeNode.x][mazeNode.y+1]!=1){
                legality=true;
            }
            return legality;
        } else {
            return legality;
        }

    }

    // 下一个位置，从右开始，顺时针
    public MazeNode nextPos(MazeNode mazeNode) {
        MazeNode newMazeNode = new MazeNode(mazeNode.x, mazeNode.y, mazeNode.direction);
        switch (mazeNode.direction) {
            case 1:
                if (newMazeNode.y + 1 < mazeSizeY && maze[newMazeNode.x][newMazeNode.y + 1] != 1 && mark[newMazeNode.x][newMazeNode.y + 1] != 1) {
                    newMazeNode.y += 1;
                    newMazeNode.direction = 1;
                    stack.push(newMazeNode);
                } else {
                    newMazeNode = stack.pop();//先弹出去，换个方向再进来
                    newMazeNode.direction++;
                    stack.push(newMazeNode);
                    mark[newMazeNode.x][newMazeNode.y] = 0;//回退到之前位置，重新判定，入栈，故mark标记置0
                }
                break;
            case 2:
                if (newMazeNode.x + 1 < mazeSizeX && maze[newMazeNode.x + 1][newMazeNode.y] != 1 && mark[newMazeNode.x + 1][newMazeNode.y] != 1) {
                    newMazeNode.x += 1;
                    newMazeNode.direction = 1;
                    stack.push(newMazeNode);
                } else {
                    newMazeNode = stack.pop();//先弹出去，换个方向再进来
                    newMazeNode.direction++;
                    stack.push(newMazeNode);
                    mark[newMazeNode.x][newMazeNode.y] = 0;//回退到之前位置，重新判定，入栈，故mark标记置0
                }
                break;
            case 3:
                if (newMazeNode.y - 1 >= 0 && maze[newMazeNode.x][newMazeNode.y - 1] != 1 && mark[newMazeNode.x][newMazeNode.y - 1] != 1) {
                    newMazeNode.y -= 1;
                    newMazeNode.direction = 1;
                    stack.push(newMazeNode);
                } else {
                    newMazeNode = stack.pop();//先弹出去，换个方向再进来
                    newMazeNode.direction++;
                    stack.push(newMazeNode);
                    mark[newMazeNode.x][newMazeNode.y] = 0;//回退到之前位置，重新判定，入栈，故mark标记置0
                }
                break;
            case 4:
                if (newMazeNode.x - 1 >= 0 && maze[newMazeNode.x - 1][newMazeNode.y] != 1 && mark[newMazeNode.x - 1][newMazeNode.y] != 1) {
                    newMazeNode.x -= 1;
                    newMazeNode.direction = 1;
                    stack.push(newMazeNode);
                } else {
                    while (newMazeNode.direction == 4&&!stack.isEmpty()) {
                        newMazeNode = stack.pop();
                        mark[newMazeNode.x][newMazeNode.y] = 0;
                    }
                    if(newMazeNode.direction<4){//如果栈中还有可拓展节点，如果没有的话返回原始节点
                        newMazeNode.direction++;
                        stack.push(newMazeNode);
                        mark[newMazeNode.x][newMazeNode.y] = 0;//回退到之前位置，重新判定，入栈，故mark标记置0
                    }

                }
                break;
            default:
                break;
        }
        return newMazeNode;
    }

    //迷宫算法的主体函数
    public void process() {
        initMark();
        MazeNode mazeNode = new MazeNode(startX, startY, 1);
        stack.push(mazeNode);
        do {
            // 此路径可走 maze:0代表可走，1代表不可走,2代表起点，3代表终点
            System.out.println("当前x：" + mazeNode.x + ",当前y：" + mazeNode.y + ",当前方向：" + mazeNode.direction);
            if (checkLegalityOfMazeNode(stack.peek())) {//判断当前节点是否合法（节点下标无越界，节点可走且有可走临近点）
                mark[mazeNode.x][mazeNode.y] = 1;//标级节点已走过
                jbtn[mazeNode.x][mazeNode.y].setBackground(Color.black);//将当前格子背景色设为黑色
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                //stack.push(mazeNode);
                // 已到终点
                if (mazeNode.x == endX && mazeNode.y == endY) {
                    hasPath=true;                                                   //有从起点到终点的路径
                    MazeNode node;
                    while (!stack.isEmpty()) {
                        node = stack.pop();
                        if (node.x == startX && node.y == startY) {                  //起点设为蓝色
                            jbtn[node.x][node.y].setBackground(Color.blue);
                        } else if (node.x == endX && node.y == endY) {              //终点设为红色
                            jbtn[node.x][node.y].setBackground(Color.red);
                        } else {                                                    //除了起点和终点，路径上的其他节点都变为白色
                            jbtn[node.x][node.y].setBackground(Color.white);
                        }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
                //获取下一个节点
                mazeNode = nextPos(mazeNode);
            }
            // 走不通
            else {
                if (!stack.isEmpty()) {
                    stack.pop();
                }
                System.out.println("进入了else部分");
            }
        }
        while (!stack.isEmpty());

    }

    //打印出迷宫
    public void drawMaze() {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                System.out.print(maze[i][j]);
            }
            System.out.print("\n");
        }
        System.out.print("\n");
    }

}



