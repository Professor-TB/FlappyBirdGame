package game;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

	int boardWidth = 360;
	int boardHeight = 640;
	
	// IMAGES
	Image bgImg;
	Image birdImg;
	Image topPipeimg;
	Image bottomPipeimg;
	Image restart;

	// BIRD
	int birdX = boardWidth / 8;
	int birdY = boardHeight / 2;
	int birdWidth = 34;
	int birdHeight = 24;

	class Bird {
		int x = birdX;
		int y = birdY;
		int width = birdWidth;
		int height = birdHeight;

		Image img;

		Bird(Image img) {
			this.img = img;
		}
	}

	// Pipes
	int pipeX = boardWidth;
	int pipeY = 0;
	int pipeWidth = 64;
	int pipeHeight = 512;

	class Pipe {
		int x = pipeX;
		int y = pipeY;
		int width = pipeWidth;
		int height = pipeHeight;
		Image img;
		boolean passed = false;

		Pipe(Image img) {
			this.img = img;
		}
	}

	// Game Logic
	Bird bird;
	int velocityX = -4;
	int velocityY = 0;
	int gravity = 1;

	ArrayList<Pipe> pipes;
	Random random = new Random();

	Timer gameLoop;
	Timer placePipesTimer;

	boolean gameOver = false;
	double score = 0;
	boolean iconPressed = false;
	double highScore= ScoreStorage.loadHighScore();
	boolean inMenu = true; // Added to track the menu state

	public FlappyBird() {
		setPreferredSize(new Dimension(boardWidth, boardHeight));
//		setBackground(Color.BLUE);
		setFocusable(true);
		addKeyListener(this);

		// LOAD IMG
		bgImg = new ImageIcon(getClass().getClassLoader().getResource("img/flappybirdbg.png")).getImage();
		birdImg = new ImageIcon(getClass().getClassLoader().getResource("img/flappybird.png")).getImage();
		topPipeimg = new ImageIcon(getClass().getClassLoader().getResource("img/toppipe.png")).getImage();
		bottomPipeimg = new ImageIcon(getClass().getClassLoader().getResource("img/bottompipe.png")).getImage();
		restart = new ImageIcon(getClass().getClassLoader().getResource("img/restart.png")).getImage();
		JLabel reset = new JLabel(new ImageIcon(restart));
		reset.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				iconPressed = true;
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}
		});

		// bird
		bird = new Bird(birdImg);
		pipes = new ArrayList<Pipe>();

		// place pipe timer
		placePipesTimer = new Timer(1500, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				placePipes();
			}

		});
		placePipesTimer.start();

		// game timer
		gameLoop = new Timer(1000 / 60, this);
		gameLoop.start();
	}

	public void placePipes() {
		/*
		 * (0-1) * pipeHeight/2 -> (0-256) 128 0-128 -(0-256) --> 1/4 pipeHeight --> 3/4
		 * pipeHeight
		 */

		int randomPipeY = (int) (pipeY - pipeHeight / 4 - (Math.random() * pipeHeight / 2));
		int openSpace = boardHeight / 4;

		Pipe topPipe = new Pipe(topPipeimg);
		topPipe.y = randomPipeY;
		pipes.add(topPipe);

		Pipe bottomPipe = new Pipe(bottomPipeimg);
		bottomPipe.y = topPipe.y + pipeHeight + openSpace;
		pipes.add(bottomPipe);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	public void draw(Graphics g) {
		// BackGround
		g.drawImage(bgImg, 0, 0, boardWidth, boardHeight, null);

		if (inMenu) {
			// Draw the start menu
			g.setColor(Color.white);
			g.setFont(new Font("Arial", Font.PLAIN, 32));
			g.drawString("Flappy Bird", boardWidth / 5, boardHeight / 4);
			g.setFont(new Font("Arial", Font.PLAIN, 20));
			g.drawString("Press SPACE to Start", boardWidth / 5, boardHeight / 3);
//			g.drawString("Instructions: Tap SPACE to flap", boardWidth / 4, boardHeight / 2);
			return; // Skip drawing the bird and pipes
		}

		// bird
		g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

		// pipes
		for (int i = 0; i < pipes.size(); i++) {
			Pipe pipe = pipes.get(i);
			g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
		}

		// score
		g.setColor(Color.white);
		g.setFont(new Font("Arial", Font.PLAIN, 32));
		if (gameOver) {
			g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
			g.drawImage(restart, (boardWidth / 2) - 10, boardHeight / 2, 20, 20, null);

		} else {
			g.drawString(String.valueOf((int) score), 10, 35);
		}
		g.setFont(new Font("Arial", Font.PLAIN, 22));
		g.drawString("HighScore: " + String.valueOf((int) highScore), 10, 65);
	}

	public void move() {
		// bird
		velocityY += gravity;
		bird.y += velocityY;
		bird.y = Math.max(bird.y, 0);

		// pipes
		for (int i = 0; i < pipes.size(); i++) {
			Pipe pipe = pipes.get(i);
			pipe.x += velocityX;

			// score
			if (!pipe.passed && bird.x > pipe.x + pipe.width) {
				pipe.passed = true;
				score += 0.5; // 0.5 per pipes and there are 2 pipes(top & bottom)
			}
			

			if (collision(bird, pipe)) {
				gameOver = true;
			}
		}
		if (bird.y > boardHeight) {
			gameOver = true;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		move();
		repaint();
		if (gameOver) {
			placePipesTimer.stop();
			gameLoop.stop();
		}

	}

	public boolean collision(Bird b, Pipe p) {
		return b.x < p.x + p.width && // b top left corner didnt reach p top right corner
				b.x + b.width > p.x && // b top right corner passes p top left corner
				b.y < p.y + p.height && // Birds top left corner didnt reach pipes bottom left corner
				b.y + b.height > p.y; /// Birds bottom left corner passes pipes top left corner
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (inMenu) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				inMenu = false; // Start the game
			}
		} else {

			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				velocityY = -9;
				if (gameOver) {
					if (highScore < score) {
						highScore = score;
						ScoreStorage.saveHighScore(highScore);
					}
					// restart game by resetting conditions
					bird.y = birdY;
					velocityY = 0;
					pipes.clear();
					score = 0;
					gameOver = false;
					gameLoop.start();
					placePipesTimer.start();
					
				}
			}
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
