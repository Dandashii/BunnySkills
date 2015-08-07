import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class SkillCraneSweep extends TargettedSkill {

    public SkillCraneSweep(Heroes plugin) {
        super(plugin, "CraneSweep");
        setDescription("Dashes through your target, dealing damage");
        setUsage("/skill CraneSweep");
        
        setArgumentRange(0, 1);
        setIdentifiers("skill CraneSweep");
        
        setTypes(SkillType.DAMAGING);
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
        
        node.set(SkillSetting.MAX_DISTANCE.node(), 25);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();
        if(target == null) {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        if(target instanceof Player) {
            if (player.equals(((Player) target))) {
            return SkillResult.FAIL;
            }
        }
        if (!com.herocraftonline.heroes.characters.skill.Skill.damageCheck(player,target)) {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        addSpellTarget(target, hero);
        
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 1, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        
        damageEntity(target, player, damage, DamageCause.MAGIC);
        
        Vector thrv = target.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(1.5).setY(0.3);
        Location asdd = target.getLocation();
        asdd.setYaw(player.getLocation().getYaw()+180);
        player.teleport(asdd);
        player.getLocation().setYaw(asdd.getYaw());
        hero.getPlayer().setVelocity(thrv);
        
        
        
        if(hero.hasEffect("Crane's Swiftness")) {
            for(int i=1;hero.getPlayer().getFoodLevel()>=7 && i<4;i++) {
            final Player pla = player;
            final Hero her = hero;
            final LivingEntity tar = target;
            final Skill skill = this;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
            public void run() {
                    if(!damageCheck(pla, tar)) {
                        return;
                    }
                    Vector thrv = tar.getLocation().subtract(pla.getLocation()).toVector().normalize().multiply(1.5).setY(0.3);
                    Location asdd = tar.getLocation();
                    float yawbro = her.getPlayer().getLocation().getYaw()+180;
                    asdd.setYaw(yawbro);
                    pla.teleport(asdd);
                    pla.getLocation().setYaw(yawbro);
                    her.getPlayer().setVelocity(thrv);
                    int d2 = (int) SkillConfigManager.getUseSetting(her, skill, "bonus-damage", 1, false);
                    damageEntity(tar, pla, d2, DamageCause.MAGIC);
                }
            }, i*16);
            hero.getPlayer().setFoodLevel(hero.getPlayer().getFoodLevel()-2);
            }
        }
        return SkillResult.NORMAL;
    }
}