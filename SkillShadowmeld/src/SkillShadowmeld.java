import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.InvisibleEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class SkillShadowmeld extends ActiveSkill {

    public SkillShadowmeld(Heroes plugin) {
        super(plugin, "Shadowmeld");
        setDescription("You meld into the shadows turning invisible as long as you stand still");
        setUsage("/skill Shadowmeld");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Shadowmeld" });
        
        setTypes(SkillType.ILLUSION);
        
        Bukkit.getServer().getPluginManager().registerEvents(new MovementEntityListener(this),plugin);
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
        node.set(SkillSetting.DURATION.node(), 0);
        node.set(SkillSetting.MAX_DISTANCE.node(), 25);
        node.set(SkillSetting.COOLDOWN.node(), 60000);
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        int duration = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 0, false));
        duration = duration > 0 ? duration : 0;
        
        if(duration==0) {
            duration=3600000;
        }
        
        hero.addEffect(new com.herocraftonline.heroes.characters.effects.Effect(this, "ShadowMeld"));
        hero.addEffect(new InvisibleEffect(this, duration, hero.getPlayer().getDisplayName()+" melds into the shadows.", hero.getPlayer().getDisplayName()+" is revealed from the shadows!"));
        
        return SkillResult.NORMAL;
    }

    
public class MovementEntityListener implements Listener {
        private final Skill skill;
        public MovementEntityListener(Skill skill) {
            this.skill = skill;
        }
        
        @EventHandler()
        public void onPlayerMove(PlayerMoveEvent event) {
            Hero movingHero = plugin.getCharacterManager().getHero(event.getPlayer());
            
            if(event.getTo().getWorld().equals(event.getFrom().getWorld())) {
                if(event.getTo().distance(event.getFrom())>0.05) {
                    if(movingHero.hasEffect("ShadowMeld")) {
                        movingHero.removeEffect(movingHero.getEffect("Invisible"));
                        movingHero.removeEffect(movingHero.getEffect("ShadowMeld"));
                    }
                }
            }
        }
    }
}
