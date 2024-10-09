import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

// Walk1.java has a JPanel and JFrame
// that allows Timer Action events

public class Raycaster extends JPanel implements ActionListener {

	// Window Variables
	private static int xmin, xmax, ymax;
	private Timer timer;
	private static boolean showMiniMap = false;

	private static boolean wadeLoaded;
	// Player variables
	private static double px = 96, py = 96, pa = .1, pz = 0, pzv = 0, gravity = .0012;
	private static double pdx = Math.cos(pa), pdy = Math.sin(pa);

	private static double speed = 8;

	// WADE variables
	private static int[][] level;
	private static int[] palette = { 0xFFFFFF, 0xFF0000, 0xFFA500, 0xFFFF00, 0x00FF00, 0x0000FF, 0x4B0082, 0x7F00FF,
			0x23B0FF, 0x000000 };
	private static ArrayList<int[][]> walls = new ArrayList<>(); 	
	private static int mapS = 1;

	private static int floor = 0;
	private static int sky = 0;
	private static double fov = 90;
	private static double scale = 800;

	public Raycaster() {
		xmin = 0;
		xmax = 600;
		ymax = 600;

		timer = new Timer(16, this);
		timer.start();
		int[][] wall = {
				{ 1 }
		};
		for (int i = 0; i < 256; i++) {
			walls.add(wall);
		}
	}

	public static void main(String[] args) throws Exception {
		wadeLoaded = false;
		// Launch Window
		JFrame f = new JFrame();
		Raycaster p = new Raycaster();

		f.setTitle("Raycaster");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		f.setSize(600, 600);
		f.add(p);

		// Controls
		f.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				pdx = Math.cos(pa) * speed;
				pdy = Math.sin(pa) * speed;

				int keyCode = e.getKeyCode();
				if (KeyEvent.getKeyText(keyCode).equals("W")) {
					if (level[(int) Math.floor(py + pdy) >> 6][(int) Math.floor(px + pdx) >> 6] == 0) {
						px += pdx;
						py += pdy;
					}
				}

				if (KeyEvent.getKeyText(keyCode).equals("A")) {
					if (level[(int) Math.floor(py - pdx) >> 6][(int) Math.floor(px + pdy) >> 6] == 0) {
						px += pdy;
						py -= pdx;
					}
				}

				if (KeyEvent.getKeyText(keyCode).equals("S")) {
					if (level[(int) Math.floor(py - pdy) >> 6][(int) Math.floor(px - pdx) >> 6] == 0) {
						px -= pdx;
						py -= pdy;
					}
				}

				if (KeyEvent.getKeyText(keyCode).equals("D")) {
					if (level[(int) Math.floor(py + pdx) >> 6][(int) Math.floor(px - pdy) >> 6] == 0) {
						px -= pdy;
						py += pdx;
					}
				}

				if (KeyEvent.getKeyText(keyCode).equals("Left")) {
					pa -= .1;
				}

				if (KeyEvent.getKeyText(keyCode).equals("Right")) {
					pa += .1;
				}

				if (KeyEvent.getKeyText(keyCode).equals("Space")) {
					pzv = .5;
				}

				if (KeyEvent.getKeyText(keyCode).equals("M")) {
					showMiniMap = !showMiniMap;
				}

			}
		});

		// Initialize the base wall
		// walls.add({{1}});
		// walls.add(
		// {
		// {1, 1},
		// {1, 1}
		// });

		// Load WADE file
		p.loadWade(new File(args[0]));
		wadeLoaded = true;

	}

	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	public void loadWade(File file) throws Exception {
		wadeLoaded = false;
		Scanner scan = new Scanner(file);

		if (!scan.nextLine().equals("This is a WADE file! Not a PNG!")) {
			throw new Exception("Not a WADE file!");
		}

		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			String[] tokens = line.split("\\s+");

			if (tokens[0].equals("level")) {
				level = new int[Integer.parseInt(tokens[2])][Integer.parseInt(tokens[1])];
				for (int i = 0; i < Integer.parseInt(tokens[2]); i++) {
					String a = scan.nextLine();

					for (int ii = 0; ii < Integer.parseInt(tokens[1]); ii++) {
						level[i][ii] = Integer.parseInt(a.substring(ii * 2, ii * 2 + 2), 16);
					}
				}

				line = scan.nextLine();
			} else if (tokens[0].equals("wall")) {
				int n = Integer.parseInt(tokens[1], 16);
				int w = Integer.parseInt(tokens[2]);
				int h = Integer.parseInt(tokens[3]);
				int[][] wall = new int[h][w];

				for (int i = 0; i < h; i++) {
					String s = scan.nextLine();
					for (int ii = 0; ii < w; ii++) {
						wall[i][ii] = Integer.parseInt(s.substring(ii, ii + 1));
					}
				}

				walls.set(n, wall);
			} else if (tokens[0].equals("start")) {
				px = Integer.parseInt(tokens[1]) * 64 + 32;
				py = Integer.parseInt(tokens[2]) * 64 + 32;
				pa = Double.parseDouble(tokens[3]);
			} else if (tokens[0].equals("floor")) {
				floor = Integer.parseInt(tokens[1]);
			} else if (tokens[0].equals("sky")) {
				sky = Integer.parseInt(tokens[1]);
			} else if (tokens[0].equals("palette")) {
				boolean endOfPalette = false;
				while (!endOfPalette) {
					String paletteLine = scan.nextLine();

					if (paletteLine.equals("endpalette")) {
						endOfPalette = true;
					} else {
						String[] paletteTokens = paletteLine.split("\\s+");
						palette[Integer.parseInt(paletteTokens[0], 16)] = Integer.parseInt(paletteTokens[1], 16);
					}
				}
			} else if (tokens[0].equals("fov")) {
				fov = Double.parseDouble(tokens[1]);
			}
		}

		scan.close();
		wadeLoaded = true;
	}

	public void paint(Graphics g) {

		// Settings variables
		
		// Clear
		g.clearRect(0, 0, xmax, ymax);

		if (showMiniMap) {
			
		} else {
			// Floor and Sky Drawing
			g.setColor(new Color(palette[floor], false));
			g.fillRect(0, ymax / 2 + (int)(pz), xmax, ymax / 2);

			g.setColor(new Color(palette[sky], false));
			g.fillRect(0, 0, xmax, ymax / 2  + (int)(pz));
		}

		if (wadeLoaded) {
			int res = xmax / 2;
			scale = res / ((level[0].length * level.length) / 100.0);
			double r, mp, dof, vx, vy, hx, hy, rx, ry, ra, xo, yo;
			int mx, my;
			mapS = level.length * level[0].length;
			rx = 0;
			ry = 0;
			xo = 0;
			yo = 0;
			ra = pa - (0.0174 * fov / 2);
			if (ra > 2 * 3.14) {
				ra -= 2 * 3.14;
			}
			if (ra < 0) {
				ra += 2 * 3.14;
			}

			for (r = 0; r < res; r++) {
				boolean horizontalFlipTexture = false;
				boolean verticalFlipTexture = false;
				boolean flipTexture = false;
				// Horizontal Line Check
				dof = 0;
				double hDist = 2000000000;
				hx = px;
				hy = py;
				double aTan = -1 / (Math.tan(ra));
				if (ra > 3.14) {
					ry = Math.floor(py / 64) * 64 - 0.001;
					rx = (py - ry) * aTan + px;
					yo = -64;
					xo = -yo * aTan;
				}
				if (ra < 3.14) {
					ry = Math.floor(py / 64) * 64 + 64;
					rx = (py - ry) * aTan + px;
					yo = 64;
					xo = -yo * aTan;
				}
				if (ra == 0 || ra == 3.1415) {
					rx = px;
					ry = py;
					dof =  Math.max(level.length, level[0].length);
				}

				while (dof < Math.max(level.length, level[0].length)) {
					mx = (int) Math.floor(rx) >> 6;
					my = (int) Math.floor(ry) >> 6;
					mp = my * level[0].length + mx;
					if (my >= 0 && my < level.length && mx >= 0 && mx < level[0].length && level[my][mx] != 0) {
						dof =  Math.max(level.length, level[0].length);
						hx = rx;
						hy = ry;
						hDist = distance(px, py, hx, hy);
						if (yo < 0)
						{
							horizontalFlipTexture = true;
						}
					} else {
						rx += xo;
						ry += yo;
						dof += 1;
					}
				}

				// Vertical Line Check (ha, VLC)
				dof = 0;
				double vDist = 2000000000;
				vx = px;
				vy = py;
				double nTan = -(Math.tan(ra));
				if (ra > 3.14 / 2 && ra < (3.14 / 2) * 3) {
					rx = Math.floor(px / 64) * 64 - 0.001;
					ry = (px - rx) * nTan + py;
					xo = -64;
					yo = -xo * nTan;
				}
				if (ra < 3.14 / 2 || ra > (3.14 / 2) * 3) {
					rx = Math.floor(px / 64) * 64 + 64;
					ry = (px - rx) * nTan + py;
					xo = 64;
					yo = -xo * nTan;
				}
				if (ra == 0 || ra == 3.14) {
					rx = px;
					ry = py;
					dof =  Math.max(level.length, level[0].length);
				}

				while (dof <  Math.max(level.length, level[0].length)) {
					mx = (int) Math.floor(rx) >> 6;
					my = (int) Math.floor(ry) >> 6;
					mp = my * level[0].length + mx;
					if (mp > 0 && mp < level[0].length * level.length && level[my][mx] != 0) {
						dof =  Math.max(level.length, level[0].length);
						vx = rx;
						vy = ry;
						vDist = distance(px, py, vx, vy); 
						if (xo < 0) {
							verticalFlipTexture = true;
						}
					} else {
						rx += xo;
						ry += yo;
						dof += 1;
					}
				}

				double textureShift = 0;
				// Get minimum distance
				if (hDist > vDist) {

					rx = vx;
					ry = vy;
					if (verticalFlipTexture)
					{
						textureShift = 64 - (ry % 64);
					} else {
						textureShift = ry % 64;
					}
				}
				if (hDist < vDist) {
					rx = hx;
					ry = hy;
					if (horizontalFlipTexture)
					{
						textureShift = rx % 64;
					} else {
						textureShift = 64 - (rx % 64);
					}
				}
				g.setColor(new Color(0xFF, 0xFF, (int) (textureShift * 4)));

				double mDist = Math.min(hDist, vDist);

				// Fix warped view
				double ca = pa - ra;
				if (ca < 0) {
					ca += 2 * 3.1415;
				}
				if (ca > 2 * 3.1415) {
					ca -= 2 * 3.1415;
				}
				mDist = mDist * Math.cos(ca);

				// Get height of line
				double lineH = (scale * mapS) / mDist;
				// if (lineH > ymax) {
				// 	lineH = ymax;
				// }

				int x1 = (int) (r * (xmax / res));
				int y1 = (int) (Math.floor((ymax / 2) - (((lineH) / 1) / 2)));
				int w = (int) (Math.floor(xmax / res));
				int h = (int) (lineH);

				// Jumping Math
				pz += pzv;
				pzv = pzv - gravity;

				if (pz < 0)
				{
					pz = 0;
				}

				// Draw wall
				int[][] wall = walls.get(level[(int) Math.floor(ry) >> 6][(int) Math.floor(rx) >> 6]);
				int column = (int) (textureShift / 64 * wall[0].length);

				int y1d = y1;
				double pixelSize = (double) h / wall.length;

				for (int i = 0; i < wall.length; i++) {
					Color c = new Color(palette[wall[i][column]]);
					g.setColor(c);

					int startY = y1 + (int) (i * pixelSize);
					int endY = y1 + (int) ((i + 1) * pixelSize);

					g.fillRect(x1, startY + (int)pz, w, endY - startY);
				}

				// Increment ray angle
				ra += 0.0174 * (fov / res);
				if (ra > 2 * 3.14) {
					ra -= 2 * 3.14;
				}
				if (ra < 0) {
					ra += 2 * 3.14;
				}

			}

		}

		if (showMiniMap)
		{
			g.clearRect(0, 0, xmax, ymax);
			int mapScale = Math.min(xmax, ymax) / Math.max(level.length, level[0].length);
			for (int i = 0; i < level.length; i++) {
				for (int ii = 0; ii < level[i].length; ii++) {
					if (level[i][ii] != 0) {
						g.setColor(Color.BLACK);
						g.fillRect(mapScale * ii, mapScale * i, mapScale, mapScale);
					} else {
						g.setColor(Color.WHITE);
						g.fillRect(mapScale * ii, mapScale * i, mapScale, mapScale);
					}
				}

				g.setColor(Color.RED); // Player color
				g.fillOval((int) (px / 64 * mapScale), (int) (py / 64 * mapScale), mapScale, mapScale);
			}
		}
	}

	private void drawRays(Graphics g) {
		int cellSize = Math.min(xmax, ymax) / Math.max(level.length, level[0].length); // Size of each cell in the map
		g.setColor(Color.BLUE); // Ray color

		double raTemp = pa - (0.0174 * 90 / 2); // Starting ray angle
		for (int r = 0; r < xmax; r++) {
			double rx = px;
			double ry = py;
			double xo = Math.cos(raTemp) * cellSize;
			double yo = Math.sin(raTemp) * cellSize;

			for (int i = 0; i < Math.max(level.length, level[0].length); i++) { // Limit the ray length for the mini-map
				rx += xo;
				ry += yo;
				if (level[(int) ry / 64][(int) rx / 64] != 0) {
					break; // Stop drawing the ray if it hits a wall
				}
			}

			g.drawLine((int) px / 64 * cellSize + cellSize / 2, (int) py / 64 * cellSize + cellSize / 2,
					(int) rx / 64 * cellSize, (int) ry / 64 * cellSize);

			raTemp += 0.0174 * (fov / xmax); // Increment the ray angle
			if (raTemp > 2 * 3.14) {
				raTemp -= 2 * 3.14;
			}
			if (raTemp < 0) {
				raTemp += 2 * 3.14;
			}
		}
	}

	public void actionPerformed(ActionEvent a) {

		xmax = (int) getSize().getWidth();
		ymax = (int) getSize().getHeight();

		repaint();
	}
}
