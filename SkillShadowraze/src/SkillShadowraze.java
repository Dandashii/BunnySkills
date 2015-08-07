import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftFirework;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public class SkillShadowraze extends ActiveSkill implements Listener {
    public SkillShadowraze(Heroes plugin) {
        super(plugin, "Shadowraze");
        setDescription("You blast shadows out of your target location, dealing $1 damage and costing $2 soul shards.");
        setUsage("/skill Shadowraze");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill Shadowraze"});
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.DARK, SkillType.SUMMON, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        int damage = (int) SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 40, false);
        int soulcost = (int) SkillConfigManager.getUseSetting(hero, this, "soul-cost", 2, false);
        
        String description = getDescription().replace("$1",damage+"").replace("$2",soulcost+"");
        
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
        node.set("soul-cost", 2);
        node.set("radius", 4);
        node.set(SkillSetting.DAMAGE.node(), 40);
        node.set(SkillSetting.MAX_DISTANCE.node(), 30);
        
        return node;
    }
    
    public int getSouls(Hero hero) {
        
        int maxsouls = (SkillConfigManager.getUseSetting(hero, plugin.getSkillManager().getSkill("SoulMastery"), "max-souls", 20, false));
                maxsouls = maxsouls > 0 ? maxsouls : 0;
                
        if(hero.hasEffect("SoulShards")) {
            int souls = ((SoulShardEffect)hero.getEffect("SoulShards")).getSouls();
            return souls;
        }
        else {
            hero.addEffect(new SoulShardEffect(this,plugin,"SoulShards",60000,maxsouls));
        }
        
        return 1;
    }
    
    public void messageSouls(Player pl, int soulcount, int max) {
        int souls = soulcount;
        
        String stext = "§5Soul Shards: §0[ ";
        
        for(int i=0;i<souls;i++) {
            stext+="§5• ";
        }
        
        for(int i=0;i<(max-souls);i++) {
            stext+="§0• ";
        }
        
        stext += "§0] §5"+soulcount;
        
        pl.sendMessage(stext);
    }
    
    public void raze(Hero hero, int damage, Block b, int radius) {
        for(LivingEntity le:getNearbyLivingEntities(b.getLocation(),radius)) {
            if(Skill.damageCheck(hero.getPlayer(), le)) {
                addSpellTarget(le, hero);
                Skill.damageEntity(le, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC, true);
            }
        }
        
        final Location loc = b.getLocation();
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override public void run() {
                fireWk(loc);
                fireWkball(loc);
            }
        },1);
        
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override public void run() {
                fireWkball(loc.clone().add(0,2,0));
            }
        },2);
        
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override public void run() {
                fireWkball(loc.clone().add(0,3,0));
            }
        },3);
        
    }
    
    
    public void fireWk(Location loc) {
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect2 = FireworkEffect.builder().withColor(Color.BLACK).with(FireworkEffect.Type.BURST).withFade(Color.BLACK).build();
        fwm.addEffects(effect2);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
        ((CraftWorld)loc.getWorld()).getHandle().broadcastEntityEffect(
                ((CraftFirework)fw).getHandle(),(byte)17);
        fw.remove();
    }
    public void fireWkball(Location loc) {
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect2 = FireworkEffect.builder().withColor(Color.BLACK).with(FireworkEffect.Type.BALL).withFade(Color.BLACK).build();
        fwm.addEffects(effect2);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
        ((CraftWorld)loc.getWorld()).getHandle().broadcastEntityEffect(
                ((CraftFirework)fw).getHandle(),(byte)17);
        fw.remove();
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
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        int range = (int) SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE, 30, false);
        int soulcost = (int) SkillConfigManager.getUseSetting(hero, this, "soul-cost", 2, false);
        
        if(!hero.hasEffect("SoulShards")) {
            hero.getPlayer().sendMessage("§7You do not have enough soul shards!");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        if(((SoulShardEffect)hero.getEffect("SoulShards")).getSouls()>=soulcost) {
            ((SoulShardEffect)hero.getEffect("SoulShards")).addSouls(0-soulcost,hero);
        }
        else {
            hero.getPlayer().sendMessage("§7You do not have enough soul shards!");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        messageSouls(hero.getPlayer() , ((SoulShardEffect)hero.getEffect("SoulShards")).getSouls() , ((SoulShardEffect)hero.getEffect("SoulShards")).maxsouls);
        
        int radius = (int) SkillConfigManager.getUseSetting(hero, this, "radius", 4, false);
        
        Block wTargetBlock = hero.getPlayer().getTargetBlock(null, range).getRelative(BlockFace.UP);
        
        int damage = (int) SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 40, false);
        damage += (int) Math.round(hero.getLevel() * SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, 0, false));
        
        raze(hero,damage,wTargetBlock,radius);
        
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
}