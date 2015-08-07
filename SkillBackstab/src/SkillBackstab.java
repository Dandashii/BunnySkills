import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.left2craft.combatbase.event.DirectDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class SkillBackstab extends PassiveSkill {
    private String useText;

    public SkillBackstab(Heroes plugin) {
        super(plugin, "Backstab");
        setDescription("You have a $1% chance to deal $2% bonus damage when attacking from behind!");
        setArgumentRange(0, 0);
        setTypes(new SkillType[] { SkillType.PHYSICAL, SkillType.BUFF });
        setEffectTypes(new EffectType[] { EffectType.BENEFICIAL, EffectType.PHYSICAL });
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroesListener(this), plugin);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("weapons", Util.swords);
        node.set("attack-bonus", Double.valueOf(1.5D));
        node.set("attack-chance", Double.valueOf(0.5D));
        node.set("sneak-bonus", Double.valueOf(2.0D));
        node.set("sneak-chance", Double.valueOf(1.0D));
        node.set(SkillSetting.USE_TEXT.node(), "%hero% backstabbed %target%!");
        return node;
    }

    @Override
    public void init() {
        super.init();
        this.useText = SkillConfigManager.getRaw(this, SkillSetting.USE_TEXT, "%hero% backstabbed %target%!").replace("%hero%", "$1").replace("%target%", "$2");
    }

    private void broadcastExecuteText(Hero hero, Entity target)
    {
      Player player = hero.getPlayer();
      String targetName = (target instanceof Player) ? ((Player)target).getName() : target.getClass().getSimpleName().substring(5);
      broadcast(player.getLocation(), this.useText, new Object[] { player.getDisplayName(), target == player ? "himself" : targetName });
    }

    @Override
    public String getDescription(Hero hero) {
        double chance = SkillConfigManager.getUseSetting(hero, this, "attack-chance", 0.5D, false);
        double percent = SkillConfigManager.getUseSetting(hero, this, "attack-bonus", 1.5D, false);
        return getDescription().replace("$1", Util.stringDouble(chance * 100.0D)).replace("$2", Util.stringDouble(percent * 100.0D));
    }

    public class SkillHeroesListener implements Listener {
        private final Skill skill;

        public SkillHeroesListener(Skill skill)
        {
          this.skill = skill;
        }
        @EventHandler
        public void onWeaponDamage(DirectDamageEvent event) {
            if(!event.getDamageType().isBaseAttack()) {
                return;
            }
            if(event.getDamageType().isRanged()) {
                return;
            }
            if(event.getEntity().getNoDamageTicks()>event.getEntity().getMaximumNoDamageTicks()/2.0) {
                return;
            }
            if (!(event.getAttacker() instanceof Player)) {
                return;
            }
            Player player = (Player)event.getAttacker();
            Hero hero = plugin.getCharacterManager().getHero(player);
            if (hero.hasEffect(SkillBackstab.this.getName())) {
                ItemStack item = player.getItemInHand();
                
                if (!SkillConfigManager.getUseSetting(hero, this.skill, "weapons", Util.swords).contains(item.getType().name())) {
                    return;
                }

                if (event.getEntity().getLocation().getDirection().dot(player.getLocation().getDirection()) <= 0.0D) {
                    return;
                }

                if ((hero.hasEffect("Sneak")) && (Util.nextRand() < SkillConfigManager.getUseSetting(hero, this.skill, "sneak-chance", 1.0D, false))) {
                    //event.setDamage((int)(event.getDamage() * SkillConfigManager.getUseSetting(hero, this.skill, "sneak-bonus", 2.0D, false)));
                    event.multiplyDamage(SkillConfigManager.getUseSetting(hero, this.skill, "sneak-bonus", 2.0D, false));
                }
                else if (Util.nextRand() < SkillConfigManager.getUseSetting(hero, this.skill, "attack-chance", 0.5D, false)) {
                    //event.setDamage((int)(event.getDamage() * SkillConfigManager.getUseSetting(hero, this.skill, "attack-bonus", 1.5D, false)));
                    event.multiplyDamage(SkillConfigManager.getUseSetting(hero, this.skill, "attack-bonus", 1.5D, false));
                }

                //Entity target = event.getEntity();
                //SkillBackstab.this.broadcastExecuteText(hero, target);
            }
        }
    }
}