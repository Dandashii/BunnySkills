import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.left2craft.combatbase.event.HealEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SkillGroupHeal extends ActiveSkill implements Listener {

    public SkillGroupHeal(Heroes plugin) {
        super(plugin, "GroupHeal");
        setDescription("You restore $1 health to all nearby party members.");
        setUsage("/skill groupheal");
        setArgumentRange(0, 0);
        setIdentifiers("skill groupheal", "skill gheal");
        setTypes(SkillType.HEAL, SkillType.SILENCABLE);
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("heal-amount", 2);
        node.set(SkillSetting.RADIUS.node(), 5);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int healAmount = SkillConfigManager.getUseSetting(hero, this, "heal-amount", 2, false);
        
            //HeroRegainHealthEvent hrhEvent = new HeroRegainHealthEvent(hero, healAmount, this);
            //plugin.getServer().getPluginManager().callEvent(hrhEvent);
            //if (hrhEvent.isCancelled()) {
            //    Messaging.send(player, "Unable to heal the target at this time!");
            //    return SkillResult.CANCELLED;
            //}
            
            
            int iheal = hero.getPlayer().getHealth() + healAmount;
            HealEvent ihealevent = new HealEvent(hero.getPlayer(),hero.getPlayer(),iheal);
            Bukkit.getPluginManager().callEvent(ihealevent);
            iheal=ihealevent.getHealing();
            if (iheal>hero.getPlayer().getMaxHealth()) {
                iheal=hero.getPlayer().getMaxHealth();
            }
            if(!ihealevent.isCancelled()) {
                hero.getPlayer().setHealth(iheal);
            }
            //hero.syncHealth();
            
        int radius = (int) SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 5, false);
        // Heal party members near the caster
        for (Entity e : hero.getPlayer().getNearbyEntities(radius,5,radius)) {
            if(e instanceof Player) {
                Player friend = (Player)e;
                if(!damageCheck(hero.getPlayer(),friend)) {
                    if (!player.getWorld().equals(friend.getWorld())) {
                        continue;
                    }
                    if (!player.equals(friend)) {
                        continue;
                    }
                    Hero partyHero = plugin.getCharacterManager().getHero(friend);
                    if(!partyHero.hasEffectType(EffectType.INVULNERABILITY)) {
                        //HeroRegainHealthEvent hrhEvent2 = new HeroRegainHealthEvent(partyHero, healAmount, this);
                        //plugin.getServer().getPluginManager().callEvent(hrhEvent2);
                        //if (hrhEvent2.isCancelled()) {
                        //    Messaging.send(player, "Unable to heal the target at this time!");
                        //    return SkillResult.CANCELLED;
                        //}
                        int jheal = partyHero.getPlayer().getHealth() + healAmount;
                        HealEvent healevent = new HealEvent(partyHero.getPlayer(),hero.getPlayer(),jheal);
                        Bukkit.getPluginManager().callEvent(healevent);
                        jheal=healevent.getHealing();
                        if (jheal>partyHero.getPlayer().getMaxHealth()) {
                            jheal=partyHero.getPlayer().getMaxHealth();
                        }
                        if(!healevent.isCancelled()) {
                            partyHero.getPlayer().setHealth(jheal);
                        }
                        //partyHero.getPlayer().setHealth(jheal);
                        //partyHero.syncHealth();
                    }
                }
            }
        }
        
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

    @Override
    public String getDescription(Hero hero) {
        int heal = SkillConfigManager.getUseSetting(hero, this, "heal-amount", 2, false);
        return getDescription().replace("$1", heal + "");
    }
    
    @EventHandler
    public void onSkillUse(com.herocraftonline.heroes.api.events.SkillCompleteEvent event) {
        if(event.getResult()!=SkillResult.NORMAL) {
            return;
        }
        if(event.getSkill().equals(plugin.getSkillManager().getSkill("Pray"))) {
            long cd = (long) SkillConfigManager.getUseSetting(event.getHero(), plugin.getSkillManager().getSkill("Harmtouch"), SkillSetting.COOLDOWN, 5000, false) - SkillConfigManager.getUseSetting(event.getHero(), plugin.getSkillManager().getSkill("Harmtouch"), SkillSetting.COOLDOWN_REDUCE, 0, false);
            event.getHero().setCooldown("Harmtouch", cd + System.currentTimeMillis());
        }
        else if(event.getSkill().equals(plugin.getSkillManager().getSkill("Harmtouch"))) {
            long cd = (long) SkillConfigManager.getUseSetting(event.getHero(), plugin.getSkillManager().getSkill("Pray"), SkillSetting.COOLDOWN, 5000, false) - SkillConfigManager.getUseSetting(event.getHero(), plugin.getSkillManager().getSkill("Pray"), SkillSetting.COOLDOWN_REDUCE, 0, false);
            event.getHero().setCooldown("Pray", cd + System.currentTimeMillis());
        }
    }
    
}