import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.effects.common.CombustEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;

public class SkillFireArrow extends ActiveSkill
{
    public SkillFireArrow(Heroes plugin) {
        super(plugin, "FireArrow");
        setDescription("Your arrows will deal fire damage to the target, but they will drain $1 mana per shot!");
        setUsage("/skill firearrow");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill firearrow", "skill farrow" });
        setTypes(new SkillType[] { SkillType.FIRE, SkillType.BUFF });
        Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(this), plugin);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("mana-per-shot", Integer.valueOf(1));
        node.set("fire-ticks", Integer.valueOf(100));
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        if (hero.hasEffect("FireArrowBuff")) {
            hero.removeEffect(hero.getEffect("FireArrowBuff"));
            hero.getPlayer().sendMessage("§7You disable fire arrows!");
            return SkillResult.SKIP_POST_USAGE;
        }
        hero.addEffect(new Effect(this,"FireArrowBuff"));
            hero.getPlayer().sendMessage("§7You enable fire arrows!");
        //broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

    @Override
    public String getDescription(Hero hero) {
        int mana = SkillConfigManager.getUseSetting(hero, this, "mana-per-shot", 1, false);
        return getDescription().replace("$1", mana + "");
    }

    public class SkillEntityListener implements Listener {
        private final Skill skill;

        public SkillEntityListener(Skill skill) {
            this.skill = skill;
        }

        /*@EventHandler
        public void onEntityShoot(EntityShootBowEvent event) {
            if ((event.isCancelled()) || (!(event.getEntity() instanceof Player))) {
                return;
            }
            Hero hero = SkillFireArrow.this.plugin.getCharacterManager().getHero((Player)event.getEntity());
            if (hero.hasEffect("FireArrowBuff")) {
                event.getProjectile().setFireTicks(300);
            }
        }*/

        @EventHandler(priority=EventPriority.MONITOR)
        public void onEntityShootBow(EntityShootBowEvent event) {
            if ((event.isCancelled()) || (!(event.getEntity() instanceof Player)) || (!(event.getProjectile() instanceof Arrow))) {
                return;
            }
            Hero hero = plugin.getCharacterManager().getHero((Player)event.getEntity());
            if (hero.hasEffect("FireArrowBuff")) {
                int mana = SkillConfigManager.getUseSetting(hero, this.skill, "mana-per-shot", 1, true);
                if (hero.getMana() < mana) {
                    hero.removeEffect(hero.getEffect("FireArrowBuff"));
                    ((Player)event.getEntity()).sendMessage("§7You do not have enough mana to shoot a fire arrow!");
                }
                else {
                    hero.setMana(hero.getMana() - mana);
                    event.getProjectile().setFireTicks(300);
                    hero.addEffect(new ExpirableEffect(skill,"NextArrowFire"+event.getProjectile().getEntityId(),12000));
                }
            }
        }
        
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if ((event.isCancelled()) || (!(event instanceof EntityDamageByEntityEvent)) || (!(event.getEntity() instanceof LivingEntity))) {
                return;
            }

            Entity projectile = ((EntityDamageByEntityEvent)event).getDamager();
            if ((!(projectile instanceof Arrow)) || (!(((Projectile)projectile).getShooter() instanceof Player))) {
                return;
            }

            Player player = (Player)((Projectile)projectile).getShooter();
            Hero hero = plugin.getCharacterManager().getHero(player);
            if (!hero.hasEffect("NextArrowFire"+projectile.getEntityId())) {
                return;
            }
            hero.removeEffect(hero.getEffect("NextArrowFire"+projectile.getEntityId()));
            LivingEntity entity = (LivingEntity)event.getEntity();
            if (!Skill.damageCheck(player, entity)) {
                event.setCancelled(true);
                return;
            }
            
            //int fireTicks = SkillConfigManager.getUseSetting(hero, this.skill, "fire-ticks", 100, false);
            //entity.setFireTicks(fireTicks);
            
            int dam = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 1, true);
            //event.setDamage(event.getDamage()+dam);
            
            Skill.damageEntity(entity, player, dam, EntityDamageEvent.DamageCause.FIRE, false);
            
            entity.setFireTicks(5);

            SkillFireArrow.this.plugin.getCharacterManager().getCharacter(entity).addEffect(new CombustEffect(this.skill, player));
        }
    }
}