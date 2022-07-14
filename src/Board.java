import java.util.Arrays;


/**
 * Represents a Tetris board -- essentially a 2-d grid of booleans. Supports
 * tetris pieces and row clearing. Has an "undo" feature that allows clients to
 * add and remove pieces efficiently. Does not do any drawing or have any idea
 * of pixels. Instead, just represents the abstract 2-d board.
 */
public class Board {

	private int width;
	private int height;
	private int[] widths;
	private int[] backupWidths;
	protected int[] heights;
	private int[] backupHeights;
	protected boolean[][] grid;
	protected boolean[][] backupGrid;
	private boolean committed;
	
	/**
	 * Creates an empty board of the given width and height measured in blocks.
	 */
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		this.widths= new int[height];
		this.heights= new int[width];
		this.backupWidths = new int[height];
		this.backupHeights = new int[width];

		this.grid = new boolean[width][height];
		this.backupGrid = new boolean[width][height];
		this.committed = true;
		
		for(int i=0; i<width; i++){
			this.heights[i]=0;
			this.backupHeights[i]=0;
		}
		for(int j=0; j<height; j++){
			this.widths[j]=0;
			this.backupWidths[j]=0;
		}
		for(int i=0; i<width; i++){
			for(int j=0; j<height; j++){
				this.grid[i][j]=false;
				this.backupGrid[i][j]=false;
			}
		}	
	}
	
	public Board(Board board){
		this.width=board.width;
		this.height=board.height;
		this.backupWidths = new int[board.backupWidths.length];
		this.backupHeights = new int[board.backupHeights.length];
		this.widths=board.widths.clone();
		this.backupWidths=board.widths.clone();
		this.heights=board.heights.clone();
		this.backupHeights=board.heights.clone();
		this.grid=board.grid.clone();
		this.backupGrid=board.grid.clone();
	}
	
	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}
	public int[] getHeights() {
		return this.heights;
	}

	/**
	 * Returns the max column height present in the board. For an empty board
	 * this is 0.
	 */
	public int getMaxHeight() {
		int max=0;
		for(int i=0; i<this.width; i++){
	    	if (getColumnHeight(i)>max){
	    		max=getColumnHeight(i);
	    	}
	    }
	    return max;
	}

	/**
	 * Given a piece and an x, returns the y value where the piece would come to
	 * rest if it were dropped straight down at that x.
	 * 
	 * <p>
	 * Implementation: use the skirt and the columheights to compute this fast --
	 * O(skirt length).
	 */
	public int dropHeight(Piece piece, int x) {
		int height=getColumnHeight(x);
		for(int i=0;i<piece.getSkirt().size();i++){
			if(height<getColumnHeight(x+i)-piece.getSkirt().get(i)) {
				height=getColumnHeight(x+i-piece.getSkirt().get(i));
			}
		}
	    return height; 

	}

	/**
	 * Returns the height of the given column -- i.e. the y value of the highest
	 * block + 1. The height is 0 if the column contains no blocks.
	 */
	public int getColumnHeight(int x) {
		int highest = 0;
		for(int y =0; y<height; y++) {
			if(this.grid[x][y]) {
				highest = y+1;
			}
		}
	    return highest;
	}

	/**
	 * Returns the number of filled blocks in the given row.
	 */
	public int getRowWidth(int y) {
		int filled = 0;
		for(int x =0; x<width; x++) {
			if(this.grid[x][y]) {
				filled+=1;
			}
		}
	    return filled;
	}

	/**
	 * Returns true if the given block is filled in the board. Blocks outside of
	 * the valid width/height area always return true.
	 */
	public boolean getGrid(int x, int y) {
		if(x<0 || x>width || y<0 || y>height) {
			return true;
		}
	    return this.grid[x][y];
	}

	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;

	/**
	 * Attempts to add the body of a piece to the board. Copies the piece blocks
	 * into the board grid. Returns PLACE_OK for a regular placement, or
	 * PLACE_ROW_FILLED for a regular placement that causes at least one row to
	 * be filled.
	 * 
	 * <p>
	 * Error cases: A placement may fail in two ways. First, if part of the
	 * piece may falls out of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 * Or the placement may collide with existing blocks in the grid in which
	 * case PLACE_BAD is returned. In both error cases, the board may be left in
	 * an invalid state. The client can use undo(), to recover the valid,
	 * pre-place state.
	 */
	public int place(Piece piece, int x, int y) {
	    if (!this.committed) {
		throw new RuntimeException("can only place object if the board has been commited");
	    }
	    this.committed = false;
	    int dx, dy;
	    for(TPoint p :piece.getBody()){
	    	dx = p.x + x;
	    	dy = p.y + y;
	    	if(dx < 0 || dx >= this.width || dy >= this.height || dy < 0)
	    		return PLACE_OUT_BOUNDS;
	    	if (this.grid[dx][dy] == true)
	    		return PLACE_BAD;
	    	this.grid[dx][dy] = true;
	    }
	    
	    this.updateWidthsHeights();
	    
	    for(int i=y; i<piece.getHeight(); i++){
	    	if(widths[i]==this.width){
	    		return PLACE_ROW_FILLED;
	    	}
	    }
		
		//Retourne place_ok si le positionnement est reussi et qu'il ne remplit aucune ligne
		
		return PLACE_OK;
	}

	/**
	 * Deletes rows that are filled all the way across, moving things above
	 * down. Returns the number of rows cleared.
	 */
	public int clearRows() {
		int compteur=0;
		
		//On parcourt le tableau widths, on cherche s'il y a des lignes remplies
		for (int j=0; j<this.widths.length; j++) {
			if (this.widths[j]==this.width){
				compteur++;
				
				//Si on trouve une ligne remplie, on baisse toutes les valeurs de heights de 1
				for(int x=0; x<this.width; x++) {
					this.heights[x]-=1;
				}
				
				//Puis on fait un decalage pour les valeurs de widths et pour le tableau de booleens.
				for(int y=j; y<this.height-1; y++) {
					this.widths[y]=this.widths[y+1];
					for(int x=0; x<this.width; x++) {
						this.grid[x][y]=this.grid[x][y+1];
					}
				}
				//On initialise les lignes du haut a 0 et false 
				this.widths[this.height-1]=0;
				for(int x=0; x<this.width; x++) {
					this.grid[x][this.height-1]=false;
				}
				//Si on supprime une ligne, on decremente j pour continuer "l'examination" a partir du bon indice
				j--;
			}
		}
		return compteur;
	}

	/**
	 * Reverts the board to its state before up to one place and one
	 * clearRows(); If the conditions for undo() are not met, such as calling
	 * undo() twice in a row, then the second undo() does nothing. See the
	 * overview docs.
	 */
	public void undo() {
		if (!this.committed) {
			 for (int i=0; i<this.grid.length; i++){
				for (int j=0; j<this.grid[i].length; j++){
					this.grid[i][j]=this.backupGrid[i][j];
				}		
			 }
			for (int i=0; i<this.heights.length; i++){
				this.heights[i]=this.backupHeights[i];
			}
			for (int i=0; i<this.widths.length; i++){
				this.widths[i]=this.backupWidths[i];
			}
			this.committed=true;
		}
	}

	/**
	 * Puts the board in the committed state.
	 */
	public void commit() {
		if (!this.committed) {
			for (int i=0; i<this.grid.length; i++){
				for (int j=0; j<this.grid[i].length; j++){
					this.backupGrid[i][j]=this.grid[i][j];
				}		
			 }
			for (int i=0; i<this.heights.length; i++){
				this.backupHeights[i]=this.heights[i];
			}
			for (int i=0; i<this.widths.length; i++){
				this.backupWidths[i]=this.widths[i];
			}
		    this.committed = true;
			}
	}


	/*
	 * Renders the board state as a big String, suitable for printing. This is
	 * the sort of print-obj-state utility that can help see complex state
	 * change over time. (provided debugging utility)
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = this.height - 1; y >= 0; y--) {
			buff.append('|');
			for (int x = 0; x < this.width; x++) {
				if (getGrid(x, y))
					buff.append('+');
				else
					buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x = 0; x < this.width + 2; x++)
			buff.append('-');
		return buff.toString();
	}

	// Only for unit tests
	protected void updateWidthsHeights() {
		Arrays.fill(this.widths, 0);

		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				if (this.grid[i][j]) {
					this.widths[j] += 1;
					this.heights[i] = Math.max(j + 1, this.heights[i]);
				}
			}
		}
	}

}
