package org.odonoghuelab.molecularcontroltoolkit.internal;
/******************************************************************************\
* Copyright (C) 2012-2013 Leap Motion, Inc. All rights reserved.               *
* Leap Motion proprietary and confidential. Not for distribution.              *
* Use subject to the terms of the Leap Motion SDK Agreement available at       *
* https://developer.leapmotion.com/sdk_agreement, or another agreement         *
* between Leap Motion and you, your company or other organization.             *
\******************************************************************************/

import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.Config;
import java.awt.IllegalComponentStateException;
import java.util.Timer;
import java.util.TimerTask;


import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.InteractionBox;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Pointable;
import com.leapmotion.leap.ScreenTapGesture;
import com.leapmotion.leap.Vector;
import java.awt.MouseInfo;
import java.util.logging.Logger;

/**
 * The LeapMotionConnector accesses the Leap Motion Controller using the Leap Motion Java API.
 * @author KennySabir
 */
public class LeapMotionConnector extends AbstractConnector {

	/** the leap motion controller interface */
	Controller controller;
	
	/** The custom leap motion listener for callbacks */
	LeapMotionListener listener;
        
        private static final Logger logger = Logger.getLogger(LeapMotionConnector.class.getName());
	
	/**
	 * Shutdown the interface
	 */
	public void shutdown() {
            controller.removeListener(listener);
	}
	
	/**
	 * Setup the interface
	 */
	public void setup()
	{
		System.err.println("Initialising Leap Motion from Java");
		if (controller != null) {
			shutdown();
		}
		else {
                    controller = new Controller();
		}

		listener = new LeapMotionListener();

                // Have the sample listener receive events from the controller
                controller.addListener(listener);
	}

	/**
	 * The Leap Motion Listener to handle the callbacks for events
	 * @author KennySabir
	 *
	 */
	class LeapMotionListener extends Listener {
		
		/** Store the last frame to monitor rotation and translation movements */
		Frame lastFrame = null;
		
		/** Store the lastX for custom pointing algorithm */
		float lastX = -1;
		
		/** Store the lastY for custom pointing algorithm */
		float lastY = -1;
                
                /**
                 * Stores the last value of the virtual touchscreen.
                 */
                Pointable.Zone lastTouchZone = Pointable.Zone.ZONE_NONE;
		
		/*
		 * (non-Javadoc)
		 * @see com.leapmotion.leap.Listener#onInit(com.leapmotion.leap.Controller)
		 */
		public void onInit(Controller controller) {
	        System.out.println("Initialized");
	    }

		/*
		 * (non-Javadoc)
		 * @see com.leapmotion.leap.Listener#onConnect(com.leapmotion.leap.Controller)
		 */
	    public void onConnect(Controller controller) {
	        System.out.println("Connected");
	        controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
	        //controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
                controller.enableGesture(Gesture.Type.TYPE_SWIPE);
                controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
                
                Config config = controller.config();
                config.setFloat("Gesture.ScreenTap.MinForwardVelocity", 30.0f);
                config.setFloat("Gesture.ScreenTap.HistorySeconds", 0.5f);
                config.setFloat("Gesture.ScreenTap.MinDistance", 1.0f);
                config.save();
	    }

	    /*
	     * (non-Javadoc)
	     * @see com.leapmotion.leap.Listener#onDisconnect(com.leapmotion.leap.Controller)
	     */
	    public void onDisconnect(Controller controller) {
	        //Note: not dispatched when running in a debugger.
	        System.out.println("Disconnected");
	    }

	    /*
	     * (non-Javadoc)
	     * @see com.leapmotion.leap.Listener#onExit(com.leapmotion.leap.Controller)
	     */
	    public void onExit(Controller controller) {
	        System.out.println("Exited");
	    }

	    /*
	     * (non-Javadoc)
	     * @see com.leapmotion.leap.Listener#onFrame(com.leapmotion.leap.Controller)
	     */
	    public void onFrame(Controller controller) {
                
	        // Get the most recent frame and report some basic information
	        Frame frame = controller.frame();
	        if (frame.hands().count() == 1 ) {
                        int extended = frame.fingers().extended().count();
                        logger.fine(extended + " extended fingers");

	        	if (extended > 3 && lastFrame != null) { //4 or 5 fingers extended
	        		// rotate
                                lastY = -1;
                                lastX = -1;

	        		Hand hand = frame.hands().frontmost();
//	        		float rotateProb = hand.rotationProbability(lastFrame);
	        		Vector translation = hand.translation(lastFrame);
	        		getGestureDispatcher().triggerPan((int) translation.getX(), -(int) translation.getY());
	        		getGestureDispatcher().triggerZoom((int) -translation.getZ());
                                
//        			Matrix matrix = hand.rotationMatrix(lastFrame);
    				int ROTATION_SCALE = 500;
    				float xRotation = hand.rotationAngle(lastFrame, new Vector(1,0,0));
    				float yRotation = hand.rotationAngle(lastFrame, new Vector(0,1,0));
    				float zRotation = hand.rotationAngle(lastFrame, new Vector(0,0,1));
    				getGestureDispatcher().triggerRotate((int) -(ROTATION_SCALE * xRotation), -(int) (ROTATION_SCALE * yRotation) , -(int)(ROTATION_SCALE * zRotation));
	        	}
	        	else if (extended > 0){ // 1 to 3 fingers extended
		        	try {
			        	Pointable finger = frame.fingers().frontmost();
			        	float zVel = finger.tipVelocity().getZ();
			    		int absZVel = (int) Math.abs(zVel);

//			        	Pointable lastFinger = lastFrame.fingers().frontmost();
//			        	Vector lastHit = controller.locatedScreens().closestScreenHit(lastFinger).intersect(lastFinger,true);
//			        	logger.info(finger + ": hit, last hit, xy: " + hit.getX() + ", " + hit.getY() );
                                        
                                        Vector stabilizedPosition = finger.stabilizedTipPosition();
                                        InteractionBox iBox = frame.interactionBox();
                                        Vector normalizedPosition = iBox.normalizePoint(stabilizedPosition);
                                        logger.finer("normalizedPosition: " + normalizedPosition);
                                        float x = normalizedPosition.getX();
                                        float y = normalizedPosition.getY();
                                        
                                        
                                        /* Virtual touchscreen */
                                        Pointable.Zone touchZone = finger.touchZone();
                                        logger.finer("Touch Zone: " + touchZone);
                                        
                                        //finger has moved from virtual touchscreen hover zone to touching zone
                                        if(touchZone == Pointable.Zone.ZONE_TOUCHING && lastTouchZone != Pointable.Zone.ZONE_TOUCHING)
                                        {
                                            getGestureDispatcher().touch(true);
                                        }
                                        //finger has moved away from virtual touchscreen touch zone
                                        else if(touchZone == Pointable.Zone.ZONE_HOVERING && lastTouchZone == Pointable.Zone.ZONE_TOUCHING)
                                        {
                                            getGestureDispatcher().touch(false);
                                        }
                                        lastTouchZone = touchZone;

                                        /* The action of poking the screen
                                        was found to be difficult to master as it was hard to maintain the
                                        same X and Y position of the tip of the finger while moving in the
                                        Z-axis alone. 
                                        Scale down the X and Y translation when the Z velocity is over a given threshold */
                                        if (absZVel > 50 && lastY != -1) {
			        		float scale = (float) 2500 / (absZVel * absZVel);
				        	x = (x - lastX) * scale + lastX;
				        	y = (y - lastY) * scale + lastY;
			        	}
                                        
                                        lastY = y;
			        	lastX = x;
			        	if (!Float.isNaN(x) && !Float.isNaN(y)) {
				        	getGestureDispatcher().point(x, y); //move the mouse cursor
			        	}
		        	}
		        	catch (IllegalComponentStateException e) {
		        		e.printStackTrace();
		        	}
		        }
	        	else { //no fingers extended
                            lastY = -1;
                            lastX = -1;
	        	}

                        GestureList gestures = frame.gestures();
		        for (int i = 0; i < gestures.count(); i++) {
		            Gesture gesture = gestures.get(i);
		            switch (gesture.type()) 
                            {
		                case TYPE_SWIPE:
                                    logger.fine("Swipe Gesture"); //not using this gesture at present - using Circle for reset instead
		                    break;
		                case TYPE_SCREEN_TAP:
                                    if(extended == 1)
                                    {
                                        getGestureDispatcher().selectMouseCursor();
                                    }
		                    break;
		                case TYPE_KEY_TAP:
                                    logger.fine("Key Tap Gesture"); //not using this gesture at present
		                    break;
                                case TYPE_CIRCLE:
                                    if(extended < 4)
                                    {
                                        getGestureDispatcher().reset();
                                    }
                                    break;
		                default:
		                    logger.warning("Unknown gesture type.");
		                    break;
		            }
		        }
	        }
	        lastFrame = frame;
	    }
	}
	
	@Override
	public boolean supports(InteractiveType type) {
		if (type == InteractiveType.GESTURE) {
			return true;
		}
		return false;
	}

}
