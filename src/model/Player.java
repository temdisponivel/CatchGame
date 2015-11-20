package model;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.Random;

public class Player implements Serializable {
	private static final long serialVersionUID = 1L;
	public int x = new Random().nextInt(800);
	public int y = new Random().nextInt(600);
	public int radius = 10;
	public Color color = Color.BLACK;
	private int hash = (int) (System.nanoTime() * (new Random().nextInt() << new Random().nextInt(8))); 
	public boolean dead = false;
	
	public Player() {
		Random r = new Random();
		this.color = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255), 255);
	}
	
	public void draw(Graphics g) {
		Color aux = g.getColor();
		g.setColor(color);
		g.fillOval(x, y, this.radius, this.radius);
		g.setColor(aux);
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.hashCode() == obj.hashCode();
	}
	
	@Override
	public int hashCode() {
		return this.hash;
	}
}
