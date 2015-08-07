import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;
import net.minecraft.server.MathHelper;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SkillShadowstep extends TargettedSkill {

    private String ShadowstepText;

    public SkillShadowstep(Heroes plugin) {
        super(plugin, "Shadowstep");
        setDescription("Teleports you behind your target.");
        setUsage("/skill Shadowstep");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Shadowstep" });
        
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

        if(target == null ) {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        if(target instanceof Player) {
            if(((Player)target).equals(player)) {
                return SkillResult.INVALID_TARGET_NO_MSG;
            }
        }
        
        if(!damageCheck(hero.getPlayer(), target)) {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        int direction = MathHelper.floor((double)((target.getEyeLocation().getYaw() * 4F) / 360F) + 0.5D) & 3;
        
        double zz=target.getLocation().getZ();
        double xx=target.getLocation().getX();
        
        switch(direction)
        {
            case 0: //Direction 0 = +Z
            {
                zz--;
                break;
            }
            case 1: //Direction 1 = -X
            {
                xx++;  
                break;
            }
            case 2: //Direction 2 = -Z
            {
                zz++;  
                break;
            }
            case 3: //Direction 3 = +X
            {
                xx--; 
                break;
            }
        }

        Location location = target.getLocation();
        location.setX(xx);
        location.setZ(zz);
        
        Location location2 = location;
        location2.setY(location2.getY()+1);
        
        
        if(target instanceof Player) {
            if (player.equals(((Player) target))){
                return SkillResult.INVALID_TARGET_NO_MSG;
            }
        }
        
        
        if((location.getWorld().getBlockAt(location).isEmpty()&&location.getWorld().getBlockAt(location2).isEmpty())||(location.getWorld().getBlockAt(location).isLiquid()&&location.getWorld().getBlockAt(location2).isLiquid())) {
            hero.getPlayer().teleport(location);
        }
        else {
            hero.getPlayer().teleport(target.getLocation());
        }
        if(target instanceof Player) {
            Messaging.send((Player) target, ShadowstepText);
        }
        
        return SkillResult.NORMAL;
    }

}