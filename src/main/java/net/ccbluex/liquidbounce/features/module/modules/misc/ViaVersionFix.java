package net.ccbluex.liquidbounce.features.module.modules.misc;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.utils.PacketUtil;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.minecraft.item.ItemSword;

@ModuleInfo(name = "ViaVersionFix",spacedName = "Via Version Fix",description = "PacketFix FR?", category = ModuleCategory.MISC)
public class ViaVersionFix extends Module {
    @EventTarget
    public void onUpdate(UpdateEvent event){
        if(mc.thePlayer.isBlocking() || mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.isUsingItem()){
            int packetId = 29;
            UserConnection userConnection = Via.getManager().getConnectionManager().getConnections().iterator().next();
            PacketWrapper packet = PacketWrapper.create(packetId,null,userConnection);
            PacketUtil.sendToServer(packet, Protocol1_8TO1_9.class,true,true);
        }
    }
}
