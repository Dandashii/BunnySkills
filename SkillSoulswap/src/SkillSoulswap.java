import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftFirework;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;

public class SkillSoulswap extends TargettedSkill implements Listener {
    public SkillSoulswap(Heroes plugin) {
        super(plugin, "Soulswap");
        setDescription("You swap positions with your target");
        setUsage("/skill Soulswap");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill Soulswap"});
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.DARK, SkillType.SUMMON, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        String description = getDescription();
        
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
        //node.set("init-delay", 500);
        //node.set("2nd-delay", 2000);
        node.set(SkillSetting.MAX_DISTANCE.node(), 35);
        
        return node;
    }
    
    public void fireWkball(Location loc) {
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
    
    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        if(hero.getEntity().getEntityId()==(target.getEntityId())) {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        if(!Skill.damageCheck(hero.getPlayer(), target)) {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        fireWkball(target.getLocation());
        fireWkball(hero.getPlayer().getLocation());
        
        Location loc = target.getLocation().clone();
        loc.setYaw(target.getLocation().subtract(hero.getPlayer().getLocation()).getYaw());
        loc.setPitch(target.getLocation().subtract(hero.getPlayer().getLocation()).getPitch());
        
        target.teleport(hero.getPlayer().getLocation());
        
        hero.getPlayer().teleport(loc);
        
        broadcastExecuteText(hero,target);
        return SkillResult.NORMAL;
    }
}