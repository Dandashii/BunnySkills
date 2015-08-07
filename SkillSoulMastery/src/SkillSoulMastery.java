import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftFirework;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftWitherSkull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class SkillSoulMastery extends PassiveSkill implements Listener {
    
    public SkillSoulMastery(Heroes plugin) {
        super(plugin, "SoulMastery");
        setDescription("Mastery of souls");
        setTypes(SkillType.UNBINDABLE);
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public String getDescription(Hero hero) {
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 200, false) -
                (SkillConfigManager.getUseSetting(hero, this, "radius-decrease", 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 15, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        
        String description = getDescription().replace("$1", radius + "").replace("$2", distance + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 0, false)
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
        
        node.set("shoot-cooldown", 600);
        node.set("shoot-cooldown-reduce", 0);
        node.set("range", 40);
        node.set("speed", 2);
        node.set("max-souls", 10);
        node.set("souls-per-hit", 1);
        
        return node;
    }
    
    
    @Override
    public void init() {
        super.init();
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onProj(org.bukkit.event.entity.ProjectileHitEvent event) {
        if(!(event.getEntity() instanceof CraftWitherSkull)) {
            return;
        }
        if(!(((CraftWitherSkull)event.getEntity()).getHandle() instanceof ShadowBolt)) {
            return;
        }
        
        
        fireWk(event.getEntity().getLocation());
        
        int radiusSq = 8;

        double dist=500000;
        LivingEntity kle=null;
        for(LivingEntity le:getNearbyLivingEntities(event.getEntity().getLocation(),radiusSq)) {
            if(le.getLocation().distanceSquared(event.getEntity().getLocation())<dist) {
                dist=le.getLocation().distanceSquared(event.getEntity().getLocation());
                kle=le;
            }
        }
        if(kle!=null) {
            Hero hero = ((ShadowBolt)(((CraftWitherSkull)event.getEntity()).getHandle())).hero;
            int damage = ((ShadowBolt)(((CraftWitherSkull)event.getEntity()).getHandle())).damage;
            
            if(damageCheck(hero.getPlayer(),kle)) {
                Skill.damageEntity(kle, hero.getPlayer(), damage, DamageCause.ENTITY_ATTACK, false);
                event.getEntity().remove();
                
                int maxsouls = (SkillConfigManager.getUseSetting(hero, this, "max-souls", 20, false));
                    maxsouls = maxsouls > 0 ? maxsouls : 0;
                    
                    boolean give = ((ShadowBolt)(((CraftWitherSkull)event.getEntity()).getHandle())).givesouls;
                    
                messageSouls(hero.getPlayer(),getSouls(hero,give),maxsouls,give);
                
                return;
            }
        }
        
        
        event.getEntity().remove();
    }
    
    @EventHandler
    public static void wepdmg(com.herocraftonline.heroes.api.events.WeaponDamageEvent event) {
        if(event.getDamager().hasEffect("SoulMastery")) {
            event.setDamage(10);
        }
    }
    
    
    public void fireWk(Location loc) {
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect2 = FireworkEffect.builder().withColor(Color.BLACK).with(FireworkEffect.Type.BURST).withFade(Color.BLACK).build();
        fwm.addEffects(effect2);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
        ((CraftWorld)loc.getWorld()).getHandle().broadcastEntityEffect(
                ((CraftFirework)fw).getHandle(),(byte)17);
        fw.remove();
    }
    
    public int getSouls(Hero hero, boolean givesouls) {
        
        int maxsouls = (SkillConfigManager.getUseSetting(hero, this, "max-souls", 20, false));
                maxsouls = maxsouls > 0 ? maxsouls : 0;
                
        int soulperhit = (SkillConfigManager.getUseSetting(hero, this, "souls-per-hit", 1, false));
                soulperhit = soulperhit > 0 ? soulperhit : 0;
                
        if(hero.hasEffect("SoulShards")) {
            if(givesouls) {
                ((SoulShardEffect)hero.getEffect("SoulShards")).addSouls(soulperhit,hero);
            }
            int souls = ((SoulShardEffect)hero.getEffect("SoulShards")).getSouls();
            return souls;
        }
        else {
            hero.addEffect(new SoulShardEffect(this,plugin,"SoulShards",60000,maxsouls));
        }
        
        return 1;
    }
    
    public void messageSouls(Player pl, int soulcount, int max, boolean give) {
        if(!give) {
            return;
        }
        int souls = soulcount;
        
        String stext = "§5Soul Shards: §0[ ";
        
        for(int i=0;i<souls;i++) {
            stext+="§5• ";
        }
        
        for(int i=0;i<(max-souls);i++) {
            stext+="§0• ";
        }
        
        stext += "§0] §5"+soulcount;
        
        pl.sendMessage(stext);
        //pl.sendMessage("§11§22§33§44§55§66§77§88§99§00§aa§bb§cc§dd§ee");
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        
        Hero hero = plugin.getCharacterManager().getHero(event.getPlayer());
        
        if(!hero.hasEffect("SoulMastery")) {
            return;
        }
        if(!event.getAction().equals(Action.LEFT_CLICK_BLOCK) && !event.getAction().equals(Action.LEFT_CLICK_AIR)) {
            return;
        }
        if(event.getItem()==null) {
            return;
        }
        if(event.getItem().getType()==null) {
            return;
        }
        if(event.getItem().getType()==Material.AIR) {
            return;
        }
        if(!event.hasItem()) {
            return;
        }

        Material weapon;
        if( event.getItem().getType()!=Material.BONE &&
            event.getItem().getType()!=Material.WOOD_HOE &&
            event.getItem().getType()!=Material.STONE_HOE &&
            event.getItem().getType()!=Material.IRON_HOE &&
            event.getItem().getType()!=Material.GOLD_HOE &&
            event.getItem().getType()!=Material.DIAMOND_HOE) {
            return;
        }
        else {
            weapon=event.getItem().getType();
        }
        if(hero.getCooldown("SoulMasteryS") != null && hero.getCooldown("SoulMasteryS") > System.currentTimeMillis()) {
            return;
        }
        long cooldown = (long) (SkillConfigManager.getUseSetting(hero, this, "shoot-cooldown", 500, false));
                cooldown = cooldown > 0 ? cooldown : 0;
        cooldown -= (long) (SkillConfigManager.getUseSetting(hero, this, "shoot-cooldown-reduce", 0, false));
        hero.setCooldown("SoulMasteryS", cooldown + System.currentTimeMillis());
        
        double damage = hero.getHeroClass().getItemDamage(weapon)+ (hero.getHeroClass().getItemDamageLevel(weapon) * hero.getLevel());
        
        
        try{
            @SuppressWarnings("rawtypes")
            Class[] args = new Class[3];
            args[0] = Class.class;
            args[1] = String.class;
            args[2] = int.class;
 
            Method a = net.minecraft.server.EntityTypes.class.getDeclaredMethod("a", args);
            a.setAccessible(true);
 
            a.invoke(a, ShadowBolt.class, "WitherSkull", 19);
        }catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
            
        }
        
        
        
        
        Vector dir = hero.getPlayer().getLocation().getDirection();
        dir = dir.normalize();
        
        
        WitherSkull skull = hero.getPlayer().launchProjectile(WitherSkull.class);
        
        
        Location location = hero.getPlayer().getLocation();
        World world = location.getWorld();

        net.minecraft.server.World mcWorld = ((CraftWorld) world).getHandle();

        ShadowBolt sbolt = new ShadowBolt(mcWorld);

        sbolt.setPosition(location.getX(), location.getY()+1.5, location.getZ());

        mcWorld.addEntity(sbolt, CreatureSpawnEvent.SpawnReason.CUSTOM);

        sbolt.shooter=((CraftPlayer)hero.getPlayer()).getHandle();
        sbolt.hero=hero;
        sbolt.damage=(int)Math.round(damage);
        
        mcWorld.removeEntity(((CraftWitherSkull)skull).getHandle());
        ((CraftWitherSkull)skull).setHandle(sbolt);
        
        
        int speed = (SkillConfigManager.getUseSetting(hero, this, "speed", 3, false));
                speed = speed > 0 ? speed : 0;
                
        int range = (SkillConfigManager.getUseSetting(hero, this, "range", 30, false));
                range = range > 0 ? range : 0;
        
        skull.setVelocity(dir.clone().multiply(speed));
        
        keepTurning(hero, skull, speed, range);
    }
    
    // keep turning the skull
    public void keepTurning(final Hero hero, final WitherSkull skull, final float speed, final int r) {
        if(skull==null || skull.isDead() || hero == null || hero.getPlayer() == null || hero.getPlayer().isDead()) {
            return;
        }
        if(skull.getLocation().distanceSquared(hero.getPlayer().getLocation()) >(r*r)) {
            //fireWkBall(skull.getLocation());
            skull.remove();
            return;
        }
        skull.setVelocity(hero.getPlayer().getLocation().getDirection().normalize().multiply(speed));
        
        
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                keepTurning(hero, skull, speed, r);
            }
        },1);
    }
    
    
    public static List<LivingEntity> getNearbyLivingEntities(Location l, double radius){
        int chunkRadius = radius < 16 ? 1 : ((int)(radius - (radius % 16))/16);
        List<LivingEntity> radiusEntities=new ArrayList<>();
        double radiussq = radius*radius;
            for (int chX = 0 -chunkRadius; chX <= chunkRadius; chX ++){
                for (int chZ = 0 -chunkRadius; chZ <= chunkRadius; chZ++){
                    int x=(int) l.getX(),y=(int) l.getY(),z=(int) l.getZ();
                    for (Entity e : new Location(l.getWorld(),x+(chX*16),y,z+(chZ*16)).getChunk().getEntities()){
                        if (e.getWorld().equals(l.getWorld())) {
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