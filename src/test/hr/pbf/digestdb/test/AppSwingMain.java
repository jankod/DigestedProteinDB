package hr.pbf.digestdb.test;

import java.awt.EventQueue;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import javax.swing.JFrame;

import org.apache.commons.lang3.RandomUtils;

import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.DisplayMode;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;

public class AppSwingMain {

	private JFrame frame;
	private DisplayMode dispModeOld;
	private boolean fullscreen;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AppSwingMain window = new AppSwingMain();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Method allows changing whether this window is displayed in fullscreen or
	 * windowed mode.
	 * 
	 * @param fullscreen
	 *            true = change to fullscreen, false = change to windowed
	 */
	public void setFullscreen(boolean fullscreen, JFrame frame) {
		// get a reference to the device.
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		DisplayMode dispMode = device.getDisplayMode();
		// save the old display mode before changing it.
		dispModeOld = device.getDisplayMode();

		if (this.fullscreen != fullscreen) { // are we actually changing modes.
												// change modes.
			this.fullscreen = fullscreen;
			// toggle fullscreen mode
			if (!fullscreen) {
				// change to windowed mode.
				// set the display mode back to the what it was when
				// the program was launched.
				device.setDisplayMode(dispModeOld);
				// hide the frame so we can change it.
				frame.setVisible(false);
				// remove the frame from being displayable.
				frame.dispose();
				// put the borders back on the frame.
				frame.setUndecorated(false);
				// needed to unset this window as the fullscreen window.
				device.setFullScreenWindow(null);
				// recenter window
				frame.setLocationRelativeTo(null);
				frame.setResizable(true);

				// reset the display mode to what it was before
				// we changed it.
				frame.setVisible(true);

			} else { // change to fullscreen.
						// hide everything
				frame.setVisible(false);
				// remove the frame from being displayable.
				frame.dispose();
				// remove borders around the frame
				frame.setUndecorated(true);
				// make the window fullscreen.
				device.setFullScreenWindow(frame);
				// attempt to change the screen resolution.
				device.setDisplayMode(dispMode);
				frame.setResizable(false);
				frame.setAlwaysOnTop(false);
				// show the frame
				frame.setVisible(true);
			}
			// make sure that the screen is refreshed.
			frame.repaint();
		}
	}

	/**
	 * Create the application.
	 */
	public AppSwingMain() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				System.out.println("Got key event!");
				return false;
			}
		});
		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				int keyCode = e.getKeyCode();
				System.out.println(keyCode + " key code");
				System.out.println(KeyEvent.VK_ESCAPE);
				super.keyReleased(e);
			}
		});
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JButton btnKlikniMe = new JButton("Klikni me");
		btnKlikniMe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFullscreen(RandomUtils.nextBoolean(), frame);
			}
		});

		frame.getContentPane().add(btnKlikniMe, BorderLayout.CENTER);
	}

}
