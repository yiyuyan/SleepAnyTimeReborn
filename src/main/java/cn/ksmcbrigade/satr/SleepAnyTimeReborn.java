package cn.ksmcbrigade.satr;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(SleepAnyTimeReborn.MODID)
public class SleepAnyTimeReborn {

    public static final String MODID = "satr";

    public static Direction tmp = Direction.UP;
    public static final SimpleChannel channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID,"sync"),()->"1",(a)->true,(b)->true);

    public SleepAnyTimeReborn() {
        MinecraftForge.EVENT_BUS.register(this);
        channel.registerMessage(0,Message.class,Message::encode,Message::decode,(msg,context)->{
            ServerPlayer player = context.get().getSender();
            if(player!=null){
                tmp = msg.direction;

                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->tmp = msg.direction);  //sync tmp

                player.startSleepInBed(msg.pos);
            }
            context.get().setPacketHandled(true);
        });
    }

    public record Message(BlockPos pos, Direction direction){
        public static void encode(Message msg, FriendlyByteBuf buf){
            buf.writeBlockPos(msg.pos());
            buf.writeEnum(msg.direction());
        }

        public static Message decode(FriendlyByteBuf buf){
            return new Message(buf.readBlockPos(),buf.readEnum(Direction.class));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.RightClickBlock event){
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockState block = level.getBlockState(event.getPos());

        if(player.isShiftKeyDown() && player.getUseItem().isEmpty() && !(block.getBlock() instanceof BedBlock)){
            boolean full = Block.isShapeFullBlock(block.getShape(level,event.getPos()));
            BlockPos pos = event.getPos();

            tmp = player.getDirection();

            channel.sendToServer(new Message(full?pos.above():pos,player.getDirection()));
        }
    }

    @SubscribeEvent
    public void onSleep(SleepingTimeCheckEvent event) {
        event.setResult(Event.Result.ALLOW);
    }
}
