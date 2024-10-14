package cn.ksmcbrigade.satr.mixin;

import cn.ksmcbrigade.satr.SleepAnyTimeReborn;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StateHolder.class)
public abstract class StateHolderMixin {
    @Shadow public ImmutableMap<Property<?>, Comparable<?>> values;

    @Inject(method = "getValue",at = @At("HEAD"),cancellable = true)
    public <T extends Comparable<T>> void get(Property<T> p_61144_, CallbackInfoReturnable<T> cir){
        T ret = (T) this.values.get(p_61144_);

        cir.setReturnValue(ret!=null?ret: (T) SleepAnyTimeReborn.tmp);
        cir.cancel();
    }
}
