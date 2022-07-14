import java.util.List;

import java.util.ArrayList;


/**
 * An immutable representation of a tetris piece in a particular rotation. Each
 * piece is defined by the blocks that make up its body.
 * 
 * Typical client code looks like...
 * 
 * <pre>
 * Piece pyra = new Piece(PYRAMID_STR); // Create piece from string
 * int width = pyra.getWidth(); // 3
 * Piece pyra2 = pyramid.computeNextRotation(); // get rotation
 * 
 * Piece[] pieces = Piece.getPieces(); // the array of all root pieces
 * </pre>
 */
public class Piece {

	// String constants for the standard 7 Tetris pieces
	public static final String STICK_STR = "0 0 0 1 0 2 0 3";
	public static final String L1_STR = "0 0 0 1 0 2 1 0";
	public static final String L2_STR = "0 0 1 0 1 1 1 2";
	public static final String S1_STR = "0 0 1 0 1 1 2 1";
	public static final String S2_STR = "0 1 1 1 1 0 2 0";
	public static final String SQUARE_STR = "0 0 0 1 1 0 1 1";
	public static final String PYRAMID_STR = "0 0 1 0 1 1 2 0";

	// Attributes
	private List<TPoint> body= new ArrayList<>();
	private ArrayList<Integer> skirt;
	private int width;
	private int height;
	
	static private Piece[] pieces; // singleton static array of first rotations

	/**
	 * Defines a new piece given a TPoint[] array of its body. Makes its own
	 * copy of the array and the TPoints inside it.
	 */
	public Piece(List<TPoint> points) {
		
		this.body=points;
		
		int x=0;
		int y=0;
		for(TPoint pt: points){
			if(pt.x>x){
				x=pt.x;
			}
			if(pt.y>y){
				y=pt.y;
			}
		}
		this.width=x+1;
		this.height=y+1;
		
		//declaration d'un tableau de taille x+1 (le comptage de la largeur debutant a 0) pour le skirt
		//pour calculer le skirt, on l'initialise a l'ordonnee max donc a y 
		this.skirt=new ArrayList<Integer>(x+1);
		for(int i=0;i<=x;i++) {
			this.skirt.add(y);
		}
		for(TPoint pt: points){
			if(pt.y<this.skirt.get(pt.x)) {
				this.skirt.set(pt.x, pt.y);
			}
		}
	}
	
	
	/**
	 * Alternate constructor, takes a String with the x,y body points all
	 * separated by spaces, such as "0 0 1 0 2 0 1 1". (provided)
	 */
	public Piece(String points) {
		this(parsePoints(points));
	}

	public Piece(Piece piece) {
		this.width=piece.width;
	    this.height=piece.height;
	    this.body=new ArrayList<TPoint>(piece.body);
	    this.skirt=new ArrayList<Integer>(piece.skirt);
	}


	/**
	 * Given a string of x,y pairs ("0 0 0 1 0 2 1 0"), parses the points into a
	 * TPoint[] array. (Provided code)
	 */
	private static List<TPoint> parsePoints(String rep) {
		List<Integer> coordonnee= new ArrayList<>();
		List<TPoint> coordonnees= new ArrayList<>();
	    for (String c:rep.split(" ")){
	    	coordonnee.add(Integer.parseInt(c));
	    }
	    for (int i=0;i<coordonnee.size();i=i+2){
	    	TPoint point= new TPoint(coordonnee.get(i), coordonnee.get(i+1));
	    	coordonnees.add(point);
	    }
	    return coordonnees;
	}
	
	/**
	 * Returns the width of the piece measured in blocks.
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Returns the height of the piece measured in blocks.
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Returns a reference to the piece's body. The caller should not modify this
	 * list.
	 */
	public List<TPoint> getBody() {
		return this.body;
	}

	/**
	 * Returns a reference to the piece's skirt. For each x value across the
	 * piece, the skirt gives the lowest y value in the body. This is useful for
	 * computing where the piece will land. The caller should not modify this
	 * list.
	 */
	public ArrayList<Integer> getSkirt() {
		return this.skirt;
	}

	/**
	 * Returns a new piece that is 90 degrees counter-clockwise rotated from the
	 * receiver.
	 */
	public Piece computeNextRotation() {
		
		//On cree une nouvelle liste avec les abscisses et les ordonnees inversees
	    List<TPoint> coordsinverse = new ArrayList<>();
		for(TPoint coords: this.body) {
			coordsinverse.add(new TPoint(coords.y,coords.x));
		}
		
		//On cree une nouvelle liste avec les points symetrises par rapport a l'axe verticale x=0 
		List<TPoint> coordsinversesym = new ArrayList<>();
		for(TPoint coords: coordsinverse) {
			coordsinversesym.add(new TPoint(-coords.x,coords.y));
		}
		
		
		//On cree encore une nouvelle liste avec les points translates d'une maniere a replacer la piece a l'origine
		List<TPoint> coordsinversesymtrans = new ArrayList<>();
		//On cherche l'abscisse minimale
		int absmin = coordsinversesym.get(0).x;
		for(TPoint coords:coordsinversesym) {
			if (coords.x<absmin) {
				absmin=coords.x;
			}
		}
		//On cherche l'ordonnée minimale
		int ordmin = coordsinversesym.get(0).y;
		for(TPoint coords:coordsinversesym) {
			if (coords.y<ordmin) {
				ordmin=coords.y;
			}
		}
		for(TPoint coords: coordsinversesym) {
			coordsinversesymtrans.add(new TPoint(coords.x+Math.abs(absmin),coords.y+Math.abs(ordmin)));
		}
		
		//On cree la piece a l'aide du body des points qui ont subi la rotation de -pi/2 (/!\ PS: CA MARCHE MAIS LES COORDONNEES SONT DANS LE MAUVAIS ORDRE)
		return new Piece(coordsinversesymtrans);
		
	}

	/**
	 * Returns true if two pieces are the same -- their bodies contain the same
	 * points. Interestingly, this is not the same as having exactly the same
	 * body arrays, since the points may not be in the same order in the bodies.
	 * Used internally to detect if two rotations are effectively the same.
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof Piece)) {
			return false;
		}
		Piece p = (Piece) obj;
		
		for (TPoint point:this.body ) {
			boolean test=false;
			for(int i=0;i<4;i++){
				if (point.equals(p.body.get(i))){
					 test=true;					
				}
			}
			if(test==false) {
				return false;
			}
		}
	    return true; 
	}

	public String toString() {
		String body="";
		
		for(TPoint p:this.body){
			body+=p;
			body+=" ";
		}
		return body;
	}

	/**
	 * Returns an array containing the first rotation of each of the 7 standard
	 * tetris pieces in the order STICK, L1, L2, S1, S2, SQUARE, PYRAMID. The
	 * next (counterclockwise) rotation can be obtained from each piece with the
	 * {@link #fastRotation()} message. In this way, the client can iterate
	 * through all the rotations until eventually getting back to the first
	 * rotation. (provided code)
	 */
	public static Piece[] getPieces() {
		// lazy evaluation -- create static array if needed
		if (Piece.pieces == null) {
			Piece.pieces = new Piece[] { 
					new Piece(STICK_STR), 
					new Piece(L1_STR),
					new Piece(L2_STR), 
					new Piece(S1_STR),
					new Piece(S2_STR),
					new Piece(SQUARE_STR),
					new Piece(PYRAMID_STR)};
		}

		return Piece.pieces;
	}

}
