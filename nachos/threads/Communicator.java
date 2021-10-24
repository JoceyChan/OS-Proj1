package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	mutex.acquire();
    	// while there is a word in the buffer
    	while(wordToBeHeard){ //will wake up listener and speaker will sleep
    		listener.wake();
    		speaker.sleep();
    	}
    	this.word = word;

    	// notes that buffer is full
    	wordToBeHeard = true;

    	//wake the listener and put speaker to sleep
    	listener.wake();
    	speaker.sleep();

    	lock.Release();


    }
    this.word = word;


    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
	return 0;
    }
    
 // buffer to pass word
    private int word;

    // lock for condition variables and to maintain atomicity
    private Lock mutex = new Lock;

    // declare condition variable for listeners here //mutex = lock
    private Condition2 listener = new Condition2(mutex);

    // declare condition variable for speakers here
    private Condition2 speaker = new Condition2(mutex);

    
    
}
