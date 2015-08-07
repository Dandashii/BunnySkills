import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SkillVortex extends TargettedSkill {

    public SkillVortex(Heroes plugin) {
        super(plugin, "Vortex");
        setDescription("Teleports your target to your position and knocks him in the air");
        setUsage("/skill Vortex");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Vortex" });
        
        setTypes(SkillType.TELEPORT, SkillType.KNOWLEDGE, SkillType.SILENCABLE);
        //registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
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
        node.set(SkillSetting.RADIUS.node(), 1);
        node.set("strength", 2);
        
        node.set(SkillSetting.MAX_DISTANCE.node(), 25);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        if(!damageCheck(hero.getPlayer(), target)) {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        Player player = hero.getPlayer();
        if(target instanceof Player) {
            if(((Player)target).equals(player)) {
                return SkillResult.INVALID_TARGET_NO_MSG;
            }
        }
        if(target == null) {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 15, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        
        
        Location location = target.getLocation();
        
        double pladistance = location.distance(player.getLocation());
        
        if(pladistance<=distance) {
        
        Messaging.send(hero.getPlayer(), "Your vortex is charging");
        
        final Location floc = player.getLocation();
        final LivingEntity ftarget = target;
        final Hero fhero = hero;
        
        if(target instanceof Player) {
            Hero tHero = plugin.getCharacterManager().getHero((Player)target);
            tHero.addEffect(new ExpirableEffect(this, "Vortex"+hero.getPlayer().getEntityId(), 2500));
        }
        
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
        public void run() {
                if(ftarget instanceof Player) {
                    Hero tHero = plugin.getCharacterManager().getHero((Player)ftarget);
                    if(!tHero.hasEffect("Vortex"+fhero.getPlayer().getEntityId())) {
                        Messaging.send(fhero.getPlayer(), "Your vortex cancelled");
                        return;
                    }
                    tHero.removeEffect(tHero.getEffect("Vortex"+fhero.getPlayer().getEntityId()));
                    Messaging.send((Player) ftarget, "You feel the vortex pull you in!");
                }
                ftarget.teleport(floc);
                Vector thrv = new Vector(0,1.08,0);
                ftarget.setVelocity(thrv);
            }
        }, (long)(2.0*20.0));
        
        //Vector thrvv = player.getLocation().subtract(target.getLocation()).toVector().multiply(strength*0.1);
        
        //Vector thrv = player.getLocation().subtract(target.getLocation()).toVector();
        //thrv.setY(thrv.getY()*0);
        
        }
        return SkillResult.NORMAL;
    }

}