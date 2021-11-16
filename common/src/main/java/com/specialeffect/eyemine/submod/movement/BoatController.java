/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * 
 * Developed for SpecialEffect, www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.eyemine.submod.movement;

import com.specialeffect.eyemine.mixin.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.vehicle.Boat;

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
		final KeyMapping kbLeft = Minecraft.getInstance().options.keyLeft;
		final KeyMapping kbRight = Minecraft.getInstance().options.keyRight;
		
		if (haveOverriddenLeft) {
			KeyMapping.set(((KeyMappingAccessor)kbLeft).getActualKey(), false);
			haveOverriddenLeft = false;
		}
		if (haveOverriddenRight) {
			KeyMapping.set(((KeyMappingAccessor)kbRight).getActualKey(), false);
			haveOverriddenRight = false;
		}
	}
	
	// speed [-1, +1] where +1 is for turning right
	// if abs(speed) > 1, it will just clip the effect
	private void steer(double speed) {
		System.out.println("steer " + speed);

		final KeyMapping kbLeft = Minecraft.getInstance().options.keyLeft;
		final KeyMapping kbRight = Minecraft.getInstance().options.keyRight;

		if (Math.random() < Math.abs(speed)) {
			if (speed > 0) {				
				KeyMapping.set(((KeyMappingAccessor)kbLeft).getActualKey(), true);
				haveOverriddenLeft = true;
			}
			else {
				KeyMapping.set(((KeyMappingAccessor)kbRight).getActualKey(), true);
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
	
	public void pid_step(Boat boat, double yaw_error) {
		// Reset if new boat 
		if (boat.getId() != this.boat_id) {
			this.yaw_error = 0;
			this.boat_id = boat.getId();
		}
		
		// TODO: timesteps??
		double d_err = yaw_error - this.yaw_error;
		accum_err += d_err;
		
		double control_input = Kp*yaw_error + Ki*accum_err + Kd*d_err;

		this.control_model(control_input);
		
		this.yaw_error = yaw_error;
	}
	
}
