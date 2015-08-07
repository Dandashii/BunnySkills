import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.*;
import net.minecraft.server.TileEntitySkull;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftFirework;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.meta.FireworkMeta;


    
public class SkillPoisonward extends ActiveSkill implements Listener {

    public SkillPoisonward(Heroes plugin) {
        super(plugin, "Poisonward");
        setDescription("Place a poison ward at the target location to assist you");
        setUsage("/skill Poisonward");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Poisonward" });
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        setTypes(SkillType.DAMAGING, SkillType.KNOWLEDGE, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, "scatter", 2, false) -
                (SkillConfigManager.getUseSetting(hero, this, "scatter-increase", 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 15, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        
        String description = getDescription().replace("$1", radius + "").replace("$2", distance + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 100, false)
                - SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this));
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, SkillSetting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DAMAGE.node(), 2);
        node.set("attack-speed", 1000);
        node.set("drift", 2);
        node.set(SkillSetting.MANA.node(),0);
        node.set(SkillSetting.COOLDOWN.node(),5000);
        node.set(SkillSetting.DURATION.node(),10000);
        
        node.set(SkillSetting.MAX_DISTANCE.node(), 35);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();

        Location target = player.getTargetBlock(null, 0).getLocation();
        if (target == null) { return SkillResult.INVALID_TARGET;}
        
        Location t2 = target.clone();
        t2 = t2.add(0,3,0);
        
        //this.world.addParticle("fireworksSpark", this.locX, this.locY - 0.3D, this.locZ, this.random.nextGaussian() * 0.05D, -this.motY * 0.5D, this.random.nextGaussian() * 0.05D);
        
        
        
        //((CraftWorld)t2.getWorld()).getHandle().addParticle("fireworksSpark", t2.getX(), t2.getY() - 0.3D, t2.getZ(), 0, 1 * 0.5D, 0);
        
        //Firework fw = player.getWorld().spawn(t2, Firework.class);
        Firework fw = (Firework) player.getWorld().spawnEntity(t2, EntityType.FIREWORK);
            
        FireworkMeta fwm = fw.getFireworkMeta();

        FireworkEffect effect = FireworkEffect.builder().withColor(Color.BLUE).with(Type.BURST).build();
        
        fwm.addEffects(effect);
        fwm.setPower(0);       
        fw.setFireworkMeta(fwm);
        
        ((CraftWorld)t2.getWorld()).getHandle().broadcastEntityEffect(
                ((CraftFirework)fw).getHandle(),(byte)17);
        
        fw.remove();
        
        
        
        
        
        
        
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 15, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        
        
        if (target.distance(player.getLocation())>distance) {
            return SkillResult.FAIL;
        }

        if(!newWard(target, hero)) {
            return SkillResult.INVALID_TARGET;
        }
        
        
        
        
        return SkillResult.NORMAL;
    }
    
    static Map<Location, Location> m = new HashMap<>();
    
    public boolean newWard(Location tar, Hero hero) {
        Location base = tar.getBlock().getLocation();
            base.setY(base.getY()+1);
        Location head = tar.getBlock().getLocation();
            head.setY(head.getY()+2);
        if(!head.getBlock().getType().equals(Material.AIR) || !base.getBlock().getType().equals(Material.AIR)) {
            return false;
        }
        
        CraftWorld cw = (CraftWorld)tar.getWorld();
        
        m.put(base, head);
        
        base.getBlock().setType(Material.COBBLE_WALL);
        base.getBlock().setData((byte)1);
        
        head.getBlock().setType(Material.SKULL);
        head.getBlock().setData((byte)1);
        
        
        TileEntitySkull nsk = (TileEntitySkull) cw.getHandle().getTileEntity(head.getBlockX(), head.getBlockY(), head.getBlockZ());
        nsk.setRotation((int)(Math.random()*8));
        //nsk.setSkullType(0, "");

        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false));
        duration = duration > 0 ? duration : 0;
        
        long attackspeed = (long) (SkillConfigManager.getUseSetting(hero, this, "attack-speed", 5000, false) +
            (SkillConfigManager.getUseSetting(hero, this, "attack-speed", 0.0, false) * hero.getSkillLevel(this)));
        attackspeed = attackspeed > 0 ? attackspeed : 0;
        
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 10, false) +
            (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        
        final Location fbase = base;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
            @Override
            public void run(){
                removeWard(fbase);
        }},(long)(duration/50.0));
        
        
        attackScheme(hero, base, (long)Math.round(attackspeed/50.0), radius);
        
        
        return true;
    }
    
    public void attackScheme(Hero hero, Location base, int delay, final int radius) {
        attackScheme(hero, base, (long)delay, radius);
    }
    public void attackScheme(Hero hero, Location base, final long delay, final int radius) {
        final Location fbase = base;
        final Hero fher = hero;
        final Player fpla = hero.getPlayer();
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){@Override
            public void run(){
            if(m.containsKey(fbase) && fbase.getBlock().getType().equals(Material.COBBLE_WALL)) {
                //ATTACK
                fbase.getWorld().playEffect(m.get(fbase), Effect.MOBSPAWNER_FLAMES, 50);
                List<LivingEntity> nle = getNearbyLivingEntities(fbase,radius);
                System.out.println("Nearby List done");
                for(int r = (int)(Math.random()*nle.size());
                        nle.size()>0;
                        r = (int)(Math.random()*nle.size())) {
                    
                    if(damageCheck(fher.getPlayer(),nle.get(r))) {
                        //damageEntity(nle.get(r),fher.getPlayer(),2, EntityDamageEvent.DamageCause.POISON);
                        Location sh = new Location(fher.getPlayer().getWorld(),fbase.getX(),fbase.getY()+1.5,fbase.getZ());
                        Arrow ar = fbase.getWorld().spawn(sh, Arrow.class);
                        ar.setShooter(fher.getPlayer());
                        
                        org.bukkit.util.Vector thrv = nle.get(r).getLocation().subtract(m.get(fbase)).toVector().normalize().multiply(2);
                        //thrv.setY(0.4);
                        //thrv.add(new org.bukkit.util.Vector(0.0,0.3,0.0));
                        
                        int drift = (int) (SkillConfigManager.getUseSetting(fher, plugin.getSkillManager().getSkill("Poisonward"), "drift", 2, false));
                        drift = drift > 0 ? drift : 0;
                        
                        thrv.setX(thrv.getX()+((Math.random()-0.5)/10.0)*drift);
                        thrv.setY(thrv.getY()+((Math.random()-0.5)/10.0)*drift);
                        thrv.setZ(thrv.getZ()+((Math.random()-0.5)/10.0)*drift);
                        
                        ar.setVelocity(thrv);
                        
                        fbase.getWorld().playEffect(nle.get(r).getLocation(), Effect.POTION_BREAK, 612);
                        nle.removeAll(nle);
                        System.out.println("attack"+r);
                    }
                    else {
                        nle.remove(r);
                        System.out.println("removed"+r);
                    }
                    
                }
                
                //Schedule next
                attackScheme(fher,fbase,delay,radius);
            }
        }},delay);
    }
    
    public static List<LivingEntity> getNearbyLivingEntities(Location l, int radius) {
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16))/16;
        System.out.println("Started looking for nearby entities");
        List<LivingEntity> radiusEntities=new ArrayList<>();
        int radiussq = radius*radius;
            for (int chX = 0 -chunkRadius; chX <= chunkRadius; chX ++){
                for (int chZ = 0 -chunkRadius; chZ <= chunkRadius; chZ++){
                    int x=(int) l.getX(),y=(int) l.getY(),z=(int) l.getZ();
                    for (Entity e : new Location(l.getWorld(),x+(chX*16),y,z+(chZ*16)).getChunk().getEntities()){
                        if (e.getLocation().distanceSquared(l) <= radiussq && e.getLocation().getBlock() != l.getBlock()) {
                            if(e instanceof LivingEntity) {
                                radiusEntities.add((LivingEntity)e);
                            }
                        }
                    }
                }
            }
        return radiusEntities;
    }
    
    public void removeWard(Location loc) {
        if(m.containsKey(loc)) {
            loc.getBlock().setType(Material.AIR);
            m.get(loc).getBlock().setType(Material.AIR);
            m.remove(loc);
        }
        else if(m.containsValue(loc)) {
            loc.setY(loc.getY()+1);
            loc.getBlock().setType(Material.AIR);
            m.get(loc).getBlock().setType(Material.AIR);
            m.remove(loc);
        }
    }
    
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if(m.containsKey(event.getBlock().getLocation()) || m.containsValue(event.getBlock().getLocation())) {
            removeWard(event.getBlock().getLocation());
            event.setCancelled(true);
        }
    }
    
    
}