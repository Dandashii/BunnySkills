import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.skill.*;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class SkillInfernal extends ActiveSkill {
    
    
    public SkillInfernal(Heroes plugin) {
        super(plugin, "Infernal");
        asd=plugin;
        setDescription("Burn your target alive. This stacks 3 times to increase combustion damage. Your target will ignite on the 3rd stack.");
        setUsage("/skill Infernal");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Infernal" });
        
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
        node.set(SkillSetting.MAX_DISTANCE.node(), 60);
        node.set(SkillSetting.RADIUS.node(), 30);
        node.set(SkillSetting.DAMAGE.node(), 1);
        node.set(SkillSetting.MANA.node(),0);
        node.set(SkillSetting.COOLDOWN.node(),100);
        node.set("fullstackbonus-damage",1);
        return node;
    }

    @Override
    public void init() {
        super.init();
        
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        
        Location nxt=hero.getPlayer().getLocation();
        nxt = nxt.add(0,30,0);
        
        int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 30, false);
        
        Block wTargetBlock = hero.getPlayer().getTargetBlock(null, radius);//.getRelative(BlockFace.UP);
        if(wTargetBlock==null || wTargetBlock.getType()==Material.AIR) {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        wave(hero,nxt,0,wTargetBlock,radius);
        
        
        return SkillResult.NORMAL;
    }
    
    public void wave(Hero hero, Location nxt, int c, Block tblock, final int radius) {
        
        Block wTargetBlock = hero.getPlayer().getTargetBlock(null, radius);//.getRelative(BlockFace.UP);
        
        if(wTargetBlock==null || wTargetBlock.getType()==Material.AIR) {
            wTargetBlock=tblock;
        }
        else if((wTargetBlock.getY()-tblock.getY())>4) {
            wTargetBlock=tblock;
        }
        
        Vector thrv = wTargetBlock.getLocation().subtract(nxt).toVector().normalize();
        
        nxt=nxt.add(thrv);
        nxt.getWorld().playEffect(nxt,Effect.MOBSPAWNER_FLAMES,50);
        c++;
        
        if(wTargetBlock.getLocation().distanceSquared(nxt)<3) {
            System.out.println("EXPLODE");
            explode(hero, nxt);
            return;
        }
        
        int distance = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE, 50, false);
        if(c>distance) {
            return;
        }
        
        final Block fblock = wTargetBlock;
        final Location fnxt = nxt;
        final Hero fhero = hero;
        final int fc = c;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(asd, new Runnable(){                
            @Override public void run(){ wave(fhero,fnxt,fc,fblock,radius); }},(long)(c%2));
    }
    
    public void explode(Hero hero, Location loc) {
        int radius = 8;
        double x=loc.getX();
        double y=loc.getY();
        double z=loc.getZ();
        for(int i=0;i<40;i++) {
            int xr = (int)Math.round((Math.random()*radius*2)-radius);
            int yr = (int)Math.round((Math.random()*radius*2)-radius);
            int zr = (int)Math.round((Math.random()*radius*2)-radius);
            
            Location locx = new Location(loc.getWorld(),x+xr,y+yr,z+zr);
            
            if(Math.random()>=0.82) {
                // explosion
                loc.getWorld().createExplosion(locx, 0, false);
            }
            else {
                loc.getWorld().playEffect(locx, Effect.MOBSPAWNER_FLAMES,50);
            }
        }
        
        for(LivingEntity le:getNearbyLivingEntities(loc,5)) {
        int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 100, false);
            if(Skill.damageCheck(hero.getPlayer(), le)) {
                Skill.damageEntity(le, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC, true);
            }
        }
        
        LivingEntity gol = loc.add(0,1,0).getWorld().spawn(loc, IronGolem.class);
        gol.setMaxHealth(500);
        gol.setHealth(500);
        Monster m = plugin.getCharacterManager().getMonster(gol);
        m.setDamage(50);
        
        
        m.addEffect(new com.herocraftonline.heroes.characters.effects.common.SummonEffect(this, 10000, hero, ""));
        
        
    }
    
    public static List<LivingEntity> getNearbyLivingEntities(Location l, int radius) {
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16))/16;
        List<LivingEntity> radiusEntities=new ArrayList<>();
        int radiussq = radius*radius;
            for (int chX = 0 -chunkRadius; chX <= chunkRadius; chX ++) {
                for (int chZ = 0 -chunkRadius; chZ <= chunkRadius; chZ++) {
                    int x=(int) l.getX(),y=(int) l.getY(),z=(int) l.getZ();
                    for (Entity e : new Location(l.getWorld(),x+(chX*16),y,z+(chZ*16)).getChunk().getEntities()) {
                        if(e.getLocation().getWorld().equals(l.getWorld())) {
                            if (e.getLocation().distanceSquared(l) <= radiussq && e.getLocation().getBlock() != l.getBlock()) {
                                if(e instanceof LivingEntity) {
                                    radiusEntities.add((LivingEntity)e);
                                }
                            }
                        }
                    }
                }
            }
        return radiusEntities;
    }
    
    static Heroes asd;

}