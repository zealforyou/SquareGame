package com.zz.squarebrick.particle.factory;

import android.graphics.Rect;

import com.zz.squarebrick.game.Square;
import com.zz.squarebrick.particle.particle.Particle;

import java.util.List;


/**
 * Created by Administrator on 2015/11/29 0029.
 */
public abstract class ParticleFactory {
    public abstract Particle[][] generateParticles(List<Square.Cell> cells, Rect bound, int mRow, int cellWidth);
    public int getColor(List<Square.Cell> cells, int cellCol, int cellRow) {
        for (int i = 0; i < cells.size(); i++) {
            Square.Cell cell = cells.get(i);
            if (cell.getRow() == cellRow && cell.getCol() == cellCol) {
                return cell.color;
            }
        }
        return 0x0000;
    }
}
