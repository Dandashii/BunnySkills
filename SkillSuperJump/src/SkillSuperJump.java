import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;


public class SkillSuperJump extends ActiveSkill {
    private String applyText;
    private String removeText;

    public SkillSuperJump(Heroes plugin) {
        super(plugin, "SuperJump");
        setDescription("You launch into the air, and float safely to the ground.");
        setUsage("/skill superjump");
        setArgumentRange(0, 0);
        setIdentifiers("skill superjump");
        Bukkit.getServer().getPluginManager().registerEvents(new DeathFromAboveListener(this), plugin);
        setTypes(SkillType.MOVEMENT, SkillType.PHYSICAL);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DURATION.node(), 5000);
        node.set("jump-force", 4.0);
        
        
        node.set(SkillSetting.DURATION.node(), 10000);
        node.set("duration-increase", 0);
        node.set(SkillSetting.RADIUS.node(), 5);
        node.set("radius-increase", 0);
        node.set("damage-multiplier", 1.0);
        node.set("damage-multi-increase", 0);
        node.set("safefall", "true");
        node.set("exp-per-player-hit", 0);
        node.set("exp-per-creature-hit", 0);
        node.set(SkillSetting.APPLY_TEXT.node(), "%hero% is ready to pounce!");
        node.set("remove-text", "%hero% is not ready to pounce anymore!");
        
        
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        applyText = SkillConfigManager.getUseSetting(null, this, SkillSetting.APPLY_TEXT.node(), "%hero% is ready to pounce!").replace("%hero%", "$1");
        removeText = SkillConfigManager.getUseSetting(null, this, "remove-text", "%hero% is not ready to pounce anymore!").replace("%hero%", "$1");
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        float jumpForce = (float) SkillConfigManager.getUseSetting(hero, this, "jump-force", 1.0, false);
        Vector v1 = new Vector(0, jumpForce, 0);
        Vector v = player.getVelocity().add(v1);
        player.setVelocity(v);
        player.setFallDistance(-8f);
        int duration = (int) SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 5000, false);
        
        
        //hero.addEffect(new SafeFallEffect(this, duration));
        hero.addEffect(new DeathFromAboveEffect(this, duration));
        
        broadcastExecuteText(hero);

        return SkillResult.NORMAL;
    }

    @Override
    public String getDescription(Hero hero) {
        return getDescription();
    }
    
        public class DeathFromAboveEffect extends ExpirableEffect {
        public DeathFromAboveEffect(Skill skill, long duration) {
            super(skill, "DeathFromAbove", duration);
            this.types.add(EffectType.BENEFICIAL);
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.PHYSICAL);
        }
        @Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            broadcast(hero.getPlayer().getLocation(), applyText, hero.getPlayer().getDisplayName());
        }
        
        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            broadcast(hero.getPlayer().getLocation(), removeText, hero.getPlayer().getDisplayName());
        }
    }
    
    public class DeathFromAboveListener implements Listener {
        private Skill skill;
        public DeathFromAboveListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || event.getDamage() == 0 || !(event.getEntity() instanceof Player)
                    || event.getCause() != EntityDamageEvent.DamageCause.FALL) {
                return;
            }
            Player player = (Player) event.getEntity();
            Hero hero = plugin.getCharacterManager().getHero(player);
            if (!hero.hasEffect("DeathFromAbove")) {
                return;
            }
            int radius = (int) (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.RADIUS.node(), 5, false) +
                    (SkillConfigManager.getUseSetting(hero, skill, "radius-increase", 0.0, false) * hero.getSkillLevel(skill)));
            radius = radius > 0 ? radius : 0;
            int damage = event.getDamage();
            double damageMulti = (SkillConfigManager.getUseSetting(hero, skill, "damage-multiplier", 1.0, false) +
                    (SkillConfigManager.getUseSetting(hero, skill, "damage-multi-increase", 0.0, false) * hero.getSkillLevel(skill)));
            damageMulti = damageMulti > 0 ? damageMulti : 0;
            damage = (int) (damage * damageMulti);
            double expPlayer = SkillConfigManager.getUseSetting(hero, skill, "exp-per-player-hit", 0, false);
            double expCreature = SkillConfigManager.getUseSetting(hero, skill, "exp-per-creature-hit", 0, false);
            double exp = 0;
            for (Entity e : player.getNearbyEntities(radius,radius,radius)) {
                if (e instanceof Player && !(e.equals(player))) {
                    Player p = (Player) e;
                    damageEntity(p, player, damage, EntityDamageEvent.DamageCause.MAGIC);
                    //p.damage(damage, player);
                    if (expPlayer > 0) {
                        exp += expPlayer;
                    }
                } else if (e instanceof Creature) {
                    Creature c = (Creature) e;
                    damageEntity(c, player, damage, EntityDamageEvent.DamageCause.MAGIC);
                    //c.damage(damage, player);
                    if (expCreature > 0) {
                        exp += expCreature;
                    }
                }
            }
            if (exp > 0) {
                if (hero.hasParty()) {
                    hero.getParty().gainExp(exp, HeroClass.ExperienceType.SKILL, player.getLocation());
                } else {
                    hero.gainExp(exp, HeroClass.ExperienceType.SKILL, player.getLocation());
                }
            }
            if (SkillConfigManager.getUseSetting(hero, skill, "safefall", "true").equals("true")) {
                event.setDamage(0);
                event.setCancelled(true);
            }
        }
    }
    
}