package nachos.threads;
import nachos.ag.BoatGrader;
import nachos.machine.Machine;

public class Boat
{
    static BoatGrader bg;
    static boolean not_done;
    static boolean boat_is_on_oahu;
    static Lock lock;
    static int children_on_boat;
    
	static int children_oahu_pop;
	static int adult_oahu_pop;
	static int oahu_population;
	
	static Lock population_lock = new Lock();
	static Lock boat_lock = new Lock();
	
	
	// If using semaphores, should we share the same lock?
	static Condition pilot = new Condition(boat_lock);
	static Condition children_Molakai = new Condition(boat_lock);
	static Condition children_Oahu = new Condition(boat_lock);
	static Condition adult_Oahu = new Condition(boat_lock);
	static Condition adult_Molakai = new Condition(boat_lock);
	static Condition finish = new Condition(population_lock);
	static Alarm timer = new Alarm();
	
    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
//	System.out.println("\n ***Testing Boats with only 2 children***");
//	begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
    	
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
		bg = b;
		not_done = true;
		children_oahu_pop = 0;
		adult_oahu_pop = 0;
		oahu_population = 0;
		children_on_boat = 0;

	// Instantiate global variables here
	
	
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

    }

    static void AdultItinerary()
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
    	boat_is_on_oahu = true;
    	
    	population_lock.acquire();
    	adult_oahu_pop++;
    	oahu_population++;
    	population_lock.release();
    	
    	timer.waitUntil(100);
    	KThread.currentThread().yield();
    	
    	while(not_done) {
    		boat_lock.acquire();
    		while(!boat_is_on_oahu) {
    			adult_Oahu.sleep();
    		}
    		if(children_oahu_pop < 2 && boat_is_on_oahu && children_on_boat == 0) {
    			
    			bg.AdultRowToMolokai();
    			boat_is_on_oahu = false;
    			population_lock.acquire();
    			adult_oahu_pop--;
    			oahu_population--;
    			children_Molakai.wake();
    			adult_Molakai.sleep();
    			boat_lock.release();
    		}
    		else {
    			children_Oahu.wake();
    			boat_lock.release();
    			KThread.currentThread().yield();
    		}
    	}
    	return;
    	
    }

    static void ChildItinerary()
    {
    	boat_is_on_oahu = true;
    	
    	population_lock.acquire();
    	children_oahu_pop++;
    	oahu_population++;
    	population_lock.release();
    	
    	timer.waitUntil(100);
    	
    	while(not_done) {
    		boat_lock.acquire();
			while (!boat_is_on_oahu && children_oahu_pop < 2) {
				// If not pilots, child goes to sleep (Oahu)
				children_Oahu.sleep();				
			} 
    		if(children_on_boat == 0) {
    			// First child gets in the boat and waits for pilot
    			children_on_boat++;
    			children_Oahu.wake();
    			children_Molakai.sleep();
    			
    			// When children wakes up in Molakai checks if needs to take the boat to Oahu
    			// for more people
    			//maybe a while loop instead for all children that are already in molakay
    			while(oahu_population != 0) {
    				if(!boat_is_on_oahu) {
    					bg.ChildRowToOahu();
        				children_on_boat = 0;
        				boat_is_on_oahu = true;
            			population_lock.acquire();
            			children_oahu_pop++;
            	    	oahu_population++;
            	    	population_lock.release();
//            	    	pilot.wakeAll();
            	    	boat_lock.release();
            	    	// Restart loop since now the child is on Oahu
            	    	break;
    				}
    			}
//    			if(!boat_is_on_oahu && oahu_population != 0) {
//    				bg.ChildRowToOahu();
//    				children_on_boat = 0;
//    				boat_is_on_oahu = true;
//        			population_lock.acquire();
//        			children_oahu_pop++;
//        	    	oahu_population++;
//        	    	population_lock.release();
////        	    	pilot.wakeAll();
//        	    	boat_lock.release();
//        	    	// Restart loop since now the child is on Oahu
//        	    	continue;
//    			}
    		}
    		else if(children_on_boat == 1) {
    			bg.ChildRowToMolokai();
    			bg.ChildRideToMolokai();
    			children_on_boat++;
    			boat_is_on_oahu = false;
    			population_lock.acquire();
    			children_oahu_pop -= 2;
    	    	oahu_population -= 2;
    	    	population_lock.release();
    	    	
    	    	while(oahu_population != 0) {
    	    		if(!boat_is_on_oahu) {
    					bg.ChildRowToOahu();
        				children_on_boat = 0;
        				boat_is_on_oahu = true;
            			population_lock.acquire();
            			children_oahu_pop++;
            	    	oahu_population++;
            	    	population_lock.release();
            	    	boat_lock.release();
            	    	// Restart loop since now the child is on Oahu
            	    	break;
    				}
    	    	}
//    	    	if(oahu_population == 0) {
//        			not_done = false;
//        			adult_Molakai.wakeAll();
//        			children_Molakai.wakeAll();
//        			return;
//        		}
//    	    	else {
//    	    		// Children goes to sleep at Molakai 
//    	    		children_Molakai.wake();
//    	    		children_Molakai.sleep();
//    	    		boat_lock.release();
//    	    	}
    		}
	    	if(oahu_population == 0) {
				not_done = false;
				adult_Molakai.wakeAll();
				children_Molakai.wakeAll();
				return;
	    	}
    	}
    	return;
    }
    
}
