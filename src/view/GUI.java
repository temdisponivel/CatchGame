package view;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class GUI extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	public GUI() {
		Dimension d = new Dimension(800, 600);
		Surface s;
		this.setSize(d);
		this.setTitle("CATCH GAME");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.add(s = new Surface(d));
		this.setVisible(true);
		this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
            	s.finish();
                System.exit(0);
            }
        } );
	}
}