package com.zz.squarebrick.game;

import java.util.List;

/**
 * Created by zhuo.zhang on 2018/10/19.
 */

public class Square {
    enum Type {
        TYPE_L, TYPE_L1, TYPE_T, TYPE_I, TYPE_O, TYPE_N, TYPE_N1
    }

    public static final int colors[] = {0xffe03636, 0xffedd0be, 0xffff534d, 0xff25c6fc};
    public static final int[][] TYPE_L = {{-2, 0}, {-2, 1}, {-1, 1}, {0, 1}};//index 2
    public static final int[][] TYPE_L1 = {{-2, 1}, {-2, 0}, {-1, 0}, {0, 0}};//index 2
    public static final int[][] TYPE_T = {{-1, 1}, {0, 0}, {0, 1}, {0, 2}};//index 2
    public static final int[][] TYPE_I = {{-1, 0}, {-1, 1}, {-1, 2}, {-1, 3}};
    public static final int[][] TYPE_O = {{-1, 0}, {-1, 1}, {0, 0}, {0, 1}};
    public static final int[][] TYPE_N = {{-1, 1}, {0, 1}, {0, 0}, {1, 0}};//1
    public static final int[][] TYPE_N1 = {{-1, 0}, {0, 0}, {0, 1}, {1, 1}};//1

    public Type type;
    public int[][] cells;
    public int centerIndex;
    public int color;
    public IndexBound indexBound = new IndexBound();

    public static Square generate(int columns) {
        int randomIndex = (int) (Math.random() * Type.values().length - 1);
        Type type = Type.TYPE_L;
        int[][] base = TYPE_L;
        int centerIndex = 0;
        switch (randomIndex) {
            case 0:
                type = Type.TYPE_L;
                base = TYPE_L;
                centerIndex = 2;
                break;
            case 1:
                type = Type.TYPE_L1;
                base = TYPE_L1;
                centerIndex = 2;
                break;
            case 2:
                type = Type.TYPE_T;
                base = TYPE_T;
                centerIndex = 2;
                break;
            case 3:
                type = Type.TYPE_I;
                base = TYPE_I;
                centerIndex = 2;
                break;
            case 4:
                type = Type.TYPE_O;
                base = TYPE_O;
                break;
            case 5:
                type = Type.TYPE_N;
                base = TYPE_N;
                centerIndex = 1;
                break;
            case 6:
                type = Type.TYPE_N1;
                base = TYPE_N1;
                centerIndex = 1;
                break;
        }
        int cells[][] = new int[4][2];
        for (int i = 0; i < cells.length; i++) {
            cells[i][0] = base[i][0];
            cells[i][1] = base[i][1] + columns / 2 - 2;
        }
        Square square = new Square();
        square.color = colors[(int) (Math.random() * colors.length)];
        square.cells = cells;
        square.centerIndex = centerIndex;
        square.type = type;
        square.refreshBound();
        return square;
    }

    //获取边界索引
    public void refreshBound() {
        int left = cells[0][1];
        int right = cells[0][1];
        int top = cells[0][0];
        int bottom = cells[0][0];
        int leftIndex = 0;
        int rightIndex = 0;
        int topIndex = 0;
        int bottomIndex = 0;
        for (int i = 0; i < cells.length; i++) {
            int[] point = cells[i];
            if (point[1] < left) {
                left = point[1];
                leftIndex = i;
            }
            if (point[1] > right) {
                right = point[1];
                rightIndex = i;
            }
            if (point[0] < top) {
                top = point[0];
                topIndex = i;
            }
            if (point[0] > bottom) {
                bottom = point[0];
                bottomIndex = i;
            }
        }
        indexBound.leftIndex = leftIndex;
        indexBound.rightIndex = rightIndex;
        indexBound.topIndex = topIndex;
        indexBound.bottomIndex = bottomIndex;
    }

    public int[] getBottomCell() {
        return cells[indexBound.bottomIndex];
    }

    //检查下边界 碰撞则返回true
    public boolean checkDown(Cell cell) {
        for (int i = 0; i < cells.length; i++) {
            int[] first = cells[i];
            if (first[0] + 1 == cell.getRow() && first[1] == cell.getCol()) {
                return true;
            }
        }
        return false;
    }

    public boolean canMoveDown(List<Cell> dst, int rows) {
        for (int i = 0; i < dst.size(); i++) {
            Cell cell = dst.get(i);
            if (checkDown(cell)) {
                return false;
            }
        }
        return cells[indexBound.bottomIndex][0] != rows - 1;
    }

    public int[][] canRotate(int deg, List<Cell> dst, int cols, int rows) {

        if (type == Type.TYPE_O) return cells;
        if (!canMoveLeft(dst) && !canMoveRight(dst, cols)) {
            return null;
        }
        double d = Math.PI / 180 * deg;
        int temp[][] = new int[4][2];
        for (int i = 0; i < cells.length; i++) {
            int[] p = cells[i];
            double x = p[0] - cells[centerIndex][0];
            double y = p[1] - cells[centerIndex][1];
            double nx = x * Math.cos(d) - y * Math.sin(d);
            double ny = y * Math.cos(d) + x * Math.sin(d);

            temp[i][0] = (int) Math.round(nx + cells[centerIndex][0]);
            temp[i][1] = (int) Math.round(ny + cells[centerIndex][1]);
        }
        int leftIndex = 0;
        int rightIndex = 0;
        int bottomIndex = 0;
        int left = temp[0][1];
        int right = temp[0][1];
        int bottom = temp[0][0];
        for (int i = 0; i < temp.length; i++) {
            int[] point = temp[i];
            if (point[1] < left) {
                left = point[1];
                leftIndex = i;
            }
            if (point[1] > right) {
                right = point[1];
                rightIndex = i;
            }
            if (point[0] > bottom) {
                bottom = point[0];
                bottomIndex = i;
            }
        }
        //碰到底部
        if (bottom > rows - 1) {
            return null;
        }

        //出界归位
        if (left < 0) {
            for (int i = 0; i < temp.length; i++) {
                temp[i][1] += Math.abs(left);
            }
        } else if (right > cols - 1) {
            for (int i = 0; i < temp.length; i++) {
                temp[i][1] -= Math.abs(right - cols + 1);
            }
        }

        //碰到其他方块
        if (dst.size() > 0) {
            for (int i = 0; i < dst.size(); i++) {
                Cell cell = dst.get(i);
                if (checkRoateWithCell(temp, cell))
                    return null;
            }
        }
        return temp;
    }

    private boolean checkRoateWithCell(int[][] cells, Cell cell) {
        for (int i = 0; i < cells.length; i++) {
            int[] first = cells[i];
            if (first[0] == cell.getRow() && first[1] == cell.getCol()) {
                return true;
            }
        }
        return false;
    }

    //逆时针旋转
    public void rotate(int deg, int[][] ints) {
        if (type == Type.TYPE_O) return;
//        double d = Math.PI / 180 * deg;
//        for (int i = 0; i < cells.length; i++) {
//            int[] p = cells[i];
//            double x = p[0] - cells[centerIndex][0];
//            double y = p[1] - cells[centerIndex][1];
//            double nx = x * Math.cos(d) - y * Math.sin(d);
//            double ny = y * Math.cos(d) + x * Math.sin(d);
//            p[0] = (int) (nx + cells[centerIndex][0]);
//            p[1] = (int) (ny + cells[centerIndex][1]);
//        }
        if (ints != null) {
            cells = ints;
            refreshBound();
        }
    }

    public static class Cell {
        int row;
        int col;
        public int color;

        public Cell(int row, int col, int color) {
            this.row = row;
            this.col = col;
            this.color = color;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getCol() {
            return col;
        }

        public void setCol(int col) {
            this.col = col;
        }
    }

    public static class IndexBound {
        public int leftIndex;
        public int rightIndex;
        public int topIndex;
        public int bottomIndex;
    }

    public void move() {
        for (int i = 0; i < cells.length; i++) {
            int[] cell = cells[i];
            cell[0] = cell[0] + 1;
        }
    }

    //检查左边界 碰撞则返回true
    private boolean checkLeft(int[][] cells, Cell cell) {
        for (int i = 0; i < cells.length; i++) {
            int[] first = cells[i];
            if (first[0] == cell.getRow() && first[1] - 1 == cell.getCol()) {
                return true;
            }
        }
        return false;
    }

    public boolean canMoveLeft(List<Cell> dst) {
        for (int i = 0; i < dst.size(); i++) {
            Cell cell = dst.get(i);
            if (checkLeft(cells, cell)) {
                return false;
            }
        }
        return cells[indexBound.leftIndex][1] > 0;
    }

    public void moveLeft() {
        for (int i = 0; i < cells.length; i++) {
            int[] cell = cells[i];
            cell[1] = cell[1] - 1;
        }
    }

    //检查右边界 碰撞则返回true
    private boolean checkRight(int[][] cells, Cell cell, int cols) {
        for (int i = 0; i < cells.length; i++) {
            int[] first = cells[i];
            if (first[0] == cell.getRow() && first[1] + 1 == cell.getCol()) {
                return true;
            }
        }
        return false;
    }

    public boolean canMoveRight(List<Cell> dst, int cols) {
        for (int i = 0; i < dst.size(); i++) {
            Cell cell = dst.get(i);
            if (checkRight(cells, cell, cols)) {
                return false;
            }
        }
        return cells[indexBound.rightIndex][1] < cols - 1;
    }

    public void moveRight() {
        for (int i = 0; i < cells.length; i++) {
            int[] cell = cells[i];
            cell[1] = cell[1] + 1;
        }
    }
}
