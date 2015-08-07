import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class SkillPoisonnova extends ActiveSkill {
    public SkillPoisonnova(Heroes plugin) {
        super(plugin, "Poisonnova");
        setDescription("Spray out a wave of poison to damage your enemies");
        setUsage("/skill Poisonnova");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Poisonnova" });
        
        setTypes(SkillType.DAMAGING, SkillType.DEBUFF, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 30, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        
        String description = getDescription().replace("$1", distance + "");
        
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
        node.set(SkillSetting.MAX_DISTANCE.node(), 30);
        node.set(SkillSetting.DAMAGE.node(), 1);
        node.set(SkillSetting.DURATION.node(), 3000);
        node.set(SkillSetting.MANA.node(),5);
        node.set(SkillSetting.COOLDOWN.node(),100);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
        
    }
    
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        
        int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 1, false);
        int distance = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE, 1, false);
        long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 3000, false);
        
        Vector dir = hero.getPlayer().getLocation().getDirection();
        dir = dir.normalize();
        Location next = hero.getPlayer().getLocation();
        next.setY(next.getY()+1.5);
        double mul = 2;
        
        //run recursive method
        forward(next,hero,dir,mul,damage,distance,duration, 0);
        
        return SkillResult.NORMAL;
    }
    
    public void forward(Location next, final Hero hero, final Vector dir, final double mul, final int damage, final double distance, final long duration, final int c) {
                List<LivingEntity> targets = new ArrayList<>();
                next = next.add(dir.getX()*mul, dir.getY()*mul, dir.getZ()*mul);
                next.getWorld().playEffect(next, Effect.POTION_BREAK, 612);

                /*
                Firework fw = (Firework) next.getWorld().spawnEntity(next, EntityType.FIREWORK);
            
                FireworkMeta fwm = fw.getFireworkMeta();

                FireworkEffect effect = FireworkEffect.builder().withColor(Color.AQUA).with(FireworkEffect.Type.BURST).withFade(Color.BLACK).build();
                fwm.addEffects(effect);
                
                if((c%2)==0) {
                    FireworkEffect effect2 = FireworkEffect.builder().withColor(Color.WHITE).with(FireworkEffect.Type.BURST).withFade(Color.BLACK).build();
                    fwm.addEffects(effect2);
                }
                
                
                fwm.setPower(0);
                fw.setFireworkMeta(fwm);

                ((CraftWorld)next.getWorld()).getHandle().broadcastEntityEffect(
                        ((CraftFirework)fw).getHandle(),(byte)17);

                fw.remove();*/
                
                
                
                
                for(LivingEntity e:getNearbyLivingEntities(next, 1.9)) {
                    if(damageCheck(hero.getPlayer(),e)) {
                        addSpellTarget(e,hero);
                        targets.add(e);
                    }
                }

                for(LivingEntity e:targets) {
                    if(e instanceof Player) {
                        if(damageCheck(hero.getPlayer(), (Player)e)) { 
                            if(!plugin.getCharacterManager().getHero((Player)e).hasEffect("poisonwave")) {
                                damageEntity((Player)e, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC);
                                plugin.getCharacterManager().getHero((Player)e).addEffect(new com.herocraftonline.heroes.characters.effects.common.SlowEffect(this, "poisonwave", duration, 2, true, "", "", hero));
                                next.getWorld().playEffect(e.getLocation(), Effect.POTION_BREAK, 612);
                            }
                        }
                    }
                    if(e instanceof Creature) {
                        if(damageCheck(hero.getPlayer(), (Creature)e)) {
                            damageEntity((Creature)e, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC);
                        }
                        next.getWorld().playEffect(e.getLocation(), Effect.POTION_BREAK, 612);
                    }
                }
                
                if(next.distance(hero.getPlayer().getLocation().getBlock().getLocation())<distance) {
                    final Location nxt = next;
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){                
                        @Override
                        public void run(){ forward(nxt,hero,dir,mul,damage,distance,duration,c+1); }},(long)(2));
                }
    }
    
    public static List<LivingEntity> getNearbyLivingEntities(Location l, double radius){
        int chunkRadius = radius < 16 ? 1 : ((int)(radius - (radius % 16))/16);
        List<LivingEntity> radiusEntities=new ArrayList<>();
        double radiussq = radius*radius;
            for (int chX = 0 -chunkRadius; chX <= chunkRadius; chX ++){
                for (int chZ = 0 -chunkRadius; chZ <= chunkRadius; chZ++){
                    int x=(int) l.getX(),y=(int) l.getY(),z=(int) l.getZ();
                    for (Entity e : new Location(l.getWorld(),x+(chX*16),y,z+(chZ*16)).getChunk().getEntities()){
                        if (e.getLocation().distanceSquared(l) <= radiussq && e.getLocation().getBlock() != l.getBlock()) {
                            if(e instanceof LivingEntity) {
                                radiusEntities.add((LivingEntity)e);
                            }
                        }
                    }
                }
            }
        return radiusEntities;
    }

}