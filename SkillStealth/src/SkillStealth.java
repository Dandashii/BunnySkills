import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class SkillStealth extends ActiveSkill implements Listener {

    public SkillStealth(Heroes plugin) {
        super(plugin, "Stealth");
        setDescription("You blend into the shadows. Can only be used out of combat while on full health and enemies within $1 blocks can see you.");
        setUsage("/skill stealth");
        setArgumentRange(0, 0);
        setIdentifiers("skill stealth");
        setNotes("Note: Taking damage removes the effect");
        setTypes(SkillType.ILLUSION, SkillType.BUFF, SkillType.COUNTER, SkillType.STEALTHY);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DURATION.node(), 20000);
        node.set("reveal-distance", 5);
        node.set("reveal-distance-decrease", 0.05);
        node.set("stealth-slow-amp", 2);
        return node;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        if(hero.isInCombat()) {
            hero.getPlayer().sendMessage("§7You cannot stealth while in combat!");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        if(hero.getPlayer().getHealth()<hero.getPlayer().getMaxHealth()) {
            hero.getPlayer().sendMessage("§7You cannot stealth while not full health! ("+((int)(100.0*hero.getPlayer().getHealth()/(hero.getPlayer().getMaxHealth()*1.0)))+"%)");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        
        long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 20000, false);
        Player player = hero.getPlayer();
        player.getWorld().playEffect(player.getLocation(), org.bukkit.Effect.SMOKE, 4);
        
        float distance = SkillConfigManager.getUseSetting(hero, this, "reveal-distance", 5, false);
        distance -= SkillConfigManager.getUseSetting(hero, this, "reveal-distance-decrease", 0.05, false) * hero.getLevel();
        //distancesq -= 0.05 * hero.getLevel();
        
        int slowamp = SkillConfigManager.getUseSetting(hero, this, "stealth-slow-amp", 2, false);
        
        hero.addEffect(new StealthEffect(this, duration, distance, slowamp));

        //broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
    
    /*
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onStealthTarget(EntityTargetLivingEntityEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(!(event.getTarget() instanceof Player)) {
            return;
        }
        
        Player player = (Player)event.getTarget();
        Hero hero = plugin.getCharacterManager().getHero(player);
        
        if(!hero.hasEffect("Stealthed")) {
            return;
        }
        
        float distancesq = SkillConfigManager.getUseSetting(hero, this, "reveal-distance", 5, false);
        distancesq -= SkillConfigManager.getUseSetting(hero, this, "reveal-distance-decrease", 0.05, false) * hero.getLevel();
        
        distancesq *= distancesq;
        
        if(event.getEntity().getLocation().distanceSquared(event.getTarget().getLocation())>=distancesq) {
            event.setCancelled(true);
            return;
        }
        
    }*/
    
    @Override
    public String getDescription(Hero hero) {
        return getDescription();
    }
}