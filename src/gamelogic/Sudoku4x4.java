/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Dan Andersson
 */
public class Sudoku4x4
{
    public class ConflictingCell
    {
        public int row, col;
        
        public ConflictingCell(int row, int col)
        {
            this.row = row;
            this.col = col;
        }
    }
    
    private final int cell[][] = new int[4][4];
    private final boolean locked[][] = new boolean[4][4];
    
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
    
    // if returns 0, then that field isn't filled
    public int get(int row, int col)
    {
        checkBoundary(row, 0, 3);
        checkBoundary(col, 0, 3);

        return this.cell[row][col];
    }
    
    // returns a list of conflicting cells with the same input value. If the list.size() > 0 then the value of the target cell remains unchanged.
    public List<ConflictingCell> set(int row, int col, int value) throws Exception
    {
        checkBoundary(row, 0, 3);
        checkBoundary(col, 0, 3);
        checkValue(value);

        List<ConflictingCell> conflicts = this.checkForConflicts(value, row, col);
        if(conflicts.size() < 1)
            this.cell[row][col] = value;
        return conflicts;
    }
        
    public void clear(int row, int col)
    {
        checkBoundary(row, 0, 3);
        checkBoundary(col, 0, 3);

        this.cell[row][col] = 0;
    }

    // returns the subField row* or col* index. *Depends on input context 
    private static int subField(int rowOrCol)
    {
        return rowOrCol >> 1;
    }
    
    private static void checkBoundary(int v, int min, int max) throws IndexOutOfBoundsException
    {
        if( v < min || v > max) throw new IndexOutOfBoundsException();
    }
    
    private static void checkValue(int v) throws Exception
    {
        if( v < 1 || v > 4) throw new Exception("Value needs to be 1-4.\nFor clearing use the clear(int, int, ..) methods");
    }
        
    private static void paintValueLock(int row, int col, boolean valueCanvas[][])
    {
        { // lock all in subfield
            int sfRow = Sudoku4x4.subField(row),
                sfCol = Sudoku4x4.subField(col);

            valueCanvas[sfRow][sfCol] = false;
            valueCanvas[sfRow][sfCol+1] = false;
            valueCanvas[sfRow+1][sfCol] = false;
            valueCanvas[sfRow+1][sfCol+1] = false;
        }
        
        // lock all in row
        for(int r = 0; r < 4; ++r)
            valueCanvas[r][col] = false;
        
        // lock all in col
        for(int c = 0; c < 4; ++c)
            valueCanvas[row][c] = false;
    }
    
    private static boolean[][][] generateFuzzyBoard()
    {
        boolean potential[][][] = new boolean[4][4][4];
        
        // Every cell value starts as all the values (think uncertaincy principle)
        for(int v = 0; v < 4; ++v) for(int row = 0; row < 4; ++row)
        {
            potential[v][row][1] = true;
            potential[v][row][0] = true;
            potential[v][row][2] = true;
            potential[v][row][3] = true;
        }
        
        return potential;
    }

    private static boolean[][][] generateFuzzyBoard(int board[][])
    {
        boolean potential[][][] = generateFuzzyBoard();
        
        for(int row = 0; row < 4; ++row) for(int col = 0; col < 4; ++col)
        {
            if(board[row][col] > 0)
                paintValueLock(row, col, potential[board[row][col] - 1]);
        }
        return potential;
    }
    
    // returns 0 if there is no 100% certain value. Else rerturns the value (1-4)
    private static int getFuzzyCertain(int row, int col, boolean potential[][][])
    {
        int value = 0,
            count = 0;
        
        for(int v = 0; v < 4; ++v)
        {
            if(potential[v][row][col])
            {
                value = v;
                ++count;
            }
        }
        
        return count > 1 ? 0 : value;        
    }
    
    private static int[][] generateSolution(Random ran)
    {
        int solution[][] = new int[4][4];
        boolean potential[][][] = generateFuzzyBoard();
        
        for(int row = 0; row < 4; ++row)
        {
            for(int col = 0; col < 4; ++col)
            {
                int val = ran.nextInt(4);
                while(!potential[val][row][col]) // todo: bug found here
                    val = (val+1) % 4;
                paintValueLock(row, col, potential[val]);
                solution[row][col] = val+1;
            }
        }
        
        return solution;
    }
    
    private void clearBoard()
    {
        for(int row = 0; row < 2; ++row) for(int col = 0; col < 2; ++col)
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
        int solution[][] = generateSolution(ran);
        
        // prelocking one value in each subfield
        for(int i = 0; i < 4; ++i)
        {
            int row = (i >> 1) + ran.nextInt(2),
                col = (i % 2) + ran.nextInt(2);
            
            this.cell[row][col] = solution[row][col];
            this.locked[row][col] = true;
        }
       
        int filledCells = 4;
        while(filledCells < 16)
        { // perform a mock solving to make sure that there is only 1 solution
            
            int mockBoard[][] = this.cell.clone();
            boolean available[][][] = generateFuzzyBoard(mockBoard);
            
            // fill all 100% certain fields
            int numCertainFound;
            do
            {
                numCertainFound = 0;
                for(int row = 0; row < 4; ++row) for(int col = 0; col < 4; ++col)
                {
                    int certain = getFuzzyCertain(row, col, available);
                    if( certain != 0)
                    {
                        ++numCertainFound;
                        ++filledCells;
                        mockBoard[row][col] = certain;
                        paintValueLock(row, col, available[certain]);
                    }
                }
            }
            while(numCertainFound > 0);
            
            if( filledCells < 16 )
            {
                // we have a guessing case and thus potential cause of multiple solutions
                // we'll solve this by adding a random locked cell and try again
                
                for(boolean notAdded = true; notAdded;)
                {
                    int row = ran.nextInt(4),
                        col = ran.nextInt(4);
                    
                    if(mockBoard[row][col] == 0)
                    {
                        int value = solution[row][col];
                        this.cell[row][col] = value;
                        this.locked[row][col] = true;
                        
                        mockBoard[row][col] = value;
                        paintValueLock(row, col, available[value]);
                        
                        ++filledCells;
                        notAdded = false;
                    }                    
                }
            }
        }
    }
    
    private List<ConflictingCell> checkForConflicts(int value, int row, int col)
    {
        List<ConflictingCell> conflicts = new ArrayList<>(3);
        
        { // check the subField
            int sRow = Sudoku4x4.subField(row),
                sCol = Sudoku4x4.subField(col);

            for(int dr = 0, dc = 0; dr < 2; ++dc)
            {                
                if(dc == 2) { dc = 0; ++dr; }            
                if(this.cell[sRow+dr][sCol+dc] == value)
                {
                    conflicts.add(new ConflictingCell(sRow+dr, sCol+dc));
                    break;
                }
            }
        }
        
        // check row
        for(int r = 0; r < 4; ++r)
        {
            if(this.cell[r][col] == value)
            {
                conflicts.add(new ConflictingCell(r, col));
                break;
            }
        }
        
        // check column
        for(int c = 0; c < 4; ++c)
        {
            if(this.cell[row][c] == value)
            {
                conflicts.add(new ConflictingCell(row, c));
                break;
            }
        }
        
        return conflicts;
    }

}
