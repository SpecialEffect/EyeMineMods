package com.specialeffect.eyemine.mixin;

import com.specialeffect.eyemine.submod.movement.IStepUp;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements IStepUp {

	@Unique
	private static final Identifier EYEMINE_STEP_UP_ID = Identifier.fromNamespaceAndPath("eyemine", "extra_step_up");

	@Unique
	public float eyemine$extraStepUp = 0.0F;

	@Override
	public float eyemine$getExtraStepUp() {
		return this.eyemine$extraStepUp;
	}

	@Override
	public void eyemine$setExtraStepUp(float stepUp) {
		this.eyemine$extraStepUp = stepUp;
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof Player) {
			AttributeInstance attr = self.getAttribute(Attributes.STEP_HEIGHT);
			if (attr != null) {
				// Remove old modifier if present
				attr.removeModifier(EYEMINE_STEP_UP_ID);
				// Add new modifier if non-zero
				if (stepUp > 0.0F) {
					attr.addTransientModifier(new AttributeModifier(
							EYEMINE_STEP_UP_ID,
							stepUp,
							AttributeModifier.Operation.ADD_VALUE
					));
				}
			}
		}
	}
}
