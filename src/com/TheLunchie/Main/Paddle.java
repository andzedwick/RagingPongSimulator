package com.TheLunchie.Main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import com.TheLunchie.Main.Main.Direction;

public class Paddle {

	public int sizeXPixelBoundDivisor = 80;
	public int sizeYPixelBoundDivisor = 6;
	
	// The X and Y location, by percentage of the allowed height and width, of the paddle
	private double locationX = 0;
	private double locationY = 0;
	private double lastLocationX = 0;
	private double lastLocationY = 0;
	private int sizeX;
	private int sizeY;
	private Direction wallToProtect;
	private Dimension speed = new Dimension(0, 0);
	private Dimension direction = new Dimension(0, 0);
	private Color color = Color.white;
	
	//____________________________________________________________________________________________
	// Constructor(s)
	//____________________________________________________________________________________________
	
	// Creates a new paddle
	public Paddle(Color paddleColor, Direction wallToProtect, int pixelBoundX, int pixelBoundY) {
		this.color = paddleColor;
		sizeX = (int)(pixelBoundX / sizeXPixelBoundDivisor);
		sizeY = (int)(pixelBoundY / sizeYPixelBoundDivisor);
		this.wallToProtect = wallToProtect;
	}
	
	// Creates a new paddle
	public Paddle(Color paddleColor, Direction wallToProtect, Dimension paddleSize) {
		this.color = paddleColor;
		this.sizeX = paddleSize.width;
		this.sizeY = paddleSize.height;
		this.wallToProtect = wallToProtect;
	}
	
	//____________________________________________________________________________________________
	// Public Methods
	//____________________________________________________________________________________________
	
	// Should be called in the Game.tick() method
	public void detectAndSetDirectionAndSpeed() {
		// Set the paddle x movement direction
		if (lastLocationX < locationX)
			direction.height = 1;
		else if (lastLocationX > locationX)
			direction.height = -1;
		else
			direction.height = 0;
		
		// Set the paddle y movement direction
		if (lastLocationY < locationY)
			direction.height = 1;
		else if (lastLocationY > locationY)
			direction.height = -1;
		else
			direction.height = 0;
		
		// Set the speed of the paddle (pixels per second)
		speed.width = (int) ((locationX - lastLocationX) * Game.TICKS_PER_SECOND);
		speed.height = (int) ((locationY - lastLocationY) * Game.TICKS_PER_SECOND);
		
		// Set the last detected Location y and x
		lastLocationX = locationX;
		lastLocationY = locationY;
	}
	
	//____________________________________________________________________________________________
	// Getters
	//____________________________________________________________________________________________
	
	// Returns the size of the paddle's width
	public int getWidth() {
		return sizeX;
	}
	
	// Returns the size of the paddle's height
	public int getHeight() {
		return sizeY;
	}
	
	// Returns the x position of the paddle
	public double getLocationX() {
		return locationX;
	}
	
	// Returns the y position of the paddle
	public double getLocationY() {
		return locationY;
	}
	
	// Returns the center x position of the paddle
	public double getCenterLocationX() {
		return locationX + (sizeX / 2);
	}
	
	// Returns the center y position of the paddle
	public double getCenterLocationY() {
		return locationY + (sizeY / 2);
	}
	
	// Returns the color of the paddle
	public Color getColor() {
		return color;
	}
	
	// Returns the collision box of the paddle
	public Rectangle getCollisionBox() {
		return new Rectangle((int)locationX, (int)locationY, sizeX, sizeY);
	}
	
	// Returns the direction which the paddle is moving in
	public Dimension getDirection() {
		return direction;
	}
	
	// Returns the speed of the paddle
	public Dimension getSpeed() {
		return speed;
	}
	
	public Direction getWallToProtect() {
		return wallToProtect;
	}
	
	//____________________________________________________________________________________________
	// Setters
	//____________________________________________________________________________________________
	
	// Set the paddle size
	public void setSize(int pixelBoundX, int pixelBoundY) {
		sizeX = (int)(pixelBoundX / sizeXPixelBoundDivisor);
		sizeY = (int)(pixelBoundY / sizeYPixelBoundDivisor);
	}
	
	// Set the paddle size
	public void setSize(Dimension size) {
		sizeX = size.width;
		sizeY = size.height;
	}
	
	// Set the x location
	public void setLocationX(double location) {
		locationX = location;
	}
	
	// Set the y location
	public void setLocationY(double location) {
		locationY = location;
	}
	
	// Sets the color of the paddle
	public void setColor(Color paddleColor) {
		this.color = paddleColor;
	}
}
