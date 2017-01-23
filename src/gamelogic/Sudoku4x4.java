/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import java.time.LocalTime;
import java.util.Random;
import javax.print.attribute.standard.DateTimeAtCompleted;

/**
 *
 * @author Dan Andersson
 */
public class Sudoku4x4
{
    private int cell[][] = new int[4][4];
    private boolean locked[][] = new boolean[4][4];
    
    private void checkBoundary(int v, int min, int max) throws IndexOutOfBoundsException
    {
        if( v < min || v > max) throw new IndexOutOfBoundsException();
    }
    
    private void checkValue(int v) throws Exception
    {
        if( v < 1 || v > 9) throw new Exception("Value needs to be 1-9.\nFor clearing use the clear(int, int, ..) methods");
    }
    
    public Sudoku4x4() 
    {
        this.generateNewChallenge(LocalTime.now().toNanoOfDay());
    }
    
    public Sudoku4x4(long seed)
    {
        this.generateNewChallenge(seed);        
    }
    
    public boolean isLocked(int row, int col)
    {
        return this.locked[row][col];
    }
    
    public int get(int row, int col)
    {
        this.checkBoundary(row, 0, 3);
        this.checkBoundary(col, 0, 3);

        return this.cell[row][col];
    }
        
    public void set(int row, int col, int value) throws Exception
    {
        this.checkBoundary(row, 0, 3);
        this.checkBoundary(col, 0, 3);
        this.checkValue(value);

        this.cell[row][col] = value;
    }
        
    public void clear(int row, int col)
    {
        this.checkBoundary(row, 0, 3);
        this.checkBoundary(col, 0, 3);

        this.cell[row][col] = 0;
    }
    
    public void clear(int subField_Row, int subField_Col, int row, int col)
    {
        this.checkBoundary(subField_Row, 0, 1);
        this.checkBoundary(subField_Col, 0, 1);
        this.checkBoundary(col, 0, 1);
        this.checkBoundary(row, 0, 1);

        this.cell[(subField_Row * 2) + row][(subField_Col * 2) + col] = 0;   
    }

    // returns the subField row* or col* index. *Depends on input context 
    private static int subField(int rowOrCol)
    {
        return rowOrCol / 2;
    }
    
    private static void paintValueLock(int row, int col, boolean valueCanvas[][])
    {
        { // lock all in subfield
            int sfRow = Sudoku4x4.subField(row) * 2,
                sfCol = Sudoku4x4.subField(col) * 2;

            valueCanvas[sfRow][sfCol] = false;
            valueCanvas[sfRow][sfCol+1] = false;
            valueCanvas[sfRow+1][sfCol] = false;
            valueCanvas[sfRow+1][sfCol+1] = false;
        }
        
        // lock all in row
        for(int i = 0; i < 4; ++i)
            valueCanvas[i][col] = false;
        
        // lock all in col
        for(int i = 0; i < 4; ++i)
            valueCanvas[col][i] = false;
    }
    
    private static int[][] generateSolution(Random ran)
    {
        int solution[][] = new int[4][4];
        boolean undecided[][][] = new boolean[9][4][4];
        // Every cell value starts as all the values (think uncertaincy principle)
        for(boolean v[][] : undecided) for(boolean r[] : v) for(boolean b : r)
            b = true;
        
        int count = 0; // count the number of values we have generated. We stop at 4x4 = 32
        int val, row, col;
        while(count < 32)
        {
            val = ran.nextInt(9);
            row = ran.nextInt(4);
            col = ran.nextInt(4);
            
            if(undecided[val][row][col])
            {
                solution[row][col] = val + 1;
                Sudoku4x4.paintValueLock(row, col, undecided[val]);
                ++count;
            }
        }
        return solution;
    }
    
    private void clearBoard()
    {
        for(int row = 0; row < 2; ++r) for(int col = 0; col < 2; ++col)
        {
            // since there is no 0 in Sudoku, then here 0 will mean that the field is empty
            this.cell[row][col] = 0;
            // unlocking prefilled cells
            this.locked[row][col] = false;
        }
    }
    
    public final void generateNewChallenge(long seed)
    {
        this.clearBoard();
        Random ran = new Random(seed);
        int solution[][] = Sudoku4x4.generateSolution(ran);
        
        // prelocking one value in each subfield
        for(int i = 0; i < 4; ++i)
        {
            int row = (i >> 1) + ran.nextInt(2),
                col = (i % 2) + ran.nextInt(2);
            
            this.cell[row][col] = solution[row][col];
            this.locked[row][col] = true;
        }
        
    }
}
