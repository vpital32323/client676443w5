package dev.pulse.menu.mixin;

import dev.pulse.menu.Backgrounds;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void pulse$background(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (self instanceof MultiplayerScreen || self instanceof SelectWorldScreen) {
            Backgrounds.draw(ctx, self.width, self.height, 1f);
            ctx.fill(0, 0, self.width, self.height, 0x59000000);
            ci.cancel();
        }
    }
}
