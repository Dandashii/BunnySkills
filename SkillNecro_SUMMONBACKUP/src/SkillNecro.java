import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class SkillNecro extends ActiveSkill implements Listener {
    public SkillNecro(Heroes plugin) {
        super(plugin, "Necro");
        setDescription("$1% chance to spawn 1 Skeleton, $2% for 2, and $3% for 3.");
        setUsage("/skill Necro");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill Necro"});
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.DARK, SkillType.SUMMON, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        int chance2x = (int) (SkillConfigManager.getUseSetting(hero, this, "chance-2x", 0.2, false) * 100 +
                SkillConfigManager.getUseSetting(hero, this, "added-chance-2x-per-level", 0.0, false) * hero.getSkillLevel(this));
        int chance3x = (int) (SkillConfigManager.getUseSetting(hero, this, "chance-3x", 0.1, false) * 100 +
                SkillConfigManager.getUseSetting(hero, this, "added-chance-3x-per-level", 0.0, false) * hero.getSkillLevel(this));
        String description = getDescription().replace("$1", (100 - (chance2x + chance3x)) + "").replace("$2", chance2x + "").replace("$3", chance3x + "");
        
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
        node.set("chance-2x", 0.2);
        node.set("chance-3x", 0.1);
        node.set("added-chance-2x-per-level", 0.0);
        node.set("added-chance-3x-per-level", 0.0);
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        broadcastExecuteText(hero);
        double chance2x = SkillConfigManager.getUseSetting(hero, this, "chance-2x", 0.2, false);
        double chance3x = SkillConfigManager.getUseSetting(hero, this, "chance-3x", 0.1, false);
        Block wTargetBlock = player.getTargetBlock(null, 20).getRelative(
                        BlockFace.UP);
        double rand = Math.random();
        LivingEntity le = player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.SKELETON);
        le.getWorld().playEffect(le.getLocation(), Effect.SMOKE, 3);
        lel.put(le,player.getName());
        
        LivingEntity le2 = null;
        LivingEntity le1 = null;
        
        int count = 1;
        if (rand > (1 - chance2x - chance3x)) {
            le1 = player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.SKELETON);
            le1.getWorld().playEffect(le1.getLocation(), Effect.SMOKE, 3);
            count++;
        lel.put(le1,player.getName());
        }
        
        if (rand > (1 - chance3x)) {
            le2 = player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.SKELETON);
            le2.getWorld().playEffect(le2.getLocation(), Effect.SMOKE, 3);
            count++;
        lel.put(le1,player.getName());
        }
        
        //hero.addEffect(new InvulnerabilityEffect(this,100));
        //hero.addEffect(new InvisibleEffect(this, 100,"",""));
        
        
        final LivingEntity fle = le;
        final LivingEntity fle1 = le1;
        final LivingEntity fle2 = le2;
        
        hero.addEffect(new ExpirableEffect(this,plugin,"Untargetable",15000));
          
        if (hero.getPlayer().getFoodLevel()>15) {
            hero.getPlayer().setFoodLevel(15);
        }
              
        for(Entity e: hero.getPlayer().getNearbyEntities(40,40,40)) {
            if(e instanceof Player) {
                if(!damageCheck(hero.getPlayer(),(LivingEntity)e)) {
                    plugin.getCharacterManager().getHero((Player)e).addEffect(new ExpirableEffect(this,plugin,"Untargetable",15000));
                    
                }
            }
        }
        
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if(fle!=null) {
                    fle.remove();
                    lel.remove(fle);
                }
                if(fle1!=null) {
                    fle1.remove();
                    lel.remove(fle1);
                }
                if(fle2!=null) {
                    fle2.remove();
                    lel.remove(fle2);
                }
                }
            }, (15*20));
        
        return SkillResult.NORMAL;
    }
    
    
    static Map<LivingEntity,String> lel = new HashMap<>();
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEDeath(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity le = (LivingEntity)event.getEntity();
        if(!lel.containsKey(le)) {
            return;
        }
        if(event.getDamage()>=le.getHealth()) {
            event.setCancelled(true);
            event.setDamage(0);
            lel.remove(le);
            le.remove();
        }
    }
    
    	@EventHandler (priority = EventPriority.HIGHEST)
	public void onTarget(EntityTargetEvent event) {
            if(event.getEntity() instanceof LivingEntity) {
                if(!lel.containsKey((LivingEntity)event.getEntity())) {
                    return;
                }
            }
            if (!event.isCancelled()) {
                if (event.getTarget() instanceof Player) {
                    Player player = (Player) event.getTarget();
                    //if (player.hasPermission("targetless")) {
                    if (plugin.getCharacterManager().getHero(player).hasEffect("Untargetable" ) && event.getEntity() instanceof LivingEntity) {
                        if(((LivingEntity)event.getEntity()).getType()==EntityType.ZOMBIE || ((LivingEntity)event.getEntity()).getType()==EntityType.SKELETON) {
                            //if (event.getReason() == TargetReason.CLOSEST_PLAYER || event.getReason() == TargetReason.RANDOM_TARGET) {
                            event.setCancelled(true);
                            //}
                        }
                    }
                }
            }
	}

}