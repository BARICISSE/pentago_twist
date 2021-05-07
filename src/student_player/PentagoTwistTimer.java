package student_player;

import java.util.Timer;
import java.util.TimerTask;

public class PentagoTwistTimer {
	Timer timer;
	boolean timeout;

	public PentagoTwistTimer(int milliseconds) {
		timer = new Timer();
		timer.schedule(new PentagoTwistTask(), milliseconds);
	}

	private class PentagoTwistTask extends TimerTask {
		public void run() {
			timeout = true;
			timer.cancel();
		}
	}
}
