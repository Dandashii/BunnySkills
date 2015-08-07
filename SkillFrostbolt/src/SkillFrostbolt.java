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
import net.minecraft.server.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

public class SkillFrostbolt extends ActiveSkill {

    static List<Snowball> snowballs = new ArrayList<>();

    //private String applyText;
    //private String expireText;
    
    public SkillFrostbolt(Heroes plugin) {
        super(plugin, "Frostbolt");
        setDescription("You launch a ball of ice that deals $1 damage to your target. This will do $2x damage on frozen targets.");
        setUsage("/skill frostbolt");
        setArgumentRange(0, 0);
        setIdentifiers("skill frostbolt");
        setTypes(SkillType.ICE, SkillType.SILENCABLE, SkillType.DAMAGING, SkillType.HARMFUL);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(this), plugin);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DAMAGE.node(), 3);
        node.set(SkillSetting.DAMAGE_INCREASE.node(), 0);
        node.set("slow-duration", 5000); // 5 seconds
        node.set("speed-multiplier", 2);
        node.set("projectile-speed", 2.0);
        node.set("frozen-amplifier", 3.0);
        node.set(SkillSetting.APPLY_TEXT.node(), "%target% has been slowed by %hero%!");
        node.set(SkillSetting.EXPIRE_TEXT.node(), "%target% is no longer slowed!");
        return node;
        
    }
    @Override
    public void init() {
        super.init();
        //applyText = SkillConfigManager.getRaw(this, SkillSetting.APPLY_TEXT, "%target% has been slowed by %hero%!").replace("%target%", "$1").replace("%hero%", "$2");
        //expireText = SkillConfigManager.getRaw(this, SkillSetting.EXPIRE_TEXT, "%target% is no longer slowed!").replace("%target%", "$1");
    }
    
    @Override
    public String getDescription(Hero hero) {
        
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 3, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE.node(), 0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        
        double amp = (SkillConfigManager.getUseSetting(hero, this, "frozen-amplifier", 3.0, false));
        amp = amp > 0 ? amp : 0;
        
        String description = getDescription().replace("$1", damage + "").replace("$2", amp + "");
        
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
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        Location location = player.getEyeLocation();

        float pitch = location.getPitch() / 180.0F * 3.1415927F;
        float yaw = location.getYaw() / 180.0F * 3.1415927F;

        double motX = -MathHelper.sin(yaw) * MathHelper.cos(pitch);
        double motZ = MathHelper.cos(yaw) * MathHelper.cos(pitch);
        double motY = -MathHelper.sin(pitch);
        Vector velocity = new Vector(motX, motY, motZ);

        double vel = SkillConfigManager.getUseSetting(hero, this, "projectile-speed", 2.0, false);
        
        velocity.multiply(vel);
        
        Snowball snowball = player.launchProjectile(Snowball.class);
        snowball.setVelocity(velocity);
        snowballs.add(snowball);

        //broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

    public class SkillEntityListener implements Listener {
        
        private final Skill skill;
        
        public SkillEntityListener(Skill skill) {
            this.skill = skill;
        }
        
        
        
        @EventHandler(priority=EventPriority.HIGHEST)
        public void onProjectileHit(ProjectileHitEvent event) {
            
            Entity projectile = event.getEntity();
            
            if (!(projectile instanceof Snowball)) {
                return;
            }
            if(!snowballs.contains((Snowball)projectile)) {
                return;
            }
            
            LivingEntity shooter = ((Snowball)projectile).getShooter();
            if(!(shooter instanceof Player)) {
                return;
            }
            
            Hero hero = plugin.getCharacterManager().getHero((Player)shooter);
            
            int damage = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 3, false);
            damage+=SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0, false)*hero.getLevel();
            int damage2 = (int)Math.round(damage*SkillConfigManager.getUseSetting(hero, skill, "frozen-amplifier", 5.0, false)-damage);
            
                int tr = 0;
                for(Player p:getNearbyPlayers(projectile.getLocation(),8)) {
                    Hero hr = plugin.getCharacterManager().getHero(p);
                    if(hr.hasEffect("frozen")) {
                        if(damageCheck((Player)shooter,p)) {
                            tr++;
                            p.sendMessage("§7You shatter for "+(damage2+damage)+" damage!");
                            
                            // auto attack not spell plx
                            //addSpellTarget(p, hero);
                            damageEntity(p, hero.getPlayer(), damage2+damage, EntityDamageEvent.DamageCause.PROJECTILE, false);
                            event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(),0,false);
                        }
                        else {System.out.println("damage check failed: "+p.getName());}
                    }
                }
                if(tr>1) {
                    int mana=SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 4, false);
                    if((hero.getMaxMana()-hero.getMana())<=mana) {
                        hero.setMana(hero.getMaxMana());
                    }
                    else {
                        hero.setMana(hero.getMana()+mana);
                    }
                    hero.getPlayer().sendMessage("§7You shatter your targets!");
                }
                else if(tr>0) {
                    hero.getPlayer().sendMessage("§7You shatter your target!");
                }
            
        }
        
        @EventHandler(priority=EventPriority.HIGHEST)
        public void onEntityDamage(EntityDamageByEntityEvent event) {
            
            
            if (!(event.getEntity() instanceof LivingEntity)) {
                return;
            }
            
            Entity projectile = event.getDamager();
    
    
            if (!(projectile instanceof Snowball)) {
                return;
            }
            if(!snowballs.contains((Snowball)projectile)) {
                return;
            }
            
            
            snowballs.remove((Snowball)projectile);
            
            
            
            Entity dmger = ((Snowball) event.getDamager()).getShooter();
            if (dmger instanceof Player) {
                Hero hero = plugin.getCharacterManager().getHero((Player) dmger);

                event.getEntity().setFireTicks(0);
                int damage = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 3, false);
                damage+=SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0, false)*hero.getLevel();
                
                if(event.getEntity() instanceof Player) {
                    if(plugin.getCharacterManager().getHero((Player)event.getEntity()).hasEffect("frozen")) {
                        projectile.remove();
                        //damage*=SkillConfigManager.getUseSetting(hero, skill, "frozen-amplifier", 5.0, false);
                        int damage2 = (int)Math.round(damage*SkillConfigManager.getUseSetting(hero, skill, "frozen-amplifier", 5.0, false)-damage);
                        
                        int tr = 0;
                        for(Player p:getNearbyPlayers(projectile.getLocation(),8)) {
                            Hero hr = plugin.getCharacterManager().getHero(p);
                            if(hr.hasEffect("frozen")) {
                                if(damageCheck(hero.getPlayer(),p)) {
                                    tr++;
                                    p.sendMessage("§7You shatter for "+(damage2+damage)+" damage!");
                                    //addSpellTarget(event.getEntity(), hero);
                                    // autoattack spell, not spell damage
                                    
                                    damageEntity(p, hero.getPlayer(), damage+damage2, EntityDamageEvent.DamageCause.PROJECTILE, false);
                                    
                                    event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(),0,false);
                                }
                            }
                        }
                        if(tr>1) {
                            int mana=SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 4, false);
                            if((hero.getMaxMana()-hero.getMana())<=mana) {
                                hero.setMana(hero.getMaxMana());
                            }
                            else {
                                hero.setMana(hero.getMana()+mana);
                            }
                            hero.getPlayer().sendMessage("§7You shatter your targets!");
                        }
                        else if(tr>0) {
                            hero.getPlayer().sendMessage("§7You shatter your target!");
                        }
                    
                        event.setDamage(0);
                        event.setCancelled(true);
                        return;
                    }
                }
                
                long duration = SkillConfigManager.getUseSetting(hero, skill, "slow-duration", 10000, false);
                int amplifier = SkillConfigManager.getUseSetting(hero, skill, "speed-multiplier", 2, false);
                
                //SlowEffect iceSlowEffect = new SlowEffect(skill, duration, amplifier, false, applyText, expireText, hero);
                SlowEffect iceSlowEffect = new SlowEffect(skill, duration, amplifier, false, "", "", hero);
                LivingEntity target = (LivingEntity) event.getEntity();
                if (target instanceof Player) {
                    Hero tHero = plugin.getCharacterManager().getHero((Player) target);
                    tHero.addEffect(iceSlowEffect);
                } /*else {
                    plugin.getEffectManager().addEntityEffect(target, iceSlowEffect);
                }*/

                //addSpellTarget(event.getEntity(), hero);
                // autoattack spell, not spell damage
                damageEntity(target, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.PROJECTILE, false);
                event.setCancelled(true);
            }
        }
    }
    
    public static List<Player> getNearbyPlayers(Location l, double radius) {
        int chunkRadius = radius < 16 ? 1 : ((int)(radius - (radius % 16))/16);
        List<Player> radiusEntities=new ArrayList<>();
        double radiussq = radius*radius;
            for (int chX = 0 -chunkRadius; chX <= chunkRadius; chX ++){
                for (int chZ = 0 -chunkRadius; chZ <= chunkRadius; chZ++){
                    int x=(int) l.getX(),y=(int) l.getY(),z=(int) l.getZ();
                    for (Entity e : new Location(l.getWorld(),x+(chX*16),y,z+(chZ*16)).getChunk().getEntities()){
                        if(e.getLocation().getWorld().equals(l.getWorld())) {
                            if (e.getLocation().distanceSquared(l) <= radiussq && e.getLocation().getBlock() != l.getBlock()) {
                                if(e instanceof Player) {
                                    radiusEntities.add((Player)e);
                                }
                            }
                        }
                    }
                }
            }
        return radiusEntities;
    }
    
}