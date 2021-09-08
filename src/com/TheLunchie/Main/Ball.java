package com.TheLunchie.Main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Random;

import com.TheLunchie.Main.Main.Difficulty;
import com.TheLunchie.Main.Main.Direction;

public class Ball {
	
	// Note that locations allow for precision points in pixels. These will be converted to ints
	// before being drawn, but for accuracy purposes during speed calculations, the information is
	// stored as floats. This lets decimal points add up.
	private double locationX = 0.0f;
	private double locationY = 0.0f;
	private int directionAngle = 0; // between 0 and 360 (wrapping from 0 to 360 and 360 to 0)
	private int maxAdjustmentAngle = 30; // the max bounce angle changeable by paddle speed
	private int maxDetectedPaddleSpeed = 8000;
	private int speed = 0; // pixels per second
	private int maxAllowedSpeed = 6000;
	private int radius;
	private int borderThickness = 3;
	private int speedIncreaseValue = 0;
	private Color color = Color.green;
	private Color borderColor = color.darker();
	
	//____________________________________________________________________________________________
	// Constructor(s)
	//____________________________________________________________________________________________
	
	// Creates a new ping pong ball
	public Ball(Difficulty difficulty, Dimension location) {
		Random rand = new Random(System.nanoTime());
		int angle = rand.nextInt(360);
		
		
		// Make sure the ball isn't starting at an inconvenient angle
		if (Game.paddleMoveLockX) {
			if (angle < 30)
				angle = 30;
			else if (angle > 150 && angle < 210)
				if (rand.nextBoolean())
					angle = 210;
				else
					angle = 150;
			else if (angle > 330)
				angle = 330;
		} else {
			if (angle > 60 && angle < 120)
				if (rand.nextBoolean())
					angle = 60;
				else
					angle = 120;
			else if (angle > 240 && angle < 300)
				if (rand.nextBoolean())
					angle = 240;
				else
					angle = 300;
		}
		
		setAngle(angle);
		locationX = location.width - radius - borderThickness;
		locationY = location.height - radius - borderThickness;
		
		switch (difficulty) {
			case EASY:
				radius = Main.screenSize.width / 60;
				speed = 500;
				speedIncreaseValue = 10;
				maxAdjustmentAngle = 10;
				break;
			case HARD:
				radius = Main.screenSize.width / 80;
				speed = 800;
				speedIncreaseValue = 30;
				maxAdjustmentAngle = 15;
				break;
			case IMPOSSIBLE:
				radius = Main.screenSize.width / 90;
				speed = 800;
				speedIncreaseValue = 40;
				maxAdjustmentAngle = 35;
				break;
			case HACKING:
				radius = Main.screenSize.width / 100;
				speed = 900;
				speedIncreaseValue = 60;
				maxAdjustmentAngle = 20;
				break;
			default:
				radius = Main.screenSize.width / 70;
				speed = 1000;
				speedIncreaseValue = 20;
				maxAdjustmentAngle = 10;
				break;
		}
		
	}
	
	//____________________________________________________________________________________________
	// Public Methods
	//____________________________________________________________________________________________
	
	// Bounces the ball with deviance depending upon parameters
	public void bounce(Dimension paddleDirection, Dimension paddleSpeed) {
		if (Game.canBounce()) {
			Random rand = new Random(System.nanoTime());
			int adjustmentAngle = rand.nextInt(maxAdjustmentAngle / 3) * ((rand.nextInt(2) == 0) ? 1 : -1);
			
			// If the paddle is moving up or down
			if (paddleDirection.height != 0) { // up or down
				bounce(false, adjustmentAngle);
				setAngle(directionAngle - (int)((float)paddleSpeed.height / (float)maxDetectedPaddleSpeed % 1.0f * maxAdjustmentAngle));
			} else
				bounce(false, adjustmentAngle);
				
			// If the paddle is moving left or right
			if (paddleDirection.width != 0) { // left or right
				bounce(true, adjustmentAngle);
				setAngle(directionAngle - (int)((float)paddleSpeed.width / (float)maxDetectedPaddleSpeed % 1.0f * maxAdjustmentAngle));
			} else {
				bounce(true, adjustmentAngle);
			}
			
			// Add speed to the ball
			if (speed < maxAllowedSpeed)
				speed += speedIncreaseValue;
		}
	}
	
	public void bounce(boolean horizontalWall, int adjustmentAngle) {
		if (Game.canBounce()) {
			Random rand = new Random(System.nanoTime());
			
			// Bounce the ball
			if (horizontalWall)
				setAngle(360 - directionAngle + 180);
			else
				setAngle(360 - directionAngle);
			
			setAngle(directionAngle + adjustmentAngle + (rand.nextInt(maxAdjustmentAngle) * ((rand.nextInt(2) == 0) ? 1 : -1)));
			
			// Reset the number of ticks since the last bounce
			// This is necessary to ensure that a bounce doesn't occur before the cooldown.
			// If a bunch of bounces occur in a row, the speed increases exponentially and odd bounce
			// angles occur. That's why a bounce cooldown is used.
			Game.ticksSinceLastBounce = 0;
		}
	}
	
	// Returns a boolean indicating if the ball is colliding with the given paddle
	public boolean checkCollisionWithPaddle(Paddle paddle) {
		boolean isColliding = false;
		
			switch (paddle.getWallToProtect()) {
				case LEFT:
					if (locationX <= paddle.getLocationX() + paddle.getWidth() && locationX >= 0 && locationY >= paddle.getLocationY() && locationY + (radius * 2) <= paddle.getLocationY() + paddle.getHeight())
						isColliding = true;
					break;
				case RIGHT:
					if (locationX + (radius * 2) >= paddle.getLocationX() && locationX + (radius * 2) <= Main.screenSize.width && locationY >= paddle.getLocationY() && locationY + (radius * 2) <= paddle.getLocationY() + paddle.getHeight())
						isColliding = true;
					break;
				case UP:
					if (locationY <= paddle.getLocationY() + paddle.getHeight() && locationY >= 0 && locationX >= paddle.getLocationX() && locationX + (radius * 2) <= paddle.getLocationX() + paddle.getWidth())
						isColliding = true;
					break;
				default: // DOWN
					if (locationY + (radius * 2) >= paddle.getLocationY() && locationY + (radius * 2) <= Main.screenSize.height && locationX >= paddle.getLocationX() && locationX + (radius * 2) <= paddle.getLocationX() + paddle.getWidth())
						isColliding = true;
					break;
			}
		
		return isColliding;
	}
	
	// Checks if the ball should bounce off a wall
	public Direction checkWallCollision() {
		Direction wallCollidedWith = Direction.NONE;
		
		if (locationX <= 0) {
			wallCollidedWith = Direction.LEFT;
			bounce(false, 0);
		} else if (locationX + (radius * 2) >= Main.screenSize.width) {
			wallCollidedWith = Direction.RIGHT;
			bounce(false, 0);
		} else if (locationY <= 0) {
			wallCollidedWith = Direction.UP;
			bounce(true, 0);
		} else if (locationY + (radius * 2) >= Main.screenSize.height) {
			wallCollidedWith = Direction.DOWN;
			bounce(true, 0);
		}
		
		return wallCollidedWith;
	}
	
	//____________________________________________________________________________________________
	// Getters
	//____________________________________________________________________________________________
	
	// Returns the inner rectangle inside the ping pong ball (for collision)
	public Rectangle getCollisionBox() {
		return new Rectangle((int)locationX, (int)locationY, radius * 2 - borderThickness, radius * 2 - borderThickness);
	}
	
	// Returns the ball's current location
	public double getLocationX() {
		return locationX;
	}
	
	public double getLocationY() {
		return locationY;
	}
	
	public double getCenterLocationX() {
		return locationX + radius;
	}
	
	public double getCenterLocationY() {
		return locationY + radius;
	}
	
	public int getAngle() {
		return directionAngle;
	}
	
	public Direction[] getDirection() {
		Direction[] dir = new Direction[] {Direction.NONE, Direction.NONE};
		
		if (directionAngle == 0)
			dir[1] = Direction.DOWN;
		else if (directionAngle == 90)
			dir[0] = Direction.RIGHT;
		else if (directionAngle == 180)
			dir[1] = Direction.UP;
		else if (directionAngle == 270)
			dir[0] = Direction.RIGHT;
		else if (directionAngle < 90) {
			dir[0] = Direction.RIGHT;
			dir[1] = Direction.DOWN;
		} else if (directionAngle < 180) {
			dir[0] = Direction.RIGHT;
			dir[1] = Direction.UP;
		} else if (directionAngle < 270) {
			dir[0] = Direction.LEFT;
			dir[1] = Direction.UP;
		} else if (directionAngle < 360) {
			dir[0] = Direction.LEFT;
			dir[1] = Direction.DOWN;
		}
		
		return dir;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public int getMaxSpeed() {
		return maxAllowedSpeed;
	}
	
	public int getRadius() {
		return radius;
	}
	
	public Color getColor() {
		return color;
	}
	
	public Color getBorderColor() {
		return borderColor;
	}
	
	public int getBorderThickness() {
		return borderThickness;
	}
	
	//____________________________________________________________________________________________
	// Setters
	//____________________________________________________________________________________________
	
	// Sets the ball's current location
	public void setLocationX(double location) {
		locationX = location;
	}
	
	public void setLocationY(double location) {
		locationY = location;
	}
	
	public void setAngle(int angle) {
		// Wrap the angle from 360 to 0 and 0 to 360
		if (angle >= 360)
			angle = angle % 360;
		else if (angle < 0)
			angle = 360 + (angle % 360);
		
		directionAngle = angle;
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public void setMaxSpeed(int speed) {
		maxAllowedSpeed = speed;
	}
	
	public void setRadius(int radius) {
		if (radius > 0)
			this.radius = radius;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public void setBorderColor(Color color) {
		borderColor = color;
	}
	
	public void setBorderThickness(int thickness) {
		borderThickness = thickness;
	}
	
}
