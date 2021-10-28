package nachos.threads;
import nachos.ag.BoatGrader;
import nachos.machine.Machine;

public class Boat
{
    static BoatGrader bg;
    static boolean not_done;
    static boolean boat_is_on_oahu;
    static int children_on_boat;
    
	static int children_oahu_pop;
	static int adult_oahu_pop;
	static int oahu_population;
	
	static Lock population_lock = new Lock();
	static Lock boat_lock = new Lock();
	
	static Condition children_Molakai = new Condition(boat_lock);
	static Condition children_Oahu = new Condition(boat_lock);
	static Condition adult_Oahu = new Condition(boat_lock);
	static Condition adult_Molakai = new Condition(boat_lock);
	static Alarm timer = new Alarm();
	
    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
//	System.out.println("\n ***Testing Boats with only 2 children***");
//	begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(3, 3, b);

  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
  	begin(2, 5, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
    	// Initialize population variables and boat location
		bg = b;
		not_done = true;
		boat_is_on_oahu = true;
		
		children_oahu_pop = 0;
		adult_oahu_pop = 0;
		oahu_population = 0;
		children_on_boat = 0;
	
	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.
	
		
	// Creating runnable for all children
	Runnable run_child = new Runnable() {
	    public void run() {
	    		ChildItinerary();
            }
        };
        
    // Creating runnable for all adult    
    Runnable run_adult = new Runnable() {
	    public void run() {
	    		AdultItinerary();
            }
        };
        
        Machine.interrupt().disable();
        
        // Create and run children threads
        for(int i = 0; i < children; i++) {
        	KThread temp = new KThread(run_child);
        	temp.setName("Child " + i);
        	temp.fork();
        }
        
        // Create and run adult threads
        for(int i = 0; i < adults; i++) {
        	KThread temp = new KThread(run_adult);
        	temp.setName("Adult " + i);
        	temp.fork();
        }
        
        Machine.interrupt().enable();
        while(not_done) {
        	KThread.yield();
        }

    }

    static void AdultItinerary()
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
    	
    	// Updating population as adults arrive
    	population_lock.acquire();
    	adult_oahu_pop++;
    	oahu_population++;
    	population_lock.release();
    	
    	timer.waitUntil(100);
//    	boat_lock.acquire();
//    	adult_Oahu.sleep();
//    	boat_lock.release();
    	
    	// Allow children go first by giving up the CPU until not threads in ready queue
    	KThread.currentThread().yield();
    	
    	while(not_done) {
    		boat_lock.acquire();
    		// While the boat is no in Oahu sleep 
    		while(!boat_is_on_oahu) {
//    			children_Molakai.wake();
    			adult_Oahu.sleep();
    		}
    		// If boat in Oahu and not children in Oahu we use the boat
    		if(children_oahu_pop < 2 && boat_is_on_oahu && children_on_boat == 0) {
    			
    			// Update boat location and population
    			bg.AdultRowToMolokai();
    			boat_is_on_oahu = false;
    			population_lock.acquire();
    			adult_oahu_pop--;
    			oahu_population--;
    			population_lock.release();
    			// Need to wake up a child to return the boat and go to sleep
    			children_Molakai.wake();
    			adult_Molakai.sleep();
    			boat_lock.release();
    		}
    		else {
    			// If there are children wake up a child to use the boat in Oahu
    			children_Oahu.wake();
    			boat_lock.release();
    			KThread.currentThread().yield();
    		}
    	}
    	return;
    }

    static void ChildItinerary()
    {    	
    	// Updating population as children arrive
    	population_lock.acquire();
    	children_oahu_pop++;
    	oahu_population++;
    	population_lock.release();
    	
    	timer.waitUntil(100);
    	
    	while(not_done) {
    		boat_lock.acquire();
    		// Need to sleep if boat is not on Oahu and if not pilot
			if(!boat_is_on_oahu || children_oahu_pop < 2) {
				// If no pilot, child lets Adults to go first
				if(children_oahu_pop < 2)
					adult_Oahu.wake();
				children_Oahu.sleep();				
			} 
			// Children first as passanger 
    		if(children_on_boat == 0) {
    			// First child gets in the boat and waits for pilot
    			children_on_boat++;
    			children_Oahu.wake();
    			children_Molakai.sleep();
    			
    			// When children wakes up in Molakai checks if needs to take the boat to Oahu
    			// for more people
    			if(oahu_population != 0 && !boat_is_on_oahu) {
    					// Update boat location and population
    					bg.ChildRowToOahu();
    					children_on_boat = 0;
        				boat_is_on_oahu = true;
            			population_lock.acquire();
            			children_oahu_pop++;
            	    	oahu_population++;
            	    	population_lock.release();
            	    	boat_lock.release();
    			}
    			else if(oahu_population == 0 ) {
    				not_done = false;
//    				adult_Molakai.wakeAll();
//    				children_Molakai.wakeAll();
    				return;
    			}
    			// This will take us back to the beginning to the loop (Oahu population logic)
    			continue;
    		}
    		// This child is the pilot and it will bring back the boat if people still left in Oahu
    		else if(children_on_boat == 1) {
    			// Update boat location and population
    			bg.ChildRowToMolokai();
    			bg.ChildRideToMolokai();
    			children_on_boat++;
    			boat_is_on_oahu = false;
    			population_lock.acquire();
    			children_oahu_pop -= 2;
    	    	oahu_population -= 2;
    	    	population_lock.release();
    	    	
    	    	// When children wakes up in Molakai checks if needs to take the boat to Oahu
    			// for more people
    	    	if(!boat_is_on_oahu && oahu_population != 0) {
    					bg.ChildRowToOahu();
        				children_on_boat = 0;
        				boat_is_on_oahu = true;
            			population_lock.acquire();
            			children_oahu_pop++;
            	    	oahu_population++;
            	    	population_lock.release();
            	    	boat_lock.release();
    	    	}
    	    	// If done terminate
    	    	else if(oahu_population == 0 ) {
    				not_done = false;
//    				adult_Molakai.wakeAll();
//    				children_Molakai.wakeAll();
    				return;
    			}
    	    	continue;
    		}
	    	if(oahu_population == 0) {
				not_done = false;
//				adult_Molakai.wakeAll();
//				children_Molakai.wakeAll();
				return;
	    	}
    	}
    	return;
    }
    
}
