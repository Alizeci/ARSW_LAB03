package edu.eci.arsw.highlandersim;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private AtomicInteger health;
    
    private int defaultDamageValue;

    private final CopyOnWriteArrayList<Immortal> immortalsPopulation;

    private final String name;
    
    boolean enPausa;

    private final Random r = new Random(System.currentTimeMillis());

	private boolean  status;


    public Immortal(String name, CopyOnWriteArrayList<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = new AtomicInteger(health);
        this.defaultDamageValue=defaultDamageValue;
        this.status = true;
    }

    public void run() {

        while (status && immortalsPopulation.size() != 1 ) {
        	synchronized(this){
				while (isEnPausa()){
					try {
						wait();                                
					} catch (InterruptedException ex) {
						ex.printStackTrace();
                    }
                }
        	}
            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);
            
            this.fight(im);
            
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void fight(Immortal i2) {
    	if(this.getHealth() <= 0) {
        	dead(this);
        	updateCallback.processReport(this +" is already dead!\n"); 
        }
    	else{
    		boolean oponenteSigueVivo = false;
    		
    		synchronized (i2) {
    			oponenteSigueVivo = i2.getHealth() > 0;
    			if (oponenteSigueVivo) {
	            	i2.changeHealth(i2.getHealth() - defaultDamageValue);
	            }
		    }
    		synchronized (this) {
    			if (oponenteSigueVivo) {
                	health.addAndGet(defaultDamageValue);
                }
            }
	        updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
	    }
    }

    public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
    
	public void changeHealth(int v) {
        health.set(v);
    }

    public int getHealth() {
        return health.intValue();
    }
    
	public boolean isEnPausa() {
		return enPausa;
	}
	
	public synchronized void restart() {
		this.setEnPausa(false);
		notify();
	}
	
	public void setEnPausa(boolean enPausa) {
		this.enPausa = enPausa;
	}
	
	public void dead(Immortal im) {
		im.setStatus(false);
    	immortalsPopulation.remove(im);
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
