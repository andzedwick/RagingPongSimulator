package com.TheLunchie.Main;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;

import com.TheLunchie.Main.Main.Difficulty;
import com.TheLunchie.Main.Main.Direction;

public class Game extends JPanel implements MouseMotionListener, KeyListener{

	public static final int MAX_TICK = 99;
	public static final int TICKS_PER_SECOND = 60;
	
	private static final long serialVersionUID = 1L;
	
	public static boolean paddleMoveLockX = true;
	public static boolean paddleMoveLockY = false;
	public static boolean playingGame = false;
	public static boolean paused = false;
	public static int delta = 0; // tracks fps as its calculated
	public static int fps = 0; // tracks the fps
	public static int ticksSinceLastBounce = 0;
	public static int widthBuffer = Main.screenSize.width / 50;
	public static int heightBuffer = Main.screenSize.height / 50;
	public static int pixelBoundX = Main.screenSize.width - (widthBuffer * 2);
	public static int pixelBoundY = Main.screenSize.height - (heightBuffer * 2);
	public static float bounceTimeBuffer = 0.1f; // the minimum number of seconds allowed between bounces.
	public static Difficulty difficulty = Difficulty.MEDIUM;
	
	// This is used on top of ticksSinceLastSecond because if the fps is set to 1 (for instance)
	// then ticksSinceLastSecond will only accumulate to 1 (making certain calculations impossible).
	private static int currentTick = 0;
	
	private boolean optionsOpened = false;
	private boolean aiOnly = false;
	private String aiName = "Bob";
	private String ai2Name = "Steve";
	private Direction aiWallToProtect = Direction.RIGHT;
	private Direction playerWallToProtect = Direction.LEFT;
	private JFrame frame;
	private JFrame optionsFrame;
	private Thread gameThread;
	private Paddle playerPaddle;
	private Paddle aiPaddle;
	private Ball ball;
	private AI ai;
	private AI ai2;
	private int mouseX = 0;
	private int mouseY = 0;
	private int ticksPerDirectionUpdate = (int) Math.ceil((double)(TICKS_PER_SECOND / 10.0));
	private int ticksSinceLastSecond = 0;
	private int visualFPSCap = 500;
	private int score1 = 0;
	private int score2 = 0;
	private int pointsToWin = 21;
	private float pointCooldown = 0.3f; // Cooldown in seconds before another point can be scored
	private long pointCooldownTimer = 0L;
	private String[] names = new String[] {
			"Jesus",
			"Satan",
			"Grandma",
			"Grandpa",
			"Bob",
			"Steve",
			"Null",
			"NA",
			"JamesBlond",
			"WhereAmI?",
			"Biden",
			"Trump",
			"GetRekt!",
			"Neewb",
			"StopHacking",
			"Clintons",
			"\"CommitedSuicide\"",
			"Loser",
			"Destroyer",
			"Lucky",
			"Streamer",
			"ItWasTheWind",
			"Insane",
			"Can'tBelieveIt!!!",
			"SoreLoser",
			"IRanOutOfNames"
	};
	
	//____________________________________________________________________________________________
	// Constructor(s)
	//____________________________________________________________________________________________
	
	// Sets up a new game window and a new game, if there is not already an instance of Game running.
	public Game() {
		init();
	}
	
	//____________________________________________________________________________________________
	// Public Methods
	//____________________________________________________________________________________________
	
	// Start a new game
	public void newGame(Difficulty diff) {
		Random rand = new Random(System.nanoTime());
		score1 = 0;
		score2 = 0;
		difficulty = diff;
		
		aiName = names[rand.nextInt(names.length)];
		ai2Name = names[rand.nextInt(names.length)];
		
		// Create a new paddle for the player
		if (diff == Difficulty.HACKING)
			playerPaddle = new Paddle(Color.gray, Direction.LEFT, pixelBoundX, pixelBoundY / 2);
		else
			playerPaddle = new Paddle(Color.white, Direction.LEFT, pixelBoundX, pixelBoundY);
		
		playerPaddle.setLocationX(widthBuffer);
		playerPaddle.setLocationY(heightBuffer);
		
		// Create a new paddle for the ai
		if (diff == Difficulty.HACKING)
			aiPaddle = new Paddle(Color.black, Direction.RIGHT, pixelBoundX, pixelBoundY / 2);
		else
			aiPaddle = new Paddle(Color.red, Direction.RIGHT, pixelBoundX, pixelBoundY);
		
		aiPaddle.setLocationX(pixelBoundX - aiPaddle.getWidth() + widthBuffer);
		aiPaddle.setLocationY((pixelBoundY / 2) - (aiPaddle.getHeight() / 2) + heightBuffer);
		
		// Create a ping pong ball
		ball = new Ball(difficulty, new Dimension(Main.screenSize.width / 2, Main.screenSize.height / 2));
	
		// Create the ai
		ai = new AI(aiPaddle, difficulty, ball, aiWallToProtect);
		
		if (aiOnly)
			ai2 = new AI(playerPaddle, difficulty, ball, playerWallToProtect);
		else
			ai2 = null;
		
		playingGame = true;
		paused = false;
		
		gameThread();
	}
	
	// Ends the current game
	public void endGame() {
		playingGame = false;
	}
	
	// Opens the game options popup
	public void openOptions() {
		JPanel topPanel;
		JPanel bottomPanel;
		JButton newGame;
		JButton resumeGame;
		JButton quit;
		JLabel difficultyHeader;
		JLabel easyHeader;
		JLabel mediumHeader;
		JLabel hardHeader;
		JLabel hackingHeader;
		JLabel impossibleHeader;
		JLabel playersHeader;
		JLabel speedHeader;
		JLabel pointsToWin;
		ButtonGroup difficultyRadioButtons;
		JRadioButton easyRadio;
		JRadioButton mediumRadio;
		JRadioButton hardRadio;
		JRadioButton hackingRadio;
		JRadioButton impossibleRadio;
		JCheckBox playersCheckBox;
		JSlider speedSlider;
		JSlider pointsToWinSlider;
		
		Font buttonFont;
		Font headerFont;
		Font labelFont;
		
		int resumeGameWidth;
		int resumeGameHeight;
		int newGameWidth;
		int newGameHeight;
		
		paused = true;
		optionsOpened = true;
		
		if (frame != null)
			frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		// Setup the JFrame
		optionsFrame = new JFrame("Ping Pong Options");
		optionsFrame.setSize(Main.screenSize.width / 2, Main.screenSize.height / 2);
		optionsFrame.setLocationRelativeTo(null);
		optionsFrame.setResizable(false);
		optionsFrame.setLayout(null);
		optionsFrame.setUndecorated(true);
		optionsFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				closeOptions();
			}
		});
		optionsFrame.addKeyListener(this);
		optionsFrame.setVisible(true);
		optionsFrame.requestFocus();
		
		// Setup the JFrame's Panel		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(null);
		bottomPanel.setSize(optionsFrame.getWidth(), optionsFrame.getHeight() / 7);
		bottomPanel.setLocation(0, optionsFrame.getHeight() - bottomPanel.getHeight());
		bottomPanel.setBackground(Color.gray);
		optionsFrame.add(bottomPanel);
		bottomPanel.setVisible(true);
		
		topPanel = new JPanel();
		topPanel.setLayout(null);
		topPanel.setSize(optionsFrame.getWidth(), optionsFrame.getHeight() - bottomPanel.getHeight());
		topPanel.setLocation(0, 0);
		topPanel.setBackground(Color.lightGray);
		optionsFrame.add(topPanel);
		topPanel.setVisible(true);
		
		// Add the difficulty options
		difficultyRadioButtons = new ButtonGroup();
		headerFont = new Font("headerFont", Font.BOLD, topPanel.getHeight() / 20);
		labelFont = new Font("labelFont", Font.PLAIN, topPanel.getHeight() / 30);
		difficultyHeader = new JLabel("Select Difficulty");
		easyHeader = new JLabel("EASY: ");
		mediumHeader = new JLabel("MEDIUM: ");
		hardHeader = new JLabel("HARD: ");
		impossibleHeader = new JLabel("IMPOSSIBLE: ");
		hackingHeader = new JLabel("HACKING: ");
		
		difficultyHeader.setFont(headerFont);
		difficultyHeader.setForeground(Color.magenta.darker());
		difficultyHeader.setBounds(10, 10, topPanel.getWidth() - 20, (topPanel.getHeight() / 6) - 20);
		topPanel.add(difficultyHeader);
		difficultyHeader.setVisible(true);
		
		easyHeader.setFont(labelFont);
		easyHeader.setBounds(10, difficultyHeader.getY() + difficultyHeader.getHeight() + 10, easyHeader.getFontMetrics(labelFont).stringWidth(easyHeader.getText()), difficultyHeader.getHeight());
		topPanel.add(easyHeader);
		easyHeader.setVisible(true);
		
		easyRadio = new JRadioButton();
		easyRadio.setBounds(easyHeader.getX() + easyHeader.getWidth(), easyHeader.getY(), 50, difficultyHeader.getHeight());
		easyRadio.setBackground(topPanel.getBackground());
		difficultyRadioButtons.add(easyRadio);
		topPanel.add(easyRadio);
		easyRadio.setVisible(true);
		easyRadio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				difficulty = Difficulty.EASY;
			}
		});
		
		mediumHeader.setFont(labelFont);
		mediumHeader.setBounds(10 + easyRadio.getX() + easyRadio.getWidth(), difficultyHeader.getY() + difficultyHeader.getHeight() + 10, mediumHeader.getFontMetrics(labelFont).stringWidth(mediumHeader.getText()) + 20, difficultyHeader.getHeight());
		topPanel.add(mediumHeader);
		mediumHeader.setVisible(true);
		
		mediumRadio = new JRadioButton();
		mediumRadio.setBounds(mediumHeader.getX() + mediumHeader.getWidth(), mediumHeader.getY(), 50, difficultyHeader.getHeight());
		mediumRadio.setBackground(topPanel.getBackground());
		difficultyRadioButtons.add(mediumRadio);
		topPanel.add(mediumRadio);
		mediumRadio.setVisible(true);
		mediumRadio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				difficulty = Difficulty.MEDIUM;
			}
		});
		
		hardHeader.setFont(labelFont);
		hardHeader.setBounds(10 + mediumRadio.getX() + mediumRadio.getWidth(), difficultyHeader.getY() + difficultyHeader.getHeight() + 10, hardHeader.getFontMetrics(labelFont).stringWidth(hardHeader.getText()), difficultyHeader.getHeight());
		topPanel.add(hardHeader);
		hardHeader.setVisible(true);
		
		hardRadio = new JRadioButton();
		hardRadio.setBounds(hardHeader.getX() + hardHeader.getWidth(), hardHeader.getY(), 50, difficultyHeader.getHeight());
		hardRadio.setBackground(topPanel.getBackground());
		difficultyRadioButtons.add(hardRadio);
		topPanel.add(hardRadio);
		hardRadio.setVisible(true);
		hardRadio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				difficulty = Difficulty.HARD;
			}
		});
		
		impossibleHeader.setFont(labelFont);
		impossibleHeader.setBounds(10 + hardRadio.getX() + hardRadio.getWidth(), difficultyHeader.getY() + difficultyHeader.getHeight() + 10, impossibleHeader.getFontMetrics(labelFont).stringWidth(impossibleHeader.getText()), difficultyHeader.getHeight());
		topPanel.add(impossibleHeader);
		impossibleHeader.setVisible(true);
		
		impossibleRadio = new JRadioButton();
		impossibleRadio.setBounds(impossibleHeader.getX() + impossibleHeader.getWidth(), impossibleHeader.getY(), 50, difficultyHeader.getHeight());
		impossibleRadio.setBackground(topPanel.getBackground());
		difficultyRadioButtons.add(impossibleRadio);
		topPanel.add(impossibleRadio);
		impossibleRadio.setVisible(true);
		impossibleRadio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				difficulty = Difficulty.IMPOSSIBLE;
			}
		});
		
		hackingHeader.setFont(labelFont);
		hackingHeader.setBounds(10 + impossibleRadio.getX() + impossibleRadio.getWidth(), difficultyHeader.getY() + difficultyHeader.getHeight() + 10, hackingHeader.getFontMetrics(labelFont).stringWidth(hackingHeader.getText()), difficultyHeader.getHeight());
		topPanel.add(hackingHeader);
		hackingHeader.setVisible(true);
		
		hackingRadio = new JRadioButton();
		hackingRadio.setBounds(hackingHeader.getX() + hackingHeader.getWidth(), hackingHeader.getY(), 50, difficultyHeader.getHeight());
		hackingRadio.setBackground(topPanel.getBackground());
		difficultyRadioButtons.add(hackingRadio);
		topPanel.add(hackingRadio);
		hackingRadio.setVisible(true);
		hackingRadio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				difficulty = Difficulty.HACKING;
			}
		});
		
		switch (difficulty) {
			case EASY:
				easyRadio.setSelected(true);
				break;
			case MEDIUM:
				mediumRadio.setSelected(true);
				break;
			case HARD:
				hardRadio.setSelected(true);
				break;
			case IMPOSSIBLE:
				impossibleRadio.setSelected(true);
				break;
			default:
				hackingRadio.setSelected(true);
				break;
		}
		
		// Add player and speed options
		playersHeader = new JLabel("AI Battle Mode: ");
		playersHeader.setBounds(10, easyHeader.getY() + easyHeader.getHeight() + 10, playersHeader.getFontMetrics(headerFont).stringWidth(playersHeader.getText()) + 20, difficultyHeader.getHeight());
		playersHeader.setFont(headerFont);
		playersHeader.setForeground(Color.magenta.darker());
		playersCheckBox = new JCheckBox();
		playersCheckBox.setBounds(playersHeader.getX() + playersHeader.getWidth() + 10, playersHeader.getY(), topPanel.getWidth(), difficultyHeader.getHeight());
		playersCheckBox.setBackground(topPanel.getBackground());
		if (aiOnly)
			playersCheckBox.setSelected(true);
		topPanel.add(playersHeader);
		topPanel.add(playersCheckBox);
		playersHeader.setVisible(true);
		playersCheckBox.setVisible(true);
		
		speedHeader = new JLabel("Maximum Ball Speed: ");
		speedHeader.setBounds(10, playersHeader.getY() + playersHeader.getHeight() + 10, speedHeader.getFontMetrics(headerFont).stringWidth(speedHeader.getText()) + 20, difficultyHeader.getHeight());
		speedHeader.setFont(headerFont);
		speedHeader.setForeground(Color.magenta.darker());
		speedSlider = new JSlider(3000, 20000, ball.getMaxSpeed());
		speedSlider.setFont(labelFont);
		speedSlider.setBackground(topPanel.getBackground());
		speedSlider.setForeground(Color.black);
		speedSlider.setBounds(speedHeader.getX() + speedHeader.getWidth() + 30, speedHeader.getY(), topPanel.getWidth() / 2, (int)(speedHeader.getHeight() * 1.5));
		speedSlider.setSnapToTicks(true);
		speedSlider.setMajorTickSpacing(3000);
		speedSlider.setMinorTickSpacing(1000);
		speedSlider.setPaintTicks(true);
		speedSlider.setPaintLabels(true);
		topPanel.add(speedHeader);
		topPanel.add(speedSlider);
		speedHeader.setVisible(true);
		speedSlider.setVisible(true);
		
		pointsToWin = new JLabel("Points to Win: ");
		pointsToWin.setBounds(10, speedSlider.getY() + speedSlider.getHeight() + 30, pointsToWin.getFontMetrics(headerFont).stringWidth(pointsToWin.getText()) + 20, difficultyHeader.getHeight());
		pointsToWin.setFont(headerFont);
		pointsToWin.setForeground(Color.magenta.darker());
		pointsToWinSlider = new JSlider(-1, 50, 21);
		pointsToWinSlider.setFont(labelFont);
		pointsToWinSlider.setBackground(topPanel.getBackground());
		pointsToWinSlider.setForeground(Color.black);
		pointsToWinSlider.setBounds(speedSlider.getX(), pointsToWin.getY(), topPanel.getWidth() / 2, (int)(pointsToWin.getHeight() * 1.5));
		pointsToWinSlider.setMajorTickSpacing(5);
		pointsToWinSlider.setMinorTickSpacing(1);
		pointsToWinSlider.setPaintTicks(true);
		pointsToWinSlider.setPaintLabels(true);
		pointsToWinSlider.setSnapToTicks(true);
		pointsToWinSlider.setValue(this.pointsToWin);
		topPanel.add(pointsToWin);
		topPanel.add(pointsToWinSlider);
		pointsToWin.setVisible(true);
		pointsToWinSlider.setVisible(true);
		
		// Add the bottom panel's buttons
		buttonFont = new Font("buttonFont", Font.PLAIN, bottomPanel.getHeight() / 4);
		
		resumeGame = new JButton("Resume Game");
		resumeGame.setFont(buttonFont);
		resumeGameWidth = bottomPanel.getWidth() / 5;
		resumeGameHeight = bottomPanel.getHeight() * 3 / 5;
		resumeGame.setBounds(bottomPanel.getWidth() - resumeGameWidth - 10, (bottomPanel.getHeight() - resumeGameHeight) / 2, resumeGameWidth, resumeGameHeight);
		bottomPanel.add(resumeGame);
		resumeGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeOptions();
				paused = false;
			}
		});
		resumeGame.setVisible(true);
		if (!playingGame)
			resumeGame.setEnabled(false);
		
		newGame = new JButton("New Game");
		newGame.setFont(buttonFont);
		newGameWidth = resumeGameWidth;
		newGameHeight = resumeGameHeight;
		newGame.setBounds(resumeGame.getLocation().x - 10 - newGameWidth, (bottomPanel.getHeight() - newGameHeight) / 2, newGameWidth, newGameHeight);
		bottomPanel.add(newGame);
		newGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playingGame = false;
				
				if (playersCheckBox.isSelected())
					aiOnly = true;
				else
					aiOnly = false;
				
				Game.this.pointsToWin = pointsToWinSlider.getValue();
				newGame(difficulty);
				ball.setMaxSpeed(speedSlider.getValue());
				closeOptions();
			}
		});
		newGame.setVisible(true);
		
		quit = new JButton("Quit Game");
		quit.setFont(buttonFont);
		quit.setBounds(10, (bottomPanel.getHeight() - resumeGameHeight) / 2, resumeGameWidth, resumeGameHeight);
		bottomPanel.add(quit);
		quit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playingGame = false;
				stopGameThread();
				closeOptions();
				frame.dispose();
				System.exit(0);
			}
		});
		quit.setVisible(true);
		
		frame.repaint();
		optionsFrame.repaint();
	}
	
	public void closeOptions() {
		if (frame != null)
			frame.setCursor(frame.getToolkit().createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), null));
		optionsFrame.dispose();
		frame.requestFocus();
		optionsOpened = false;
		paused = false;
		playingGame = true;
	}
	
	// Creates and starts the game thread
	public void gameThread() {
		gameThread = new Thread(new Runnable() {
			public void run() {
				long lastTime = System.nanoTime();
				long currentTime = System.nanoTime();
				long timeDifference = 0L;
				long delta = 0L; // tracks elapsed nanoseconds
				
				do {
					double scaleX = Math.sin(Math.toRadians((double)ball.getAngle())); // sin and cosine give opposite answers
					double scaleY = Math.cos(Math.toRadians((double)ball.getAngle())); // sin and cosine together should always add up to 1
					
					lastTime = currentTime;
					currentTime = System.nanoTime();
					timeDifference = currentTime - lastTime;
					delta += timeDifference;
					Game.delta++; // add to the frames which have elapsed (this is zeroed out in tick())
					
					if (!paused) {
							
						// Move the player's paddle
						if (playerPaddle != null && !aiOnly) {
							if (!paddleMoveLockX)
								playerPaddle.setLocationX((double)mouseX);
							if (!paddleMoveLockY)
								playerPaddle.setLocationY((double)mouseY);						
						}
						
						// Let the AI engine run
						ai.process(timeDifference);
						if (aiOnly)
							ai2.process(timeDifference);
						
						double newLocationX = ball.getLocationX() + ((timeDifference) / 1000000000.0 * (ball.getSpeed() * scaleX));
						double newLocationY = ball.getLocationY() + ((timeDifference) / 1000000000.0 * (ball.getSpeed() * scaleY));
						
						ball.setLocationX(newLocationX);
						ball.setLocationY(newLocationY);
						
						// Check for ball collisions with the paddles
						//if (ball.checkCollision(playerPaddle.getCollisionBox()))
						if (ball.checkCollisionWithPaddle(playerPaddle))
							ball.bounce(playerPaddle.getDirection(), playerPaddle.getSpeed());
						
						//if (ball.checkCollision(aiPaddle.getCollisionBox()))
						if (ball.checkCollisionWithPaddle(aiPaddle))
							ball.bounce(aiPaddle.getDirection(), aiPaddle.getSpeed());
						
						// Check for wall collisions
						Direction collision = ball.checkWallCollision();
						
						if (collision == playerWallToProtect) {
							if (pointCooldownTimer >= (long)((double)pointCooldown * 1000000000.0)) {
								pointCooldownTimer = 0L;
								score2++;
								if (score2 >= pointsToWin && score2 - score1 >= 2 && pointsToWin >= 0) {
									playingGame = false;
									JOptionPane.showMessageDialog(frame, "Player 2 won the game with a score of " + String.valueOf(score2) + "!", "Game Over!!!", JOptionPane.PLAIN_MESSAGE);
									openOptions();
								}
							}
						}
						
						if (collision == aiWallToProtect) {
							if (pointCooldownTimer >= (long)((double)pointCooldown * 1000000000.0)) {
								pointCooldownTimer = 0L;
								score1++;
								if (score1 >= pointsToWin && score1 - score2 >= 2 && pointsToWin >= 0) {
									playingGame = false;
									if (difficulty != Difficulty.HACKING)
										JOptionPane.showMessageDialog(frame, "Player 1 won the game with a score of " + String.valueOf(score1) + "!", "Game Over!!!", JOptionPane.PLAIN_MESSAGE);
									else
										JOptionPane.showMessageDialog(frame, "You may have scored " + String.valueOf(score1) + " points, but...\nYOU STILL LOSE!!!!", "YOU STILL LOSE SUCKER!!!", JOptionPane.PLAIN_MESSAGE);
									openOptions();
								}
							}
						}
						
						// Check that the ball hasn't glitched outside the screen
						if (ball.getLocationX() < -200 || ball.getLocationX() > Main.screenSize.width + 200 ||
								ball.getLocationY() < -200 || ball.getLocationY() > Main.screenSize.height + 200) {
							ball.setLocationX((Main.screenSize.getWidth() / 2) + ball.getRadius());
							ball.setLocationY((Main.screenSize.getHeight() / 2) + ball.getRadius());
						}
						
						// Check if delta has accumulated to the time between ticks
						// If so, cause a tick to occur
						if (delta >= (1.0 / TICKS_PER_SECOND) * 1000000000) {
							tick();
							delta -= (1.0 / TICKS_PER_SECOND) * 1000000000;
						}
						
						repaint();
					}
				} while (playingGame);
				
				try {
					gameThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		gameThread.start();
	}
	
	// Joins (stops) the game thread
	public void stopGameThread() {
		if (gameThread != null)
			playingGame = false;
	}
	
	// Checks if a bounce is legal currently
	public static boolean canBounce() {
		boolean canBounce = false;
		
		if ((float)ticksSinceLastBounce / (float)TICKS_PER_SECOND >= bounceTimeBuffer)
			canBounce = true;
		
		return canBounce;
	}

	@Override
	public void mouseDragged(MouseEvent e) {}

	public void mouseMoved(MouseEvent e) {
		if (!aiOnly) {
			int x = e.getXOnScreen();
			int y = e.getYOnScreen();
			int newX = 0;
			int newY = 0;
			
			// Set the mouseX value so long as it is within the bounds specified by widthBuffer
			if (playerPaddle != null) {
				if (x >= widthBuffer && x <= pixelBoundX + widthBuffer - playerPaddle.getWidth())
					newX = x;
				else if (x < widthBuffer)
					newX = widthBuffer;
				else if (x > pixelBoundX + widthBuffer - playerPaddle.getWidth())
					newX = pixelBoundX + widthBuffer - playerPaddle.getWidth();
				
				// Set the mouseY value so long as it is within the bounds specified by heightBuffer
				if (y >= heightBuffer && y <= pixelBoundY + heightBuffer - playerPaddle.getHeight())
					newY = y;
				else if (y < heightBuffer)
					newY = heightBuffer;
				else if (y > pixelBoundY + heightBuffer - playerPaddle.getHeight())
					newY = pixelBoundY + heightBuffer - playerPaddle.getHeight();
				
				// See if the newX and newY values differ from MouseX and MouseY before changing
				if (mouseX != newX)
					mouseX = newX;
				if (mouseY != newY)
					mouseY = newY;
			}
		}
	}
	
	// Paints the JPanel
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		String visualFPS;
		
		if (playingGame) {
			if (fps > visualFPSCap)
				visualFPS = "500+";
			else
				visualFPS = String.valueOf(fps);
			
			// Make sure this code is not run before the components being drawn are initialized
			if (playerPaddle != null && aiPaddle != null && ball != null) {
				// Paint the background
				g.setColor(Color.darkGray);
				g.fillRect(0, 0, Main.screenSize.width, Main.screenSize.height);
				
				// Paint the player Paddle
				g.setColor(playerPaddle.getColor());
				if (difficulty == Difficulty.IMPOSSIBLE)
					g.drawRect((int)playerPaddle.getLocationX(), (int)playerPaddle.getLocationY(), playerPaddle.getWidth(), playerPaddle.getHeight());
				else if (difficulty != Difficulty.HACKING || aiOnly)
					g.fillRect((int)playerPaddle.getLocationX(), (int)playerPaddle.getLocationY(), playerPaddle.getWidth(), playerPaddle.getHeight());
				
				// Paint the AI paddle
				g.setColor(aiPaddle.getColor());
				g.fillRect((int)aiPaddle.getLocationX(), (int)aiPaddle.getLocationY(), aiPaddle.getWidth(), aiPaddle.getHeight());
				
				// Paint the ball
				g.setColor(ball.getBorderColor());
				g.fillOval((int)(ball.getLocationX()), (int)(ball.getLocationY()), (ball.getRadius() + ball.getBorderThickness()) * 2, (ball.getRadius() + ball.getBorderThickness()) * 2);
				g.setColor(ball.getColor());
				g.fillOval((int)(ball.getLocationX()), (int)(ball.getLocationY()), ball.getRadius() * 2, ball.getRadius() * 2);
			}
			
			// Write the fps in the top left hand corner
			g.setColor(Color.yellow);
			g.setFont(new Font("subtitleFont", Font.ITALIC, Main.screenSize.width / 80));
			g.drawString("FPS: " + visualFPS, 10, 10 + g.getFont().getSize());
			
			// Write the ball's speed in the top left hand corner
			g.setColor(Color.yellow);
			g.drawString("Speed: " + ball.getSpeed(), Main.screenSize.width - (g.getFontMetrics().stringWidth("Speed: " + ball.getSpeed())) - 10, 10 + g.getFont().getSize());
			g.drawString("Max Speed: " + ball.getMaxSpeed(), Main.screenSize.width - (g.getFontMetrics().stringWidth("Max Speed: " + ball.getMaxSpeed())) - 10, (10 + g.getFont().getSize() * 2));
			
			// Write the difficulty level in the middle
			g.setColor(Color.cyan);
			g.setFont(new Font("titleFont", Font.BOLD, Main.screenSize.width / 60));
			g.drawString("DIFFICULTY: " + difficulty.name(), (Main.screenSize.width / 2) - (g.getFontMetrics().stringWidth("DIFFICULTY: " + difficulty.name()) / 2), g.getFont().getSize() + 10);
			
			// Write the current scores
			g.setColor(Color.YELLOW.darker());
			g.setFont(new Font("scoreFont", Font.BOLD, Main.screenSize.width / 80));
			g.drawString("Points to Win: " + pointsToWin, (Main.screenSize.width / 2) - (g.getFontMetrics().stringWidth("Points to Win: ") / 2),  g.getFont().getSize() + 50);
			g.drawString(ai2Name + ": " + score1 + "  |  " + aiName + ": " + score2, (Main.screenSize.width / 2) - (g.getFontMetrics().stringWidth(ai2Name + ": " + score1 + "  |  " + aiName + ": " + score2) / 2),  (g.getFont().getSize() * 2) + 60);
			
		}
	}
	
	//____________________________________________________________________________________________
	// Private Methods
	//____________________________________________________________________________________________
	
	// Initializes components for the game
	private void init() {
		// Check that variables are what they should be
		if (ticksPerDirectionUpdate <= 0) {
			ticksPerDirectionUpdate = 1;
			JOptionPane.showConfirmDialog(frame, "Error in ticksPerHalfSecond variable: math caused it to be <= 0.\nSetting variable to 1.", "Math Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
		}
		
		// Initialize the JFrame
		frame = new JFrame();
		frame.setTitle(Main.title);
		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(Main.screenSize);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setState(JFrame.MAXIMIZED_BOTH);
		frame.add(this);
		frame.addKeyListener(this);
		frame.setVisible(true);
		frame.setCursor(frame.getToolkit().createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), null));
		
		// The screen size is not the same as the size of the drawable area in the JFrame.
		// To fix the pixelBoundY to reflect the drawable area, the frame's top inset must be subtracted
		// after the frame is initialized.
		pixelBoundY -= frame.getInsets().top;
		
		this.setVisible(true);
		this.addMouseMotionListener(this);
		
		newGame(difficulty);
		openOptions();
	}
	
	// Called by gameThread() once every tick
	private void tick() {
		// FPS and tick calculations
		currentTick++;
		ticksSinceLastSecond++;
		ticksSinceLastBounce++;
		
		if (currentTick > MAX_TICK)
			currentTick = 0;
		
		if (ticksSinceLastSecond >= TICKS_PER_SECOND) {
			ticksSinceLastSecond = 0;
			fps = delta;
			delta = 0;
		}
		
		if (ticksSinceLastBounce <= (int)(bounceTimeBuffer * (float)TICKS_PER_SECOND))
			ticksSinceLastBounce++;
		
		if ((double)pointCooldownTimer / 1000000000.0 < (double)pointCooldown)
			pointCooldownTimer += (long)(1.0 / TICKS_PER_SECOND * 1000000000.0);
		
		// Check movement direction of the paddles once every 1/2 a second
		// This allows the ball's direction to be changed by the player
		// Need to use currentTick not ticksSinceLastSecond (see variable declaration comment)
		if (currentTick % ticksPerDirectionUpdate == 0) {
			playerPaddle.detectAndSetDirectionAndSpeed();
			aiPaddle.detectAndSetDirectionAndSpeed();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getExtendedKeyCode() == KeyEvent.VK_ESCAPE) {
			if (!optionsOpened)
				openOptions();
			else
				closeOptions();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}
}
