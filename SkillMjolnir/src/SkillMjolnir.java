import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;

public class SkillMjolnir extends ActiveSkill implements Listener {
    public SkillMjolnir(Heroes plugin) {
        super(plugin, "Mjolnir");
        setDescription("You throw Mjolnir forth, pushing away all enemies for a few seconds");
        setUsage("/skill Mjolnir");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill Mjolnir"});
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
        node.set("Strength", 0.6);
        node.set(SkillSetting.RADIUS.node(), 8);
        node.set(SkillSetting.RADIUS_INCREASE.node(), 0);
        node.set(SkillSetting.MAX_DISTANCE.node(), 30);
        node.set(SkillSetting.MAX_DISTANCE_INCREASE.node(), 0);
        node.set(SkillSetting.DURATION.node(), 5000);
        node.set(SkillSetting.DURATION_INCREASE.node(), 0);
        node.set(SkillSetting.PERIOD.node(), 1000);
        
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        
        int range = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE, 30, false);
        range += SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE, 0, false) * hero.getSkillLevel(this);
        
        broadcastExecuteText(hero);
        Block wTargetBlock = player.getTargetBlock(null, range).getRelative(
                        BlockFace.UP);
        
        int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 8, false);
        radius += SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS_INCREASE, 0, false) * hero.getSkillLevel(this);
        
        int strength = SkillConfigManager.getUseSetting(hero, this, "Strength", 30, false);
        
        long dur = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 5000, false);
        dur += SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, 0, false) * hero.getSkillLevel(this);
        
        long per = SkillConfigManager.getUseSetting(hero, this, SkillSetting.PERIOD, 1000, false);
        
        hero.addEffect(new KnockbackspamEffect(this, "MjolnirEffect", wTargetBlock.getLocation(), radius, strength, per, dur));
        
        return SkillResult.NORMAL;
    }
    

}