import com.herocraftonline.heroes.characters.Hero;
import net.minecraft.server.*;
// CraftBukkit end

public class ShadowBolt extends EntityWitherSkull {

    public Hero hero;
    public int damage;
    boolean givesouls = true;
    
    public ShadowBolt(World world) {
        super(world);
    }

    public ShadowBolt(World world, EntityLiving entityliving, double d0, double d1, double d2) {
        super(world, entityliving, d0, d1, d2);
    }

    @Override
    protected void a(MovingObjectPosition movingobjectposition) {
        if (!this.world.isStatic) {
            /*if (movingobjectposition.entity != null) {
                if (this.shooter != null) {
                    //if (movingobjectposition.entity.damageEntity(DamageSource.mobAttack(this.shooter), 8) && !movingobjectposition.entity.isAlive()) {
                        //this.shooter.heal(5, EntityRegainHealthEvent.RegainReason.WITHER); // CraftBukkit
                    //}
                    
                    
                    
                    
                    
                    
                } else {
                    movingobjectposition.entity.damageEntity(DamageSource.MAGIC, 5);
                }

                if (movingobjectposition.entity instanceof EntityLiving) {
                    byte b0 = 0;

                    if (this.world.difficulty > 1) {
                        if (this.world.difficulty == 2) {
                            b0 = 10;
                        } else if (this.world.difficulty == 3) {
                            b0 = 40;
                        }
                    }

                    if (b0 > 0) {
                        //((EntityLiving) movingobjectposition.entity).addEffect(new MobEffect(MobEffectList.WITHER.id, 20 * b0, 1));
                    }
                }
            }*/

            // CraftBukkit start
            /*ExplosionPrimeEvent event = new ExplosionPrimeEvent(this.getBukkitEntity(), 1.0F, false);
            this.world.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                this.world.createExplosion(this, this.locX, this.locY, this.locZ, event.getRadius(), event.getFire(), this.world.getGameRules().getBoolean("mobGriefing"));
            }*/
            // CraftBukkit end

            this.die();
        }
    }
}