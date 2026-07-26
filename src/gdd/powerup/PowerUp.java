/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package gdd.powerup;

	import gdd.ImageUtil;
	import gdd.sprite.Player;
	import gdd.sprite.Sprite;
	import java.awt.Image;

	abstract public class PowerUp extends Sprite {

		private Image[] spinFrames;
		private int currentFrame = 0;
		private int animationCounter = 0;
		private int animationSpeed = 3;

		public PowerUp(int x, int y) {
			this.x = x;
			this.y = y;
		}

		// Helper to load frames
		protected void loadFrames(String[] framePaths, int width, int height) {
			if (framePaths == null || framePaths.length == 0) return;
			
			spinFrames = new Image[framePaths.length];
			for (int i = 0; i < framePaths.length; i++) {
				spinFrames[i] = ImageUtil.fit(framePaths[i], width, height);
			}
			setImage(spinFrames[0]);
		}

		// Helper to advance to the next frame
		protected void updateAnimation() {
			if (spinFrames == null || spinFrames.length == 0) return;

			animationCounter++;
			if (animationCounter >= animationSpeed) {
				animationCounter = 0;
				currentFrame = (currentFrame + 1) % spinFrames.length;
				setImage(spinFrames[currentFrame]);
			}
		}

		abstract public void upgrade(Player player);
	}