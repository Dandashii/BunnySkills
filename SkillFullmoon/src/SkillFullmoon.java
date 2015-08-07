import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class SkillFullmoon extends ActiveSkill {

    public SkillFullmoon(Heroes plugin) {
        super(plugin, "Fullmoon");
        setDescription("Draws nearby players into the night, empowering the caster");
        setUsage("/skill Fullmoon");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Fullmoon" });
        
        
        setTypes(SkillType.KNOWLEDGE);
        //registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
        
        Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(this),plugin);
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
        
        node.set(SkillSetting.DURATION.node(), 10000);
        node.set("speed", 2);
        node.set(SkillSetting.COOLDOWN.node(), 20000);
        node.set("attack-cooldown", 2000);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        
        long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false);
        int speed = SkillConfigManager.getUseSetting(hero, this, "speed", 2, false);
        
        //final long fullt = player.getWorld().getFullTime()+(duration/50);
        //player.getWorld().setFullTime(18000);
        
        player.setPlayerTime(18000,false);
        
        //hero.addEffect(new com.herocraftonline.heroes.characters.effects.common.NightvisionEffect(this, duration, "", ""));
        
        hero.addEffect(new com.herocraftonline.heroes.characters.effects.common.QuickenEffect(this,"Wolf's haste",duration,speed,"",""));
        hero.addEffect(new com.herocraftonline.heroes.characters.effects.Effect(this,"NightTime"));
        
            if (player.isOp()) {
                player.performCommand("disguise wolf");
            } else {
                player.setOp(true);
                player.performCommand("disguise wolf");
                player.setOp(false);
            }
        
        final Hero fhero = hero;
        final Player wp = player;
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                new Runnable() {
                    @Override
                    public void run() {
                        //wp.getWorld().setFullTime(fullt);
                        fhero.removeEffect(fhero.getEffect("NightTime"));
                        wp.resetPlayerTime();
                        if (wp.isOp()) {
                            wp.performCommand("undisguise");
                        } else {
                            wp.setOp(true);
                            wp.performCommand("undisguise");
                            wp.setOp(false);
                        }
                    }
                }
        ,(duration/50));
        
        final List<Entity> flist = player.getNearbyEntities(40,20,40);
        for(Entity e:flist) {
            if(e instanceof Player) {
                final Player asd = (Player)e;
                asd.setPlayerTime(18000,false);
                Hero nearher2 = plugin.getCharacterManager().getHero(asd);
                if(!nearher2.hasEffect("NightTime")) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                            new Runnable() {
                                @Override
                                public void run() {
                                    Hero nearher = plugin.getCharacterManager().getHero(asd);
                                    if(!nearher.hasEffect("NightTime")) {
                                        nearher.getPlayer().resetPlayerTime();
                                    }
                                }
                            }
                    ,(duration/50));
                }
            }
        }
        
        return SkillResult.NORMAL;
    }
    
        
public class SkillEntityListener implements Listener {
        private final Skill skill;
        public SkillEntityListener(Skill skill) {
            this.skill = skill;
        }
        
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            
            //if (event.getCause() != DamageCause.ENTITY_ATTACK || event.isCancelled() || event.getDamage() == 0 || !(event.getEntity() instanceof Player) || !(event instanceof EntityDamageEvent))
            if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.isCancelled() || event.getDamage() == 0 || !(event instanceof EntityDamageEvent)) {
                return;
            }
            EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event;
            if (edby.getDamager() instanceof Player) {
                Player player = (Player) edby.getDamager();
                Hero hero = plugin.getCharacterManager().getHero(player);
                
                if (hero.getCooldown("WolfBleed") == null || hero.getCooldown("WolfBleed") <= System.currentTimeMillis()) {
                    if(hero.hasEffect("NightTime")) {
                        
                        //hero.getPlayer().getWorld().playEffect(hero.getPlayer().getLocation(), Effect.POTION_BREAK, 245);  
                        event.getEntity().getWorld().playEffect(new Location(event.getEntity().getWorld(),event.getEntity().getLocation().getX(),event.getEntity().getLocation().getY(),event.getEntity().getLocation().getZ()), Effect.POTION_BREAK, 245);  
                        
                        
                    long attcd = SkillConfigManager.getUseSetting(hero, plugin.getSkillManager().getSkill("Fullmoon"), "attack-cooldown", 2000, false);
                    
                    
                    
                    hero.setCooldown("WolfBleed", attcd + System.currentTimeMillis());
                    if(event.getEntity() instanceof LivingEntity) {
                            damageEntity((LivingEntity)event.getEntity(),hero.getPlayer(),(int)Math.round(event.getDamage()),EntityDamageEvent.DamageCause.CONTACT);
                        }
                    }
                }
            }
        }
    }
}
