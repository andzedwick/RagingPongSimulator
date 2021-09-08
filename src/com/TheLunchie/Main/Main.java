package com.TheLunchie.Main;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;

public class Main {

	public static enum Difficulty {
		EASY,
		MEDIUM,
		HARD,
		IMPOSSIBLE,
		HACKING
	}
	
	public static enum Direction {
		LEFT,
		RIGHT,
		UP,
		DOWN,
		NONE
	}
	
	public static final String title = "Ping Pong by Andzedwick - v1.0";
	public static Dimension screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize();
	
	
	// Launching point of the game
	public static void main(String[] args) {
		new Game();
	}

}
