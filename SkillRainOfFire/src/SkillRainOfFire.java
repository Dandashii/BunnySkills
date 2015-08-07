import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.ArrayList;
import java.util.List;
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


    
public class SkillRainOfFire extends ActiveSkill {

    public SkillRainOfFire(Heroes plugin) {
        super(plugin, "RainOfFire");
        setDescription("Call down a rain of fire over $1 radius from $2m away.");
        setUsage("/skill RainOfFire");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill RainOfFire" });
        
        setTypes(SkillType.DAMAGING, SkillType.ICE, SkillType.KNOWLEDGE, SkillType.SILENCABLE);
        //registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(this), Priority.Normal);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillRainOfFire.SkillEntityListener(this),plugin);
        //Bukkit.getServer().getPluginManager().registerEvent
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
        node.set("scatter", 2);
        node.set(SkillSetting.DAMAGE.node(), 1);
        node.set("slow-duration", 0);
        node.set("speed-multiplier", 2);
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

        Location target = player.getTargetBlock(null, 0).getLocation();
        //target = player.getTargetBlock(null, 0).getLocation();
        
        if (target == null) { return SkillResult.INVALID_TARGET;}
        
        
        
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
                for(int i=1;i<14&&target.getBlock().getType().equals(Material.AIR);i++) {
                    target.setY(target.getY()+1);
                }
                target.setY(target.getY()-1);
                
                //Snowball snowball = (Snowball) player.launchProjectile(Snowball.class);
                //snowball.teleport(target);

                final int scatt=SkillConfigManager.getUseSetting(hero, this, "scatter",2,false);
                final Player pla = player;
                final Location tar = target;
                
                    for(int i=0;i<duration;i++) {
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                            Snowball sn = (Snowball) pla.getWorld().spawn(tar,Snowball.class);
                            sn.setShooter(pla);
                            sn.setVelocity(new Vector((0.5-Math.random())/3*scatt,0-Math.random()/3,(0.5-Math.random())/3*scatt));
                            sn.setFireTicks(50);
                            snowballs.add(sn);
                            
                            Snowball sn2 = (Snowball) pla.getWorld().spawn(tar,Snowball.class);
                            sn2.setShooter(pla);
                            sn2.setVelocity(new Vector((0.5-Math.random())/3*scatt,0-Math.random()/3,(0.5-Math.random())/3*scatt));
                            sn2.setFireTicks(50);
                            snowballs.add(sn2);
                            
                            Snowball sn3 = (Snowball) pla.getWorld().spawn(tar,Snowball.class);
                            sn3.setShooter(pla);
                            sn3.setVelocity(new Vector((0.5-Math.random())/3*scatt,0-Math.random()/3,(0.5-Math.random())/3*scatt));
                            sn3.setFireTicks(50);
                            snowballs.add(sn3);
                            
                            Snowball sn4 = (Snowball) pla.getWorld().spawn(tar,Snowball.class);
                            sn4.setShooter(pla);
                            sn4.setVelocity(new Vector((0.5-Math.random())/3*scatt,0-Math.random()/3,(0.5-Math.random())/3*scatt));
                            sn4.setFireTicks(50);
                            snowballs.add(sn4);
                            
                            
                        }
                    }, (long)(i*4));
                }
                
        }
        
        return SkillResult.NORMAL;
    }
    
    
    private List<Snowball> snowballs = new ArrayList<>();
public class SkillEntityListener implements Listener {
        private final Skill skill;
        public SkillEntityListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler()
        public void onEntityDamage(EntityDamageEvent event) {
            if (!(event instanceof EntityDamageByEntityEvent) || !(event.getEntity() instanceof LivingEntity)) {
                //System.out.println("first return");
                return;
            }
            
            
            EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
            Entity projectile = subEvent.getDamager();
            if (!(projectile instanceof Snowball)) {
                //System.out.println("projectile not instance of snowball");
                return;
            }
            
            if(!snowballs.contains((Snowball)projectile)) {
                return;
            }
            snowballs.remove((Snowball)projectile);
            
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
                
                long duration = SkillConfigManager.getUseSetting(hero, skill, "slow-duration", 0, false);
                int amplifier = SkillConfigManager.getUseSetting(hero, skill, "speed-multiplier", 2, false);
                
                SlowEffect iceSlowEffect = new SlowEffect(skill, duration, amplifier, false, "", "", hero);
                LivingEntity target = (LivingEntity) event.getEntity();
                
                if (entity instanceof Player && !(entity.equals((Player)dmger))) {
                    Player p = (Player) entity;
                    Hero tHero = plugin.getCharacterManager().getHero((Player) target);
                    if(duration>0) {
                        tHero.addEffect(iceSlowEffect);
                    }
                    damageEntity(p, (Player)dmger, damage, EntityDamageEvent.DamageCause.MAGIC);
                } else if (entity instanceof Creature) {
                    Creature c = (Creature) entity;
                    damageEntity(c, (Player)dmger, damage, EntityDamageEvent.DamageCause.MAGIC);
                    plugin.getCharacterManager().getMonster(entity).addEffect(iceSlowEffect);
                    
                }
                
                
                
                event.setCancelled(true);
            }
        }
    }

}