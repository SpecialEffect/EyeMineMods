package com.specialeffect.mods.moving;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.item.BoatEntity;

public class BoatController {

	// Boats are steered with left, right arrow keys to control the paddles
	// We want to be able to steer smoothly, without excess speed, so we
	// need to throttle the controls
	
	private double Kp;
	private double Ki;
	private double Kd;
	
	private int boat_id = 0;
	
	BoatController(double Kp, double Ki, double Kd) {
		this.tune(Kp, Ki, Kd);
	}
	
	public void tune(double Kp, double Ki, double Kd) {
		this.Kp = Kp;
		this.Ki = Ki;
		this.Kd = Kd;
	}
	
	private boolean haveOverriddenLeft = false;
	private boolean haveOverriddenRight = false;

	public void releaseKeys() {
		final KeyBinding kbLeft = Minecraft.getInstance().gameSettings.keyBindLeft;
		final KeyBinding kbRight = Minecraft.getInstance().gameSettings.keyBindRight;
		
		if (haveOverriddenLeft) {
			KeyBinding.setKeyBindState(kbLeft.getKey(), false);
			haveOverriddenLeft = false;
		}
		if (haveOverriddenRight) {
			KeyBinding.setKeyBindState(kbRight.getKey(), false);
			haveOverriddenRight = false;
		}
	}
	
	// speed [-1, +1] where +1 is for turning right
	// if abs(speed) > 1, it will just clip the effect
	private void steer(double speed) {
		System.out.println("steer "+speed);

		final KeyBinding kbLeft = Minecraft.getInstance().gameSettings.keyBindLeft;
		final KeyBinding kbRight = Minecraft.getInstance().gameSettings.keyBindRight;

		if (Math.random() < Math.abs(speed)) {
			if (speed > 0) {				
				KeyBinding.setKeyBindState(kbLeft.getKey(), true);
				haveOverriddenLeft = true;
			}
			else {
				KeyBinding.setKeyBindState(kbRight.getKey(), true);
				haveOverriddenRight = true;
			}
		}
		else {
			this.releaseKeys();
		}
	}
	
	private double yaw_error = 0;
	private double accum_err = 0; 
	
	private void control_model(double control_input) {
		// this defines how we go from a requested change in yaw, 
		// to actually doing a thing. It doesn't really matter what
		// we do here, so long as in general it moves us in the right
		// direction. The PID controller will adjust around this.
		double steer_amount = control_input/45;		
		this.steer(steer_amount);
	}
	
	public void pid_step(BoatEntity boat, double yaw_error) {
		// Reset if new boat 
		if (boat.getEntityId() != this.boat_id) {
			this.yaw_error = 0;
			this.boat_id = boat.getEntityId();
		}
		
		// TODO: timesteps??
		double d_err = yaw_error - this.yaw_error;
		accum_err += d_err;
		
		double control_input = Kp*yaw_error + Ki*accum_err + Kd*d_err;

		this.control_model(control_input);
		
		this.yaw_error = yaw_error;
		
	}
	
}
