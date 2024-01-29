package com.ufa.vid;

import java.io.IOException;

/* Code by: Vid Vukobratovic
 * Date started: 04/04/2016
 * Date submited: x/04/2016
 * This is the game of battleship
 */

import java.util.Random;
import java.util.Scanner;

public class Battleship {

    private static final int MAX_SHIP_LENGTH = 5;

    private static final String[] shipNames = new String[MAX_SHIP_LENGTH];

    private static final String CORVETTE = "Corvette";
    private static final String FRIGATE = "Frigate";
    private static final String DESTROYER = "Destroyer";
    private static final String CRUISER = "Cruiser";
    private static final String CARRIER = "Aircraft Carrier";

    private static final char[] shipSymbols = new char[MAX_SHIP_LENGTH];

    // Game model
    private int playerAShipCoordinates[][] = new int[MAX_SHIP_LENGTH][3];
    private int playerBShipCoordinates[][] = new int[MAX_SHIP_LENGTH][3];

    // Game state, ship layout and display
    private char[][] playerAGameBoard = new char[10][10];
    private char[][] playerBGameBoard = new char[10][10];

    private String[] playerNames = new String[2];

    private int moves = 0;

    private Scanner scanner = null;
    
    static {
        shipNames[0] = CORVETTE;
        shipNames[1] = FRIGATE;
        shipNames[2] = DESTROYER;
        shipNames[3] = CRUISER;
        shipNames[4] = CARRIER;

        shipSymbols[0] = 'v'; // Cor(v)ette
        shipSymbols[1] = 'f'; // (F)rigate
        shipSymbols[2] = 'd'; // (D)estroyer
        shipSymbols[3] = 'c'; // (C)ruiser
        shipSymbols[4] = 'a'; // (A)ircraft carrier
    }
    
    public Battleship() {
    	scanner = new Scanner(System.in);
    }

    private void initializeGame() throws Exception {
        showInstructions();
        promptGameParameters();
        wipeBoardsClean();
        initializeModel();
        display(true);
    }

    private void close() {
    	if (scanner != null) {
    		scanner.close();
    	}
    }
    
    //instructions and rules
    private void showInstructions() throws IOException {
      System.out.println("Welcome to Vid's battleship!");
      System.out.println("Instructions: ");
      System.out.println("1) The locations of your battleships will randomly be generated.");
      System.out.println("2) The goal of the game is to sink all of your opponents ships.");
      System.out.println("3) Players will alternate firing their opponents ships.");
      System.out.println("4) Play fair and have fun!");
      System.out.println("Press ENTER to continue...");
      scanner.nextLine();
    }
    
    //Gets player names and saves in array
    private void promptGameParameters() {
      for (int counter = 0; counter < 2; counter ++) {
        System.out.println("Player " + (counter + 1) + ", enter your name! ");
        String pNames = scanner.nextLine();
        playerNames[counter] = pNames;
      }
    }

    // Clean the slate
    private void wipeBoardsClean() {
        wipeBoard(playerAGameBoard);
        wipeBoard(playerBGameBoard);
    }

    // Put a space in every board field
    private void wipeBoard(char[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                board[i][j] = ' ';
            }
        }
    }

    // Randomize battle ships on seaboards
    private void initializeModel() {

        // Initialize a randomizer to layout ships
        Random randomizer = new Random(System.currentTimeMillis());

        // x, y of where the ship starts and orientation - horizontal or
        // vertical
        int row = 0, col = 0, direction = 0;

        // Start with player A
        char[][] board = playerAGameBoard;
        int[][] shipCoordinates = playerAShipCoordinates;

        int randomizerLimit = 0;

        for (int h = 0; h < 2; h++) {
            // Start with the longest ship(s) first,
            // they are the hardest to randomly fit

            for (int length = MAX_SHIP_LENGTH; length > 0; length--) {

                boolean laidOut = false;
                while (!laidOut) {

                    // randomize the row and column indices, must fit the ship
                    // length
                    direction = ((randomizer.nextInt(10) % 2) == 0) ? 0 : 1;
                    randomizerLimit = playerAGameBoard.length;
                    if (shipHorizontal(direction)) {
                        randomizerLimit = randomizerLimit - length;
                    }
                    row = randomizer.nextInt(randomizerLimit);

                    randomizerLimit = playerAGameBoard.length;
                    if (shipVertical(direction)) {
                        randomizerLimit = randomizerLimit - length;
                    }
                    col = randomizer.nextInt(playerAGameBoard.length - length);

                    // Can this ship fit at this position with the given
                    // orientation?
                    if (allocated(board, shipCoordinates, row, col, direction, length)) {

                        // This ship has been allocated. On to the next.
                        laidOut = true;
                    }
                }
            }

            // Switch to player B
            board = playerBGameBoard;
            shipCoordinates = playerBShipCoordinates;
        }
    }

    private boolean shipHorizontal(int direction) {
    	return direction == 1;
    }

    private boolean shipVertical(int direction) {
    	return direction == 0;
    }

    private boolean allocated(
            char[][] board, 
            int[][] shipCoordinates, 
            int row, 
            int col, 
            int direction, 
            int length) {

        boolean allocated = false;
        int r = row;
        int c = col;

        if (canAllocate(board, row, col, direction, length)) {
            for (int i = 0; i < length; i++) {
                // Init the ship's 'health' by using the corresponding letter
                board[r][c] = shipSymbols[length - 1];
                
            	// Should the ship be laid out horizontally or vertically?
                if (shipHorizontal(direction)) {
                    r++;
                } else {
                    c++;
                }
            }

            // Record ship coordinates and orientation
            shipCoordinates[length - 1][0] = row;
            shipCoordinates[length - 1][1] = col;
            shipCoordinates[length - 1][2] = direction;

            allocated = true;
        }
        return allocated;
    }

    private boolean canAllocate(
            char[][] board, int row, int col, int direction, int length) {

        boolean canAllocate = true;

        for (int i = 0; i < length; i++) {
            if (board[row][col] != ' ') {
                canAllocate = false;
                break;
            }
            // Should the ship be laid out horizontally or vertically?
            if (shipHorizontal(direction)) {
                row++;
            } else {
                col++;
            }
        }
        return canAllocate;
    }

    // Should be a Runnable implementation
    private void run() {
        display();

        char[][] board = null;
        int[][] shipCoordinates = null;
        
        // main game loop
        boolean finished = false;
        
        while (!finished) {
            // Prompt current user for input (row, column)
            
            // Determine which player's turn it is
            // This player will hit their opponent's seaboard 
            if (moves % 2 == 0) {
                board = playerBGameBoard;
                shipCoordinates = playerBShipCoordinates;
            } else {
                board = playerAGameBoard;
                shipCoordinates = playerAShipCoordinates;
            }
            
            boolean samePlayerMovesAgain = promptMove(board, shipCoordinates);
            
            // Increment move count
            if (!samePlayerMovesAgain) {
            	incrementMoveCounter();
            }
            
            finished = isGameOver(shipCoordinates);
            if (!finished) {
            	display();
            }
        }

        showWinner();
    }

	private boolean promptMove(char[][] board, int[][] shipCoordinates) {

        // Read row, then column
        int row = -1, col = -1;
        String input = null;
        
        while (row < 0 || row > 9) {
        	System.out.print("Enter row (A-J): ");
            input = scanner.next().toUpperCase();
            if (input.length() > 1) {
            	System.out.println("Invalid Input, try again.");
            } 
        	row = capitalLetterToInt(input.charAt(0));
            if (row < 0 || row > 9) {
                System.out.println("Invalid row, try again (A-J)");
            }
        }
        
        System.out.println();
        while (col < 0 || col > 9) {
        	System.out.print("Enter column (0-9): ");
            // Convert to upper case, just in case..
            input = scanner.next();
            if (input.length() > 1) {
            	System.out.println("Invalid Input, try again.");
            } else if (!Character.isDigit(input.charAt(0))){
            	System.out.println("Invalid Input, try again.");
            } else {
            	col = Integer.valueOf(input);
            	if (col < 0 || col > 9) {
            		System.out.println("Invalid column, try again (0-9)");
            	}
            }
        }

        boolean hit = updateSeaboard(board, row, col);
        updateShipStatuses(board, shipCoordinates);
        return hit;
    }

    // Updates the sea board with user input. Returns false if the moves counter
    // should not increment to give the same player another shot.
    private boolean updateSeaboard(char[][] board, int row, int col) {
    	boolean hit = false;
    	
        if (board[row][col] == ' ') {
            // nothing is in that board cell, put a wave symbol '~'
            board[row][col] = '~';
        } 
        else if (board[row][col] == '~') {
            // this was already an empty
            System.out.println(playerNames[moves % 2] + ", you already shot here!");
        }
        else if (Character.isUpperCase(board[row][col])) {
            // this was a previous hit
            System.out.println(playerNames[moves % 2] + ", you already shot here!");
        } else {
            // there is a hit, put an 'X'
            board[row][col] = 'X';
            // Show the hit
            displayHit(row, col);
            hit = true;
        }
        
        return hit;
    }

    private void incrementMoveCounter() {
    	moves++;
    }
    
	private void updateShipStatuses(char[][] board, int[][] shipCoordinates) {
        
        // update ship statuses to tell the player if and when a ship is sunk
    	int row = 0;
    	int col = 0;
    	int direction = 0;
    	int length = 0;
    	boolean sunk = true;
    	
    	for (int i = 0; i < shipCoordinates.length; i++) {
    		
    		// Does this ship still exist?
    		if (shipCoordinates[i] == null) {
    			continue;
    		}
    		
    		row = shipCoordinates[i][0];
    		col = shipCoordinates[i][1];
    		direction = shipCoordinates[i][2];
    		length = i + 1;
    		
    		for (int l = 0; l < length; l++) {
    			char c = board[row][col];
    			if (c != 'X') {
    				// Any remaining segment of the ship will make it float
    				sunk = false;
    				break;
    			}
    			if (shipHorizontal(direction)) {
    				row++;
    			} else {
    				col++;
    			}
    		}
    		
    		if (sunk) {
    			row = shipCoordinates[i][0];
    			col = shipCoordinates[i][1];
    			// Replace all the 'X's with capital corresponding ship symbols
    			for (int l = 0; l < length; l++) {
    				board[row][col] = Character.toUpperCase(shipSymbols[i]);
    	
    				if (shipHorizontal(direction)) {
    					row++;
    				} else {
    					col++;
    				}
    			} 
    			// This ship is sunk, remove its coordinates from the model
    			shipCoordinates[i] = null;
    			System.out.println("Player " + playerNames[moves % 2] + ", you just sunk your oponent's " + shipNames[i] + "!");
    		}
    		
    		sunk = true;
    	}
    }

    private boolean isGameOver(int[][] shipCoordinates) {
    	boolean gameOver = true;
		for (int i = 0; i < shipCoordinates.length; i++) {
			if (shipCoordinates[i] != null) {
				// at least one more ship remaining, game is not over
				gameOver = false;
				break;
			}
		}
		return gameOver;
	}

    private void showWinner() {
        System.out.println("Congratulations player: " + playerNames[moves % 2] + ", you won!");
        System.out.println("The game is now over after: " + moves + " moves."
        		+ " The remaining fields on both seaboards are:");
        display(true);
    }

    private void display() {
        display(false);
    }
    
    private void display(boolean revealSeaboards) {
        if (!revealSeaboards) { 
        	displayHeader();
        }
        displayColumnRow();
        displaySeparatorRow();
        for (int i = 0; i < playerAGameBoard.length; i++) {
            displayRow(i, revealSeaboards);
        }
    }

    private void displayHeader() {
        StringBuilder builder = new StringBuilder();

        builder.append("Moves: ");
        builder.append(moves);
        builder.append("; Player: ");
        
        int turn = moves % 2;
        builder.append(playerNames[turn]);
       	builder.append("'s turn.");
        
        builder.append(System.getProperty("line.separator"));
        show(builder.toString());
    }

    private void show(String string) {
        System.out.println(string);
    }

    private void displayColumnRow() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            builder.append(i == 0 ? "P1" : "P2");
            builder.append("|");
            for (int j = 0; j < playerAGameBoard.length; j++) {
                builder.append(j);
            }
            builder.append("  ");
        }
        show(builder.toString());
    }

    private void displaySeparatorRow() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            builder.append("--+");
            for (int j = 0; j < playerAGameBoard.length; j++) {
                builder.append("-");
            }
            builder.append("  ");
        }
        show(builder.toString());
    }

    private void displayRow(int r, boolean revealSeaboards) {

        StringBuilder builder = new StringBuilder();
        char c;
        char[][] board = playerAGameBoard;

        for (int i = 0; i < 2; i++) {
            builder.append(" ");
            builder.append(intToCapitalLetter(r));
            builder.append("|");
            for (int j = 0; j < playerAGameBoard.length; j++) {
                if (revealSeaboards) {
                    // Show all the fields as they are
                    builder.append(board[r][j]);
                } else {
                    c = board[r][j];
                    if (c == '~' || c == 'X' || isUpperCaseLetter(c)) {
                        // game is on, only show known fields
                        builder.append(c);
                    } else {
                        // This field has not been fired upon. Show empty
                        builder.append(' ');
                    }
                }
            }
            builder.append("  ");
            board = playerBGameBoard;
        }
        show(builder.toString());
    }

    private void displayHit(int row, int col) {
    	System.out.println(playerNames[moves % 2] + " hit a ship on " 
    			+ intToCapitalLetter(row) + ":" + (col) + ", your turn again!");
	}


    private boolean isUpperCaseLetter(char c) {
        return (65 <= c && c <= 90);
    }

    private char intToCapitalLetter(int i) {
        // Add 65, the numeric value of 'A', to the int to 
        // get the corresponding letter
        return (char) ((i >= 0 && i < 26) ? (i + 65) : 'x');
    }

    private int capitalLetterToInt(char c) {
        // Subtract 65, the numeric value of 'A', from the char to 
        // get the corresponding integer value of the capital letter
        return c >= 65 ? Integer.valueOf(c - 65) : 0;
    }

  public static void main(String[] args) {
        Battleship game = new Battleship();
	try {

	    game.initializeGame();
	    game.run();

	    game.close();
	} catch (Exception e) {
	    System.out.println("An unexpected error happened.");
	    e.printStackTrace();
	}
    }
}
