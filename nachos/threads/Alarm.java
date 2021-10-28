package nachos.threads;

import nachos.machine.*;

import java.util.PriorityQueue;

import java.util.Comparator;


/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
	
	//Need a class to store contents of the waitingThread
	//waitingThread has contents of thread and time
	class waitingThread{
		public KThread thread;
		public long time;
		public waitingThread(KThread thread, long time){
			this.thread = thread;
			this.time = time;
			
		}
	}
	
	Comparator<waitingThread> comparator = new threadComparator();
	
	//wait_queue that stores the waitingThread
	//Use default initial capacity 11 and order according to comparator
	private PriorityQueue<waitingThread> wait_queue = new PriorityQueue<waitingThread>(11, comparator);
	
	
	//Comparator class to sort the waitingThreads 
	//Put shorter wait times at front of wait_queue
	//Longer wait times towards the back
	private class threadComparator implements Comparator<waitingThread>{
		public int compare(waitingThread thread_1, waitingThread thread_2) {
			if(thread_1.time > thread_2.time) {
				return 1;
			}
			else if(thread_1.time < thread_2.time) {
				return -1;
			}
			else {
				return 0;
			}
		}
	}
	
	
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
    	//Make current thread yield
    	KThread.currentThread().yield();
    	
    	//Create head from the wait_queue
    	waitingThread head = wait_queue.peek();
    	
    	//Make sure head of queue is not null
    	//Check if time has passed to satisfy waiting period
    	while((head != null) && head.time <= Machine.timer().getTime()) {
    		//Disable interrupt
    		boolean interrupt_status = Machine.interrupt().disable();
    		
    		//Add head to ready queue
    		head.thread.ready();
    		
    		//Remove head from wait_queue
    		wait_queue.remove(head);
    		
    		//Update head to take new head value from wait_queue
    		head = wait_queue.peek();
    		
    		//Restore interrupt
    		Machine.interrupt().restore(interrupt_status);
    	}
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
    	long wakeTime = Machine.timer().getTime() + x;
    	while (wakeTime > Machine.timer().getTime()) {
    		KThread.yield();
    		//Disable interrupt
    		boolean interrupt_status = Machine.interrupt().disable();
    		
    		//Make new thread with containing values of currentThread and wakeTime
    		waitingThread thread = new waitingThread(KThread.currentThread(), wakeTime);
    		
    		//Add thread to wait_queue
    		wait_queue.add(thread);
    		
    		//Make current thread sleep
    		KThread.currentThread().sleep();
    		
    		//Restore interrupts 
    		Machine.interrupt().restore(interrupt_status);
    	}
    }
    
    
//    public static void alarmTest1() {
//    	int durations[] = {1000, 10*1000, 100*1000};
//    	long t0, t1;
//    	for (int d : durations) {
//    	    t0 = Machine.timer().getTime();
//    	    System.out.println(t0);
//    	    ThreadedKernel.alarm.waitUntil (d);
//    	    t1 = Machine.timer().getTime();
//    	    System.out.println(t1);
//    	    System.out.println ("alarmTest1: waited for " + (t1 - t0) + " ticks");
//    	}
//    }
    
//    public static void alarmTest2() {
//    	int durations[] = {5000, 10*5000, 100*5000};
//    	long t2, t3;
//    	for (int d : durations) {
//    	    t2 = Machine.timer().getTime();
//    	    System.out.println(t2);
//    	    ThreadedKernel.alarm.waitUntil (d);
//    	    t3 = Machine.timer().getTime();
//    	    System.out.println(t3);
//    	    System.out.println ("alarmTest2: waited for " + (t3 - t2) + " ticks");
//    	}
//    }
    
    
//    public static void selfTest() {
//    	alarmTest1();
//    	// Invoke your other test methods here ...
//    	//alarmTest2();
//    	    }
}
