import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftFirework;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftWitherSkull;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class SkillExorcism extends ActiveSkill implements Listener {
    public SkillExorcism(Heroes plugin) {
        super(plugin, "Exorcism");
        setDescription("You release all your stored soul shards, dealing $1 damage each and costing all your soul shards.");
        setUsage("/skill Exorcism");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill Exorcism"});
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.DARK, SkillType.SUMMON, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        int damage = (int) SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 40, false);
        int soulcost = (int) SkillConfigManager.getUseSetting(hero, this, "soul-cost", 2, false);
        
        String description = getDescription().replace("$1",damage+"");
        
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
        node.set("souls-per-soul", 3);
        node.set("radius", 30);
        node.set("speed", 1);
        node.set(SkillSetting.DAMAGE.node(), 40);
        
        return node;
    }
    
    public int getSouls(Hero hero) {
        
        int maxsouls = (SkillConfigManager.getUseSetting(hero, plugin.getSkillManager().getSkill("SoulMastery"), "max-souls", 20, false));
                maxsouls = maxsouls > 0 ? maxsouls : 0;
                
        if(hero.hasEffect("SoulShards")) {
            int souls = ((SoulShardEffect)hero.getEffect("SoulShards")).getSouls();
            return souls;
        }
        else {
            hero.addEffect(new SoulShardEffect(this,plugin,"SoulShards",60000,maxsouls));
        }
        
        return 1;
    }
    
    public void messageSouls(Player pl, int soulcount, int max) {
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
    }
    
    public void release(Hero hero, int damage, int souls, int radius) {
        for(int i=0; i<souls; i++) {
            
            Vector dir = hero.getPlayer().getLocation().getDirection();
            dir.setX(Math.random()*2-1);
            dir.setZ(Math.random()*2-1);
            dir.setY(0);
            dir = dir.normalize();
            dir.setY(0);


            WitherSkull skull = hero.getPlayer().launchProjectile(WitherSkull.class);


            Location location = hero.getPlayer().getLocation();
            World world = location.getWorld();

            net.minecraft.server.World mcWorld = ((CraftWorld) world).getHandle();

            ShadowBolt sbolt = new ShadowBolt(mcWorld);
            
            sbolt.setPosition(location.getX(), location.getY()+0.3, location.getZ());
            
            mcWorld.addEntity(sbolt, CreatureSpawnEvent.SpawnReason.CUSTOM);
            
            sbolt.shooter=((CraftPlayer)hero.getPlayer()).getHandle();
            sbolt.hero=hero;
            sbolt.damage=(int)Math.round(damage);
            sbolt.givesouls=false;

            mcWorld.removeEntity(((CraftWitherSkull)skull).getHandle());
            ((CraftWitherSkull)skull).setHandle(sbolt);


            int speed = (SkillConfigManager.getUseSetting(hero, this, "speed", 3, false));
                    speed = speed > 0 ? speed : 0;

            skull.setVelocity(dir.clone().multiply(speed+Math.random()*1.5-0.5));
            
            
            
        keepPushing(hero, skull.getVelocity(), skull, speed, 40);
            
        }
    }
    
    // keep turning the skull
    public void keepPushing(final Hero hero, final Vector vec, final WitherSkull skull, final float speed, final int r) {
        if(skull==null || skull.isDead() || hero == null || hero.getPlayer() == null || hero.getPlayer().isDead()) {
            return;
        }
        if(skull.getLocation().distanceSquared(hero.getPlayer().getLocation()) >(r*r)) {
            //fireWkBall(skull.getLocation());
            skull.remove();
            return;
        }
        skull.setVelocity(vec);
        
        
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                keepPushing(hero, vec, skull, speed, r);
            }
        },1);
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
    
    
    
    
    
    
    
    public void fireWkball(Location loc) {
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect2 = FireworkEffect.builder().withColor(Color.BLACK).with(FireworkEffect.Type.BALL).withFade(Color.BLACK).build();
        fwm.addEffects(effect2);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
        ((CraftWorld)loc.getWorld()).getHandle().broadcastEntityEffect(
                ((CraftFirework)fw).getHandle(),(byte)17);
        fw.remove();
    }
    
    public static List<LivingEntity> getNearbyLivingEntities(Location l, double radius){
        int chunkRadius = radius < 16 ? 1 : ((int)(radius - (radius % 16))/16);
        List<LivingEntity> radiusEntities=new ArrayList<>();
        double radiussq = radius*radius;
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
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        if(!hero.hasEffect("SoulShards")) {
            hero.getPlayer().sendMessage("§7You do not have enough soul shards!");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        if(((SoulShardEffect)hero.getEffect("SoulShards")).getSouls()==0) {
            hero.getPlayer().sendMessage("§7You do not have any soul shards!");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        
        int radius = (int) SkillConfigManager.getUseSetting(hero, this, "radius", 4, false);
        
        int soulperhit = (int) SkillConfigManager.getUseSetting(hero, this, "souls-per-soul", 3, false);
        
        int damage = (int) SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 40, false);
        damage += (int) Math.round(hero.getLevel() * SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, 40, false));
        
        
        release(hero, damage, soulperhit * ((SoulShardEffect)hero.getEffect("SoulShards")).getSouls(), radius);
        
        
        ((SoulShardEffect)hero.getEffect("SoulShards")).addSouls(0-1000,hero);
        messageSouls(hero.getPlayer() , ((SoulShardEffect)hero.getEffect("SoulShards")).getSouls() , ((SoulShardEffect)hero.getEffect("SoulShards")).maxsouls);
        
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
}