import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.InvisibleEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SkillVoidshift extends ActiveSkill implements Listener {
    
    Skill voidshift;
    
    public SkillVoidshift(Heroes plugin) {
        super(plugin, "Voidshift");
        setDescription("You flash in and out of time for $1s. This also gives you 10% dodge chance for the duration");
        setUsage("/skill Voidshift");
        setArgumentRange(0, 0);
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        setIdentifiers(new String[]{"skill Voidshift"});
        setTypes(SkillType.DARK, SkillType.SUMMON, SkillType.SILENCABLE);
        voidshift=this;
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 0, false));
                
        String description = getDescription().replace("$1", (int)(duration/1000) + "");
        
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
        node.set(SkillSetting.DURATION.node(), 8000);
        node.set("backtrack-chance",10);
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        broadcastExecuteText(hero);
        long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 8000, false);
        hero.addEffect(new com.herocraftonline.heroes.characters.effects.ExpirableEffect(voidshift, "voidshift", duration));
        final Hero fHero = hero;
        
        for(int i=0;i<duration;i+=800) {
            final int fi=i;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    fHero.addEffect(new InvisibleEffect(voidshift,400,"",""));
                    
                    //fHero.addEffect(new SInvisibleEffect(voidshift,400,"",""));
                }
            }, ((fi/800)*20));
        }
        return SkillResult.NORMAL;
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEdby(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof Player)) {
            return;
        }
        if(event.isCancelled()) {
            return;
        }
        if(!plugin.getCharacterManager().getHero((Player)event.getEntity()).hasEffect("voidshift")) {
            return;
        }
        
        double chance = SkillConfigManager.getUseSetting(plugin.getCharacterManager().getHero((Player)event.getEntity()), this, "backtrack-chance", 10, false);
        
        if((Math.random()*100)<(chance)) {
            ((Player)event.getEntity()).sendMessage("§7You shifted through time to dodge an attack!");
            if(event.getDamager() instanceof Player) {
                ((Player)event.getDamager()).sendMessage("§7Your attack was lost in the shift of time!");
            }
            event.getEntity().getWorld().playEffect(event.getEntity().getLocation(), Effect.POTION_BREAK, 600);
            
            event.setDamage(0);
            event.setCancelled(true);
        }
    }
    
    
        /*
    public class SInvisibleEffect extends ExpirableEffect {

        private final String applyText;
        private final String expireText;

        public SInvisibleEffect(Skill skill, long duration, String applyText, String expireText) {
            super(skill, "Invisible", duration);
            this.types.add(EffectType.BENEFICIAL);
            this.types.add(EffectType.INVIS);
            this.applyText = applyText;
            this.expireText = expireText;
        }

        @Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            Player player = hero.getPlayer();
            // Tell all the logged in Clients to Destroy the Entity - Appears Invisible.
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (player.equals(onlinePlayer) || !player.getWorld().equals(onlinePlayer.getWorld()))
                    continue;

                if (player.getLocation().distanceSquared(onlinePlayer.getLocation()) > 16000 )
                    continue;

                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new Packet29DestroyEntity(((CraftPlayer) player).getEntityId()));
                //onlinePlayer.hidePlayer(player);
            }

            broadcast(player.getLocation(), applyText, player.getDisplayName());
        }

        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            Player player = hero.getPlayer();
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (onlinePlayer.equals(player) || !player.getWorld().equals(onlinePlayer.getWorld())) 
                    continue;
                if (player.getLocation().distanceSquared(onlinePlayer.getLocation()) > 16000)
                    continue;

                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new Packet20NamedEntitySpawn(((CraftPlayer) player).getHandle()));
                //onlinePlayer.showPlayer(player);
            }

            broadcast(player.getLocation(), expireText, player.getDisplayName());
        }
    }*/
    
}