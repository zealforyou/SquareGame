package com.zz.squarebrick.particle.factory;

import android.graphics.Rect;

import com.zz.squarebrick.game.Square;
import com.zz.squarebrick.particle.particle.Particle;
import com.zz.squarebrick.particle.particle.VerticalAscentParticle;

import java.util.List;


/**
 * Created by Administrator on 2015/11/29 0029.
 */
public class VerticalAscentFactory extends ParticleFactory {
    public static final int PART_WH = 8; //默认小球宽高

    public Particle[][] generateParticles(List<Square.Cell> cells, Rect bound, int mRow, int cellWidth) {
        int w = bound.width();
        int h = bound.height();

        int partW_Count = w / PART_WH; //横向个数
        int partH_Count = h / PART_WH; //竖向个数

        int bitmap_part_w = w / partW_Count;
        int bitmap_part_h = h / partH_Count;

        Particle[][] particles = new Particle[partH_Count][partW_Count];
        for (int row = 0; row < partH_Count; row++) { //行
            for (int column = 0; column < partW_Count; column++) { //列
                //取得当前粒子所在位置的颜色
                int cellCol = column * PART_WH / cellWidth;

                float x = bound.left + VerticalAscentFactory.PART_WH * column;
                float y = bound.top + VerticalAscentFactory.PART_WH * row;
                particles[row][column] = new VerticalAscentParticle(getColor(cells, cellCol, mRow), x, y, bound);
            }
        }

        return particles;
    }
}
