import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class SkillMechanicalSheep extends ActiveSkill implements Listener {
    public SkillMechanicalSheep(Heroes plugin) {
        super(plugin, "MechanicalSheep");
        setDescription("You put down a mechanical sheep that explodes after $1s.");
        setUsage("/skill MechanicalSheep");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill MechanicalSheep"});
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.DARK, SkillType.SUMMON, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 5000, false);
        String description = getDescription().replace("$1", ""+((int)Math.round(duration/1000)));
        
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
        node.set(SkillSetting.RADIUS.node(), 4);
        node.set(SkillSetting.DAMAGE.node(), 5);
        node.set(SkillSetting.DURATION.node(), 5000);
        return node;
    }
    
    public void randomexpl(Location loc) {
        loc=loc.clone().add(Math.random()*2,Math.random()*2,Math.random()*2);
        loc.getWorld().createExplosion(loc, 0, false);
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        broadcastExecuteText(hero);
        
        long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 5000, false);
        int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 5, false);
        int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 4, false);
        
        LivingEntity le1 = spawnSheep(hero.getPlayer().getLocation(),hero.getPlayer());
        
        
        final LivingEntity fle = le1;
        final Hero fhero = hero;
        final int fd = damage;
        final int fr = radius;
        
        for(int i=0;i<duration;i+=1000) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        fle.getWorld().playEffect(fle.getLocation(), Effect.CLICK2, 30);
                        fle.getWorld().playEffect(fle.getLocation(), Effect.CLICK2, 30);
                    }
            }, ((i/1000)*20));
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        fle.getWorld().playEffect(fle.getLocation(), Effect.CLICK2, 30);
                    }
            }, ((i/1000)*20+1));
        }
        
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                
                fle.getWorld().createExplosion(fle.getLocation(), 0, false);
                for(int i=0;i<6;i++) {
                    randomexpl(fle.getLocation());
                }
                
                for(LivingEntity le: getNearbyLivingEntities(fle.getLocation(),fr)) {
                    if (le.getEntityId()!=fhero.getPlayer().getEntityId() && le.getEntityId()!=fle.getEntityId()) {
                        if(le instanceof Player) {
                            if(!((Player)le).equals(fhero.getPlayer())) {
                                if(damageCheck(fhero.getPlayer(),(Player)le)) {
                                    addSpellTarget((Player)le, fhero);
                                    damageEntity((Player)le,fhero.getPlayer(),fd,EntityDamageEvent.DamageCause.MAGIC,false);
                                    le.getWorld().createExplosion(le.getLocation(), 0, false);
                                }
                            }
                        }
                        else {
                            if(damageCheck(fhero.getPlayer(),le)) {
                                addSpellTarget(le, fhero);
                                damageEntity(le,fhero.getPlayer(),fd,EntityDamageEvent.DamageCause.MAGIC,false);
                                le.getWorld().createExplosion(le.getLocation(), 0, false);
                            }
                        }
                    }
                }
                if(fle!=null) {
                    fle.remove();
                    lel.remove(fle.getEntityId());
                }
            }
        }, ((duration/1000)*20));
        
        return SkillResult.NORMAL;
    }
    
    //static Map<LivingEntity,String> lel = new HashMap<>();
    static Map<Integer,String> lel = new HashMap<>();
    
    public LivingEntity spawnSheep(Location loc, Player player) {
        LivingEntity le1;
        le1 = loc.getWorld().spawnCreature(loc,
                    CreatureType.SHEEP);
        le1.getWorld().playEffect(le1.getLocation(), Effect.SMOKE, 3);
        
        lel.put(le1.getEntityId(),player.getName());
        //le1.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 15000, 1));
        
        return le1;
    }
    
    @EventHandler
    public void onEdby(EntityDamageEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        if(!lel.containsKey(event.getEntity().getEntityId())) {
            return;
        }
        
        event.setCancelled(true);
        
        //EXPLODE
    }
    
    public static List<LivingEntity> getNearbyLivingEntities(Location l, int radius) {
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16))/16;
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

}