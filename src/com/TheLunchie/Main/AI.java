package com.TheLunchie.Main;

import java.util.Random;

import com.TheLunchie.Main.Main.Difficulty;
import com.TheLunchie.Main.Main.Direction;

public class AI {

	private Paddle paddle;
	private Ball ball;
	private Difficulty difficulty;
	private Direction wallToProtect;
	
	private int topMovementSpeed; // Maximum pixels per second the ai can move the paddle
	private int randMovementSpeed = 0;
	private int decidedToMiss = 0; // Tracks if the ai decided to miss: 0 = undecided, 1 = no, 2 = yes
	private float errorMargin; // Value between 0 and 1 which is multiplied by other values to create an error margin.
	private boolean makingRandMovement = false; // Used to track if a rand movement is in progress
	private long missTimer = 0L;
	private long randMovementTimer = 0L;
	private Direction randMovementDirection = Direction.NONE;
	private Random rand;
	
	//____________________________________________________________________________________________
	// Constructor(s)
	//____________________________________________________________________________________________
	
	public AI(Paddle aiPaddle, Difficulty difficulty, Ball ballToWatch, Direction wallToProtect) {
		paddle = aiPaddle;
		ball = ballToWatch;
		this.difficulty = difficulty;
		this.wallToProtect = wallToProtect;
		rand = new Random(System.nanoTime());
		
		switch(difficulty) {
			case EASY:
				topMovementSpeed = 1000;
				errorMargin = 0.7f;
				break;
			case MEDIUM:
				topMovementSpeed = 1500;
				errorMargin = 0.8f;
				break;
			case HARD:
				topMovementSpeed = 2000;
				errorMargin = 0.9f;
				break;
			case IMPOSSIBLE:
				topMovementSpeed = 3000;
				errorMargin = 0.95f;
				break;
			default: // HACKING
				topMovementSpeed = 10000;
				errorMargin = 0.99f;
				break;
		}
	}
	
	//____________________________________________________________________________________________
	// Public Methods
	//____________________________________________________________________________________________
	
	// The ai's engine - should be called as often as possible
	public void process(long nanoTimeSinceLastCall) {
		Direction[] direction = ball.getDirection();
		double scaleX = Math.sin(Math.toRadians((double)ball.getAngle()));
		double scaleY = Math.cos(Math.toRadians((double)ball.getAngle()));
		double dangerDistance = (double)topMovementSpeed * 4 / 3
				+ ((double)topMovementSpeed * errorMargin * ((rand.nextInt(5) == 0) ? 1 : -1));
		
		// Check if the missTimer is up
		if (missTimer <= 0L) {
			missTimer = (long)(rand.nextFloat() / 2 * 1000000000.0);
			decidedToMiss = 0;
		} else
			missTimer -= nanoTimeSinceLastCall;
		
		// Check if the ai should miss for a period of time
		if (decidedToMiss == 0) {
			if (rand.nextFloat() <= errorMargin)
				decidedToMiss = 1;
			else
				decidedToMiss = 2;
		}
		
		// Decide if the ball is in danger of going past the ai and move the paddle accordingly
		switch (wallToProtect) {
			case LEFT:				
				if (direction[0] == Direction.LEFT) {
					if (ball.getLocationX() - (Math.abs(ball.getSpeed() * scaleX)) <= dangerDistance + Game.widthBuffer + paddle.getWidth()){
						// move paddle to intercept ball
						if (decidedToMiss == 2)
							randomlyMovePaddle(false, nanoTimeSinceLastCall);
						else
							movePaddle(Direction.LEFT, nanoTimeSinceLastCall);
							
					} else {
						randomlyMovePaddle(false, nanoTimeSinceLastCall);
					}
				} else {
					randomlyMovePaddle(false, nanoTimeSinceLastCall);
				}
				break;
			case RIGHT:
				if (direction[0] == Direction.RIGHT) {
					if (ball.getLocationX() + (Math.abs(ball.getSpeed() * scaleX)) >= Main.screenSize.width - Game.widthBuffer - paddle.getWidth() - dangerDistance){
						// move paddle to intercept ball
						if (decidedToMiss == 2)
							randomlyMovePaddle(false, nanoTimeSinceLastCall);
						else
							movePaddle(Direction.RIGHT, nanoTimeSinceLastCall);
					} else {
						// randomly move or don't move paddle
						randomlyMovePaddle(false, nanoTimeSinceLastCall);
					}
				} else {
					randomlyMovePaddle(false, nanoTimeSinceLastCall);
				}
				break;
			case UP:
				if (direction[1] == Direction.UP) {
					if (ball.getLocationY() + (Math.abs(ball.getSpeed() * scaleY)) >= Main.screenSize.height - Game.heightBuffer - paddle.getHeight() - dangerDistance){
						// move paddle to intercept ball
						if (decidedToMiss == 2)
							randomlyMovePaddle(true, nanoTimeSinceLastCall);
						else
							movePaddle(Direction.UP, nanoTimeSinceLastCall);
					} else {
							randomlyMovePaddle(true, nanoTimeSinceLastCall);
					}
				} else {
					randomlyMovePaddle(false, nanoTimeSinceLastCall);
				}
				break;
			default:
				if (direction[1] == Direction.DOWN) {
					if (ball.getLocationY() + (Math.abs(ball.getSpeed() * scaleY)) <= dangerDistance + Game.heightBuffer + paddle.getHeight()){
						// move paddle to intercept ball
						if (decidedToMiss == 2)
							randomlyMovePaddle(true, nanoTimeSinceLastCall);
						else
							movePaddle(Direction.DOWN, nanoTimeSinceLastCall);
					} else {
						randomlyMovePaddle(true, nanoTimeSinceLastCall);
					}
				} else {
					randomlyMovePaddle(false, nanoTimeSinceLastCall);
				}
				break;
		}
	}
	
	//____________________________________________________________________________________________
	// Private Methods
	//____________________________________________________________________________________________
	
	// Moves the paddle based upon the ai's parameters and the ball's location
	private void movePaddle(Direction wallToProtect, long nanoSecondsSinceLastProcess) {
		int sign = (rand.nextInt(2) == 0) ? -1 : 1;
		double amountToMove = 0;
		double newLocation = 0;
		double allowedMovement = ((double)topMovementSpeed / 1000000000.0) * nanoSecondsSinceLastProcess;
		
		if (wallToProtect == Direction.LEFT || wallToProtect == Direction.RIGHT) { // move on the y axis
			if (ball.getCenterLocationY() > paddle.getCenterLocationY() ||
					ball.getCenterLocationY() < paddle.getCenterLocationY()) {
				amountToMove = (int)ball.getCenterLocationY() - (int)paddle.getCenterLocationY();
				
				if (amountToMove > allowedMovement)
					amountToMove = allowedMovement;
				else if (amountToMove < -allowedMovement)
					amountToMove = -allowedMovement;
				
				newLocation = paddle.getLocationY() + amountToMove + (sign * errorMargin * amountToMove);
				
				if (newLocation < Game.heightBuffer)
					newLocation = Game.heightBuffer;
				else if (newLocation > Main.screenSize.height - Game.heightBuffer - paddle.getHeight())
					newLocation = Main.screenSize.height - Game.heightBuffer - paddle.getHeight();
				
				paddle.setLocationY(newLocation);
			}
		} else { // move on the x axis
			if (ball.getCenterLocationX() > paddle.getCenterLocationX() ||
					ball.getCenterLocationX() < paddle.getCenterLocationX()) {
				amountToMove = (int)ball.getCenterLocationX() - (int)paddle.getCenterLocationX();
				
				if (amountToMove > allowedMovement)
					amountToMove = allowedMovement;
				else if (amountToMove < -allowedMovement)
					amountToMove = -allowedMovement;
				
				newLocation = paddle.getLocationX() + amountToMove + (sign * errorMargin * amountToMove);
				
				if (newLocation < Game.widthBuffer)
					newLocation = Game.widthBuffer;
				else if (newLocation > Main.screenSize.width - Game.widthBuffer - paddle.getWidth())
					newLocation = Main.screenSize.width - Game.widthBuffer - paddle.getWidth();
				
				paddle.setLocationX(newLocation);
			}
		}
	}
	
	// Randomly moves the paddle some distance (distance is <= topMovementSpeed)
	private void randomlyMovePaddle(boolean xAxis, long nanoSecondsSinceLastProcess) {
		double newLocation = 0.0;
		
		if (!makingRandMovement) {
			makingRandMovement = true;
			
			switch(rand.nextInt() % 4) {
				case 0:
					randMovementDirection = Direction.LEFT;
					break;
				case 1:
					randMovementDirection = Direction.RIGHT;
					break;
				case 2:
					randMovementDirection = Direction.UP;
					break;
				default:
					randMovementDirection = Direction.DOWN;
					break;
			}
			
			if (difficulty != Difficulty.HACKING && difficulty != Difficulty.IMPOSSIBLE) {
				randMovementSpeed = rand.nextInt(topMovementSpeed - (topMovementSpeed / 4)) + (topMovementSpeed / 4);
			} else {
				randMovementSpeed = topMovementSpeed;
			}
			
			randMovementTimer = (long) (rand.nextFloat() * 1000000000.0 / 2.0);
		}

		if (!xAxis) {
			// Get the new location of the paddle
			if (randMovementDirection == Direction.DOWN)
				newLocation = paddle.getLocationY() + (nanoSecondsSinceLastProcess / 1000000000.0 * (double)randMovementSpeed);
			else
				newLocation = paddle.getLocationY() - (nanoSecondsSinceLastProcess / 1000000000.0 * (double)randMovementSpeed);
			
			if (newLocation < Game.heightBuffer)
				newLocation = Game.heightBuffer;
			else if (newLocation > Main.screenSize.height - Game.heightBuffer - paddle.getHeight())
				newLocation = Main.screenSize.height - Game.heightBuffer - paddle.getHeight();
			
			// Set the new location of the paddle
			paddle.setLocationY(newLocation);
		} else {
			// Get the new location of the paddle
			if (randMovementDirection == Direction.RIGHT)
				newLocation = paddle.getLocationX() + (nanoSecondsSinceLastProcess / 1000000000.0 * (double)randMovementSpeed);
			else
				newLocation = paddle.getLocationX() - (nanoSecondsSinceLastProcess / 1000000000.0 * (double)randMovementSpeed);
			
			if (newLocation < Game.widthBuffer)
				newLocation = Game.widthBuffer;
			else if (newLocation > Main.screenSize.width - Game.widthBuffer - paddle.getWidth())
				newLocation = Main.screenSize.width - Game.widthBuffer - paddle.getWidth();
			
			// Set the new location of the paddle
			paddle.setLocationX(newLocation);
		}
		
		if (randMovementTimer > 0L)
			randMovementTimer -= nanoSecondsSinceLastProcess;
		else
			makingRandMovement = false;
	}
	
	
	//____________________________________________________________________________________________
	// Getters
	//____________________________________________________________________________________________
	public Difficulty getDifficulty() {
		return difficulty;
	}
	
	public Direction getWallToProtect() {
		return wallToProtect;
	}
	
	//____________________________________________________________________________________________
	// Setters
	//____________________________________________________________________________________________
}
