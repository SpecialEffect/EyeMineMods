package com.specialeffect.eyemine.event;

/**
 * Block outline event - now delegates to EyeMineEvents.BLOCK_OUTLINE.
 * Kept for compatibility with existing code that references OUTLINE.
 */
public class BlockOutlineEvent {
    public static final EventHolder<EyeMineEvents.BlockOutlineListener> OUTLINE = EyeMineEvents.BLOCK_OUTLINE;
}
