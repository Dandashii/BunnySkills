
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;


    
public class SkillBlastwave extends ActiveSkill {

    public SkillBlastwave(Heroes plugin) {
        super(plugin, "Blastwave");
        setDescription("Cast a Blastwave over $1 radius from $2m away.");
        setUsage("/skill Blastwave");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Blastwave" });
        
        setTypes(SkillType.DAMAGING, SkillType.FIRE, SkillType.KNOWLEDGE, SkillType.SILENCABLE);
        //registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(this), Priority.Normal);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(this),plugin);
        //Bukkit.getServer().getPluginManager().registerEvent
    }

    @Override
    public String getDescription(Hero hero) {
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, "radius", 2, false) -
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getSkillLevel(this)));
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
        node.set("radius", 5);
        node.set(SkillSetting.DAMAGE.node(), 1);
        node.set(SkillSetting.MANA.node(),0);
        node.set(SkillSetting.COOLDOWN.node(),5000);
        node.set(SkillSetting.DURATION.node(),5000);
        
        node.set(SkillSetting.MAX_DISTANCE.node(), 25);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();

        //Location target = player.getTargetBlock(null, 0).getLocation();
        Location target = player.getEyeLocation();
        //target = player.getTargetBlock(null, 0).getLocation();
        
        if (target == null) { return SkillResult.INVALID_TARGET;}
        
        
        int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 1, false);

        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 15, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 5000, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 0.0, false) * hero.getSkillLevel(this)));
        duration = duration > 0 ? distance : 0;
        
        
        
        double pladistance = target.distance(player.getLocation());
        
        if (pladistance>distance) {return SkillResult.FAIL;}
        
        if(pladistance<=distance) {
                target.setY(target.getY()+1);
                for(int i=1;i<14&&target.getBlock().getType().equals(Material.AIR);i++)
                    target.setY(target.getY()+1);
                target.setY(target.getY()-1);
                

                final int scatt=SkillConfigManager.getUseSetting(hero, this, "radius",5,false);
                
                
                //final Player pla = player;
                //final Location tar = target;
                
                
                Player[] lp = Bukkit.getOnlinePlayers();
                for(int i=0;i<lp.length;i++) {
                                
                                double asdd = //target.distance(lp[i].getLocation());
                                lp[i].getLocation().distance(player.getTargetBlock(null, 0).getLocation());
                                
                                
                    if(asdd<=scatt && !lp[i].equals(player)) {
                        if(lp[i].getFireTicks()>0) {
                            //if alreayd on fire, damage
                            int ft=0;
                            
                            if(lp[i].getFireTicks()>100)
                                ft=100;
                            else ft=lp[i].getFireTicks();
                            
                            //System.out.println(damage);
                            damage = (int)((ft/100.0)*damage) + 2;
                            //System.out.println(damage);
                            //System.out.println("ft: "+ft);
                            
                            
                                
                            if (lp[i] instanceof Player && !(target.equals(player)) && damageCheck(hero.getPlayer(),lp[i]) && (damage>0)) {
                                try{
                                
                                Hero tHero = plugin.getCharacterManager().getHero(lp[i]);
                                    
                                if(tHero.getHealth()>damage)
                                    tHero.getPlayer().setHealth(tHero.getHealth()-damage);
                                else
                                    tHero.getPlayer().setHealth(1);
                                //tHero.syncHealth();
                                    
                                //damageEntity(lp[i], hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC);
                                }catch(Exception e) {
                                    System.out.println("Blastwave error: "+player.getName()+" attacking "+lp[i].getName()); 
                                }
                            }
                                //knock up
                                lp[i].setVelocity(new Vector(0,1,0));
                        }
                        if(lp[i].getFireTicks()<20) {
                                //set on fire lol
                                lp[i].setFireTicks(20);
                                //knock up
                                lp[i].setVelocity(new Vector(0,0.5,0));
                        }
                    }
                }
                
                
                /*
                    for(int i=0;i<duration;i++) {
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                    public void run() {
                            
                        }
                    }, (long)(i*4));
                }*/
                
        }
        
        return SkillResult.NORMAL;
    }
public class SkillEntityListener implements Listener {
        private final Skill skill;
        public SkillEntityListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler()
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent) || !(event.getEntity() instanceof LivingEntity)) {
                //System.out.println("first return");
                return;
            }
            
            
            EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
            Entity projectile = subEvent.getDamager();
            if (!(projectile instanceof Snowball)) {
                //System.out.println("projectile not instance of snowball");
                return;
            }
            
            LivingEntity entity = (LivingEntity) subEvent.getEntity();
            Entity dmger = ((Snowball) projectile).getShooter();
            if (dmger instanceof Player) {
                Hero hero = plugin.getCharacterManager().getHero((Player) dmger);
                
                if (!damageCheck((Player) dmger, entity)) {
                    event.setCancelled(true);
                    //System.out.println("damage check failed????");
                    return;
                }
                
                //System.out.println("Damage Code Running");
                
                // Damage the player/mob
                addSpellTarget(entity, hero);
                int damage = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 1, false);
                
                
                LivingEntity target = (LivingEntity) event.getEntity();
                
                if (entity instanceof Player && !(entity.equals((Player)dmger))) {
                    Player p = (Player) entity;
                    Hero tHero = plugin.getCharacterManager().getHero((Player) target);
                    
                    damageEntity(p, (Player)dmger, damage, EntityDamageEvent.DamageCause.MAGIC);
                } else if (entity instanceof Creature) {
                    Creature c = (Creature) entity;
                    damageEntity(c, (Player)dmger, damage, EntityDamageEvent.DamageCause.MAGIC);
                    
                    
                }
                
                
                
                event.setCancelled(true);
            }
        }
    }

}