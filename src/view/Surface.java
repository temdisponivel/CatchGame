package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.JPanel;
import service.BaseService;
import service.Message;
import service.Service;
import service.ServiceListener;
import model.Player;

public class Surface extends JPanel implements ActionListener, ServiceListener {
	private static final long serialVersionUID = 1L;
	private LinkedList<Player> players = new LinkedList<Player>();	
	private Player player = new Player();
	private Service service;
	private Dimension size;
	private boolean closing = false;
	
	public Surface(Dimension size) {
		this.size = size;
		this.addKeyListener(new KeyAdapter()  {
            public void keyPressed(KeyEvent e){
            	MoveObject(e);
             }
         });
		this.setBackground(Color.WHITE);
		this.addPlayer(player);
		this.requestFocus();
		
		try {
			this.service = new BaseService(InetAddress.getByName("239.0.0.0"), 1997, this);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		this.sendMessage();
	}
	
	public void MoveObject(KeyEvent e) {
		 int keyCode = e.getKeyCode();		 
		 int offset = 5;
		 switch(keyCode) { 
	        case KeyEvent.VK_UP:
	        	this.player.y -= offset;
	            break;
	        case KeyEvent.VK_DOWN:
	        	this.player.y += offset;
	            break;
	        case KeyEvent.VK_LEFT:
	        	this.player.x -= offset;
	            break;
	        case KeyEvent.VK_RIGHT :
	        	this.player.x += offset;
	            break;
		 }
		 if (this.player.x < 0) {
			 this.player.x = this.size.width - 1;
		 }
		 if (this.player.y < 0) {
			 this.player.y = this.size.height - 1;
		 }
		 this.player.x = this.player.x % this.size.width;
		 this.player.y = this.player.y % this.size.height;
		 repaint();
		 this.sendMessage();
	}
	
	/**
	 * Send the player to all other players.
	 * @param player Player to send.
	 */
	private void sendMessage(Player player) {
		Message<Player> message;
		try {
			message = new Message<Player>(player);
			this.service.send(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Short hand for sendMessage passing the player that is playing from this instance.
	 */
	private void sendMessage() {
		this.sendMessage(this.player);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.repaint();
	}
	
    public void paint(Graphics g) {
		this.requestFocus();
        super.paintComponent(g);
        for (Player player : this.players) {
        	player.draw(g);
        }
        g.setColor(Color.red);
        g.drawString("YOU", this.player.x, this.player.y);
    }
	
	/**
	 * Add a player to paint in surface.
	 * @param d player to add.
	 * @return this reference to chain calls.
	 */
	public Surface addPlayer(Player d) {
		this.players.add(d);
		return this;
	}
	
	/**
	 * Removes a player.
	 * @param d Player to remove.
	 * @return this reference to chain calls. 
	 */
	public Surface removePlayer(Player d) {
		this.players.remove(d);
		return this;
	}

	@Override
	public void receive(DatagramPacket packet, Message<?> receive) {
		try {
			Player playerReceived = (Player) receive.getData();
			if (playerReceived.dead) {
				this.removePlayer(playerReceived);
				if (playerReceived.equals(this.player)) {
					if (this.closing) {
						return;
					}
					this.addPlayer(this.player = new Player());
					this.sendMessage();
				}
			}
			else if (this.players.contains(playerReceived)) {
				Player player = (Player) this.players.get(this.players.indexOf(playerReceived));
				player.x = playerReceived.x;
				player.y = playerReceived.y;
				player.color = playerReceived.color;
				player.radius = playerReceived.radius;
			}
			else {
				this.players.add(playerReceived);
				this.sendMessage();
			}
			this.repaint();
			this.validateColision();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Validate all collision between players.
	 */
	protected void validateColision() {
		for (int i = 0; i < this.players.size(); i++) {
			for (int k = i + 1; k < this.players.size(); k++) {
				Player one = this.players.get(i);
				Player two = this.players.get(k);
				if (!this.collapse(one, two)) {
					continue;
				}
				if (one.radius > two.radius) {
					two.dead = true;
					one.radius += two.radius * 0.1 ;
				}
				else if (one.radius < two.radius) {
					one.dead = true;
					two.radius += one.radius * 0.1;
				}
				else {
					boolean whitch = new Random().nextBoolean();
					if (whitch) {
						one.dead = true;
						two.radius += one.radius * 0.1;
					}
					else {
						two.dead = true;
						one.radius += two.radius * 0.1;
					}
				}
				this.sendMessage(one);
				this.sendMessage(two);
			}
		}
	}
	
	/**
	 * Validate if two players are collapsing.
	 */
	protected boolean collapse(Player first, Player second) {
		return new Rectangle(first.x, first.y, first.radius, first.radius).intersects(new Rectangle(second.x, second.y, second.radius, second.radius));
	}
	
	/**
	 * Terminate this surface and free resource.
	 */
	public void finish() {
		this.closing = true;
		this.player.dead = true;
		this.sendMessage();
		this.service.stop();
	}
}
