package com.specialeffect.eyemine.mixin;

import com.specialeffect.eyemine.submod.movement.IStepUp;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements IStepUp {
	@Shadow
	public abstract double getAttributeValue(Holder<Attribute> holder);

	@Unique
	public float eyemine$extraStepUp = 0.0F;

	@Override
	public float eyemine$getExtraStepUp() {
		return this.eyemine$extraStepUp;
	}

	@Override
	public void eyemine$setExtraStepUp(float stepUp) {
		this.eyemine$extraStepUp = stepUp;
	}

	@Inject(method = "maxUpStep()F", at = @At(value = "RETURN"), cancellable = true)
	public void maxUpStep(CallbackInfoReturnable<Float> cir) {
		LivingEntity livingEntity = (LivingEntity) (Object) this;
		if (livingEntity instanceof Player && eyemine$getExtraStepUp() > 0.0F) {
			float f = (float) this.getAttributeValue(Attributes.STEP_HEIGHT);
			float value = livingEntity.getControllingPassenger() instanceof Player ? Math.max(f, 1.0F) : f;
			cir.setReturnValue(value + this.eyemine$getExtraStepUp());
		}
	}
}
