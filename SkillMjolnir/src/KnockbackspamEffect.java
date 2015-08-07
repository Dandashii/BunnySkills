
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.PeriodicExpirableEffect;
import com.herocraftonline.heroes.characters.skill.Skill;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftFirework;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class KnockbackspamEffect extends PeriodicExpirableEffect {
    Location center;
    int radius;
    double strength;
    
    public KnockbackspamEffect(Skill sk, String name, Location l, int ra, double str, long pe, long du) {
        super(sk,name,pe,du);
        radius=ra;
        center=l;
        strength=str;
    }

    @Override
    public void tickMonster(Monster mnstr) {
        
    }

    @Override
    public void tickHero(Hero hero) {
        for(LivingEntity target:getNearbyLivingEntities(center,radius)) {
            if(Skill.damageCheck(hero.getPlayer(), target)) {
                Vector thrv = target.getLocation().subtract(hero.getPlayer().getLocation()).toVector().normalize().multiply(strength);
                thrv.setY(0.4);
                target.setVelocity(thrv);
            }
        }
        fw(center);
    }
    
    
    public void fw(final Location loc) {
        
        com.herocraftonline.heroes.characters.skill.VisualEffect asd
                = new com.herocraftonline.heroes.characters.skill.VisualEffect();
        
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().withColor(Color.AQUA).with(FireworkEffect.Type.BALL_LARGE).withFade(Color.BLACK).build();
        fwm.addEffects(effect);
        FireworkEffect effect2 = FireworkEffect.builder().withColor(Color.WHITE).with(FireworkEffect.Type.BALL_LARGE).withFade(Color.BLACK).build();
        fwm.addEffects(effect2);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
        //((CraftWorld)loc.getWorld()).getHandle().broadcastEntityEffect(
               // ((CraftFirework)fw).getHandle(),(byte)17);
        try {
            asd.playFirework(loc.getWorld(), loc, effect);
        } catch (Exception ex) {
            Logger.getLogger(KnockbackspamEffect.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        fw.remove();
    }
    
    public static List<LivingEntity> getNearbyLivingEntities(Location l, int radius) {
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16))/16;
        List<LivingEntity> radiusEntities=new ArrayList<>();
        int radiussq = radius*radius;
            for (int chX = 0 -chunkRadius; chX <= chunkRadius; chX ++){
                for (int chZ = 0 -chunkRadius; chZ <= chunkRadius; chZ++){
                    int x=(int) l.getX(),y=(int) l.getY(),z=(int) l.getZ();
                    for (Entity e : new Location(l.getWorld(),x+(chX*16),y,z+(chZ*16)).getChunk().getEntities()){
                        if(e.getLocation().getWorld().equals(l.getWorld())) {
                            if (e.getLocation().distanceSquared(l) <= radiussq && e.getLocation().getBlock() != l.getBlock()) {
                                if(e instanceof LivingEntity) {
                                    radiusEntities.add((LivingEntity)e);
                                }
                            }
                        }
                    }
                }
            }
        return radiusEntities;
    }
}
