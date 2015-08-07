import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillArtofwar extends PassiveSkill {

    public SkillArtofwar(Heroes plugin) {
        super(plugin, "Artofwar");
        setDescription("Grants you the effect of monk stances");
        setTypes(SkillType.UNBINDABLE);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
        
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
        
        node.set(SkillSetting.COOLDOWN.node(), 1000);
        node.set(SkillSetting.DAMAGE.node(), 50);
        node.set(SkillSetting.DAMAGE_INCREASE.node(), 0);
        node.set("hunger-per-attack", 2);
        node.set("hunger-increase", 0);
        node.set("health-per-attack", 2);
        node.set("health-increase", 0);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            
            //if (event.getCause() != DamageCause.ENTITY_ATTACK || event.isCancelled() || event.getDamage() == 0 || !(event.getEntity() instanceof Player) || !(event instanceof EntityDamageEvent))
            if (event.getCause() != DamageCause.ENTITY_ATTACK || event.isCancelled() || event.getDamage() == 0 || !(event instanceof EntityDamageEvent)) {
                return;
            }
            EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event;
            if (edby.getDamager() instanceof Player) {
                Player player = (Player) edby.getDamager();
                Hero hero = plugin.getCharacterManager().getHero(player);
                
                if(!(hero.getPlayer().getItemInHand().getType()==Material.AIR || hero.getPlayer().getItemInHand().getType()==Material.STICK)) {
                    if(hero.getHeroClass().getName().toUpperCase().equals("MONK")) {
                        event.setDamage((int)Math.round(event.getDamage()*0.1));
                    }
                }
                
                if (hero.hasEffect("DragonStanceEffect")) {
                    if (hero.getPlayer().getItemInHand().getType()==Material.AIR || hero.getPlayer().getItemInHand().getType()==Material.STICK) {
                        if (hero.getCooldown("DragonStanceEffect") == null || hero.getCooldown("DragonStanceEffect") <= System.currentTimeMillis()) {
                            int hunger = (int) (SkillConfigManager.getUseSetting(hero, skill, "hunger-per-attack", 2, false) +
                                    (SkillConfigManager.getUseSetting(hero, skill, "hunger-increase", 0.0, false) * hero.getSkillLevel(skill)));
                            hunger = hunger > 0 ? hunger : 0;
                            long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN.node(), 500, false) -
                                    (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN_REDUCE.node(), 0.0, false) * hero.getSkillLevel(skill)));
                            cooldown = cooldown > 0 ? cooldown : 0;
                            int damage = (int) (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE.node(), 50, false) -
                                    (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(skill)));
                            damage = damage > 0 ? damage : 0;
                            
                            if(player.getFoodLevel()>=15) {
                                return;
                            }
                            
                            if(player.getFoodLevel()+hunger>15) {
                                player.setFoodLevel(15);
                            }
                            else {
                                player.setFoodLevel(player.getFoodLevel()+hunger);
                            }
                            
                            hero.setCooldown("DragonStanceEffect", cooldown + System.currentTimeMillis());
                            hero.getPlayer().getWorld().playEffect(hero.getPlayer().getLocation(), Effect.POTION_BREAK, 245);  
                            
                            hero.addEffect(new com.herocraftonline.heroes.characters.effects.ExpirableEffect(skill, "Dragon's Strength", cooldown*2));
                            
                            double thung;
                            if(player.getFoodLevel()<=15 && player.getFoodLevel()>=6) {
                                thung=16-player.getFoodLevel();
                            }
                            else {
                                thung=0;
                            }
                            thung = thung*0.01*damage;
                            
                            //event.setDamage((int)Math.round(event.getDamage()*thung));
                            if(event.getEntity() instanceof LivingEntity) {
                                damageEntity((LivingEntity)event.getEntity(),hero.getPlayer(),(int)Math.round(event.getDamage()*thung),EntityDamageEvent.DamageCause.FIRE);
                            }
                            
                            return;
                        }
                    }
                }

                if (hero.hasEffect("CraneStanceEffect")) {
                    if (hero.getPlayer().getItemInHand().getType()==Material.AIR || hero.getPlayer().getItemInHand().getType()==Material.STICK) {
                        if (hero.getCooldown("CraneStanceEffect") == null || hero.getCooldown("CraneStanceEffect") <= System.currentTimeMillis()) {
                            int health = (int) (SkillConfigManager.getUseSetting(hero, skill, "health-per-attack", 2, false) +
                                    (SkillConfigManager.getUseSetting(hero, skill, "health-increase", 0.0, false) * hero.getSkillLevel(skill)));
                            health = health > 0 ? health : 0;
                            int hunger = (int) (SkillConfigManager.getUseSetting(hero, skill, "hunger-per-attack", 2, false) +
                                    (SkillConfigManager.getUseSetting(hero, skill, "hunger-increase", 0.0, false) * hero.getSkillLevel(skill)));
                            hunger = hunger > 0 ? hunger : 0;
                            long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN.node(), 500, false) -
                                    (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN_REDUCE.node(), 0.0, false) * hero.getSkillLevel(skill)));
                            cooldown = cooldown > 0 ? cooldown : 0;
                            
                            if(player.getFoodLevel()<=5) {
                                return;
                            }
                            
                            if(player.getFoodLevel()-hunger<5) {
                                player.setFoodLevel(5);
                            }
                            else {
                                player.setFoodLevel(player.getFoodLevel()-hunger);
                            }
                            
                            hero.setCooldown("CraneStanceEffect", cooldown + System.currentTimeMillis());
                            hero.getPlayer().getWorld().playEffect(hero.getPlayer().getLocation(), Effect.POTION_BREAK, 612);
                            
                            hero.addEffect(new com.herocraftonline.heroes.characters.effects.common.QuickenEffect(skill, "Crane's Swiftness", cooldown*5, 2, null, null));
                            
                            if (hero.getPlayer().getHealth() + health >= hero.getPlayer().getMaxHealth()) {
                                hero.getPlayer().setHealth(hero.getPlayer().getMaxHealth());
                            } else {
                                hero.getPlayer().setHealth(health + hero.getPlayer().getHealth());
                            }
                            //hero.syncHealth();
                        }
                    }
                }
            }
        }
    }
    
}