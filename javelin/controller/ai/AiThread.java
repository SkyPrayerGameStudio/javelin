package javelin.controller.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javelin.controller.exception.StopThinking;
import javelin.model.state.BattleState;

/**
 * Efficiently uses a CPU core to run an {@link AlphaBetaSearch}. When the
 * search is done it starts a new one using the same thread, to avoid creating
 * more than threads than there are CPUs available.
 * 
 * @author alex
 */
public class AiThread extends Thread {
	public static final TreeMap<Integer, List<ChanceNode>> FINISHED = new TreeMap<Integer, List<ChanceNode>>();
	static final ArrayList<Thread> STARTED = new ArrayList<Thread>();

	public static ThreadGroup group = regroup();
	/**
	 * @see #setdepth()
	 */
	static int depthincremeneter;

	public BattleState state;
	public int depth;
	List<ChanceNode> result;

	public AiThread(BattleState state) {
		super(group, (Runnable) null);
		this.state = state.clonedeeply();
		setdepth();
		start();
	}

	public static ThreadGroup regroup() {
		return new ThreadGroup("Javelin AI threads");
	}

	@Override
	public void run() {
		try {
			result = new BattleAi(depth).alphaBetaSearch(state);
			onend();
		} catch (StopThinking e) {
			// abort
		} catch (Exception e) {
			ThreadManager.ERROR = e;
		}
	}

	/**
	 * Synchronously assigns a new {@link #depth} to this thread.
	 */
	synchronized public void setdepth() {
		depth = depthincremeneter;
		setName("Javelin AI " + depth);
		depthincremeneter += 1;
	}

	synchronized private void onend() {
		if (Thread.interrupted()) {
			return;
		}
		AiThread.FINISHED.put(depth, result);
		if (!ThreadManager.interrupting && ThreadManager.working
				&& depthincremeneter <= ThreadManager.MAXIMUMDEPTH) {
			setdepth();
			run();
		}
	}

	static public void checkinterrupted() {
		if (Thread.interrupted() || !ThreadManager.working
				|| ThreadManager.interrupting) {
			if (((AiThread) Thread.currentThread()).depth != 1) {
				throw new StopThinking();
			}
		}
	}

	public static synchronized void reset() {
		ThreadManager.interrupt();
		AiThread.FINISHED.clear();
		AiThread.STARTED.clear();
	}
}