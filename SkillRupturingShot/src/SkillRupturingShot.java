import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class SkillRupturingShot extends ActiveSkill {

    public SkillRupturingShot(Heroes plugin) {
        super(plugin, "RupturingShot");
        setDescription("You shoot a razor arrow, rupturing your target dealing $1 damage every time they move over $2 seconds.");
        setUsage("/skill rupturingshot");
        setArgumentRange(0, 0);
        setIdentifiers("skill rupturingshot");
        setTypes(SkillType.BUFF);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillDamageListener(this), plugin);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DURATION.node(), 6000);
        node.set(SkillSetting.PERIOD.node(), 500);
        node.set(SkillSetting.DAMAGE.node(), 3);
        node.set("jumpfall-damage", 2);
        return node;
    }

    @Override
    public void init() {
        super.init();
        setUseText("%hero% shoots a razor arrow!".replace("%hero%", "$1"));
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Arrow ra = hero.getPlayer().launchProjectile(Arrow.class);
        rarrows.put(ra.getEntityId(),hero.getPlayer().getName());
        ra.setVelocity(ra.getVelocity().multiply(2));
        
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
    
    static Map<Integer,String> rarrows = new HashMap<>();
    //static Map<Arrow,Hero> rarrows = new HashMap<>();

    public class SkillDamageListener implements Listener {

        private final Skill skill;

        public SkillDamageListener(Skill skill) {
            this.skill = skill;
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || !(event.getEntity() instanceof Player) || !(event instanceof EntityDamageByEntityEvent)) {
                return;
            }

            Player target = (Player) event.getEntity();
            EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
            
            if (!(subEvent.getDamager() instanceof Arrow)) {
                return;
            }

            Arrow arrow = (Arrow) subEvent.getDamager();
            if (!(arrow.getShooter() instanceof Player)) {
                return;
            }

            Player player = (Player) arrow.getShooter();
            Hero hero = plugin.getCharacterManager().getHero(player);

            //if (rarrows.containsKey(arrow)) {
            if (rarrows.containsKey(arrow.getEntityId())) {
                Hero hTarget = plugin.getCharacterManager().getHero(target);
                
                int duration = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 6000, false);
                
                event.setDamage(0);
                
                hTarget.addEffect(new RuptureEffect(skill, "Ruptured"+hero.getPlayer().getEntityId(), duration, arrow));
                hero.setCooldown("RupturingShot", System.currentTimeMillis() + SkillConfigManager.getUseSetting(hero, skill, "full-cooldown", 40000, false));
            }
        }
        
        
        @EventHandler(priority=EventPriority.HIGHEST)
        public void onMove(PlayerMoveEvent event) {
            if(event.getTo().getX()==event.getFrom().getX()) {
                if(event.getTo().getZ()==event.getFrom().getZ()) {
                    if(event.getTo().getY()==event.getFrom().getY()) {
                        return;
                    }
                }
            }
            
            Hero tHero = plugin.getCharacterManager().getHero(event.getPlayer());
            Iterator<Integer> it = rarrows.keySet().iterator();
            while(it.hasNext()) {
            //for(Arrow arrow:rarrows.keySet())
                //Arrow arrow = (Arrow)it.next();
                int arrow = it.next();
                //Hero hero = rarrows.get(arrow);
                Player pl = Bukkit.getPlayerExact(rarrows.get(arrow));
                if(pl==null) {
                    System.out.println("RUPTURESHOT DEBUG: NULL PLAYER. REMOVING ARROW FROM LIST");
                    it.remove();
                    continue;
                }
                if(!pl.isOnline()) {
                    System.out.println("RUPTURESHOT DEBUG: OFFLINE PLAYER. REMOVING ARROW FROM LIST");
                    it.remove();
                    continue;
                }
                Hero hero = plugin.getCharacterManager().getHero(pl);
                if(tHero.hasEffect("Ruptured"+hero.getPlayer().getEntityId())) {
                    if(hero.getCooldown("rupturecd") == null || hero.getCooldown("rupturecd") <= System.currentTimeMillis()) {
                        
                        long period = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.PERIOD, 500, false);
                        hero.setCooldown("rupturecd", period + System.currentTimeMillis());
                        tHero.getPlayer().getWorld().playEffect(tHero.getPlayer().getLocation(), Effect.POTION_BREAK, 245); 
                        addSpellTarget(tHero.getPlayer(), hero);
                        int damage = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 2, false);
                        if(event.getFrom().getY()!=event.getTo().getY()) {
                            damage+=SkillConfigManager.getUseSetting(hero, skill, "jumpfall-damage", 1, false);
                        }
                        damageEntity(event.getPlayer(), hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC, false);
                    }
                }
            }
        }
        
        /*
        @EventHandler(priority = EventPriority.MONITOR)
        public void onEntityShootBow(EntityShootBowEvent event) {
            
        }*/
    }
    
    @Override
    public String getDescription(Hero hero) {
        int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false);
        int period = SkillConfigManager.getUseSetting(hero, this, SkillSetting.PERIOD, 1000, false);
        int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 1, false);
        damage = damage * duration / period;
        return getDescription().replace("$1", damage + "").replace("$2", duration / 1000 + "");
    }
    
    public class RuptureEffect extends ExpirableEffect {
        Arrow arrow;
        
        public RuptureEffect(Skill skill, String name, long duration, Arrow ar) {
            super(skill,name,duration);
            arrow=ar;
        }
        
        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            //rarrows.remove(arrow);
            rarrows.remove(arrow.getEntityId());
        }
    }
    
}
