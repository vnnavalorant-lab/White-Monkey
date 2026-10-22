package com.whitemonkey.entity;

import java.util.EnumSet;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MonkeyEntity extends PathfinderMob {
	/** Duração da animação "eating" do Blockbench (2 s = 40 ticks). */
	public static final int EAT_DURATION = 40;

	private static final EntityDataAccessor<Boolean> EATING =
			SynchedEntityData.defineId(MonkeyEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<ItemStack> EAT_STACK =
			SynchedEntityData.defineId(MonkeyEntity.class, EntityDataSerializers.ITEM_STACK);

	/** ID (na entidade) do jogador em cujo ombro o macaco está; -1 = livre. */
	private static final EntityDataAccessor<Integer> OWNER_ID =
			SynchedEntityData.defineId(MonkeyEntity.class, EntityDataSerializers.INT);

	private int eatTicks;
	private boolean perchedGodMode;

	public MonkeyEntity(EntityType<? extends MonkeyEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 12)
				.add(Attributes.MOVEMENT_SPEED, 0.28)
				.add(Attributes.TEMPT_RANGE, 10);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new EatingFreezeGoal(this));
		this.goalSelector.addGoal(2, new PanicGoal(this, 1.4));
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, stack -> stack.has(DataComponents.FOOD), false));
		this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.9));
		this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(EATING, false);
		builder.define(EAT_STACK, ItemStack.EMPTY);
		builder.define(OWNER_ID, -1);
	}

	// ------------------------------------------------------------------ comer
	public boolean isEating() {
		return this.entityData.get(EATING);
	}

	/** Item exibido no bone "item_eat" durante a animação. */
	public ItemStack getEatStack() {
		return this.entityData.get(EAT_STACK);
	}

	public int getEatTicks() {
		return this.eatTicks;
	}

	private void startEating(ItemStack food) {
		this.entityData.set(EAT_STACK, food.copyWithCount(1));
		this.entityData.set(EATING, true);
		this.eatTicks = 0;
		this.getNavigation().stop();
	}

	private void finishEating() {
		FoodProperties food = this.getEatStack().get(DataComponents.FOOD);
		if (food != null) {
			this.heal(Math.max(1, food.nutrition()));
		}
		this.entityData.set(EATING, false);
		this.entityData.set(EAT_STACK, ItemStack.EMPTY);
		this.eatTicks = 0;
	}

	/** SoundEvents.GENERIC_EAT pode ser SoundEvent ou Holder dependendo da versão; isto aceita os dois. */
	private static SoundEvent unwrap(Object sound) {
		return sound instanceof Holder<?> holder ? (SoundEvent) holder.value() : (SoundEvent) sound;
	}

	// ------------------------------------------------------------------ ombro
	// O macaco NÃO é passageiro do jogador (o Minecraft não deixa montar em jogadores);
	// a cada tick ele é posicionado no ombro direito do dono, no servidor e no cliente.

	public boolean isOnShoulder() {
		return this.entityData.get(OWNER_ID) != -1;
	}

	private Player getShoulderOwner() {
		int id = this.entityData.get(OWNER_ID);
		if (id == -1) return null;
		return this.level().getEntity(id) instanceof Player player ? player : null;
	}

	public static MonkeyEntity getShoulderMonkey(Player player) {
		for (MonkeyEntity monkey : player.level().getEntitiesOfClass(
				MonkeyEntity.class, player.getBoundingBox().inflate(6.0),
				m -> m.entityData.get(OWNER_ID) == player.getId())) {
			return monkey;
		}
		return null;
	}

	public boolean mountOnShoulder(Player player) {
		if (getShoulderMonkey(player) != null) {
			return false;
		}
		this.getNavigation().stop();
		this.entityData.set(OWNER_ID, player.getId());
		this.perchedGodMode = true;
		this.setInvulnerable(true); // evita sufocar em paredes quando o jogador passa por vãos
		this.setNoGravity(true);
		return true;
	}

	public void dismountFromShoulder() {
		if (!this.isOnShoulder()) return;
		this.entityData.set(OWNER_ID, -1);
		this.releasePerchState();
	}

	private void releasePerchState() {
		if (this.perchedGodMode) {
			this.perchedGodMode = false;
			this.setInvulnerable(false);
			this.setNoGravity(false);
		}
	}

	/** Coloca o macaco no ombro direito do jogador e copia a direção do corpo/cabeça dele. */
	private void followOwner(Player owner) {
		float yaw = owner.yBodyRot;
		double rad = Math.toRadians(yaw);
		double rightX = -Math.cos(rad);
		double rightZ = -Math.sin(rad);
		double x = owner.getX() + rightX * 0.34;
		double y = owner.getY() + owner.getBbHeight() * 0.76;
		double z = owner.getZ() + rightZ * 0.34;
		this.setPos(x, y, z);
		this.setDeltaMovement(0, 0, 0);
		this.setYRot(yaw);
		this.setYBodyRot(yaw);
		this.setYHeadRot(owner.yHeadRot);
		this.setXRot(owner.getXRot());
		this.fallDistance = 0;
	}

	@Override
	public boolean isPickable() {
		return !this.isOnShoulder() && super.isPickable();
	}

	@Override
	public boolean isPushable() {
		return !this.isOnShoulder() && super.isPushable();
	}

	// ------------------------------------------------------------------ interação
	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		// Shift + botão direito -> equipar no ombro
		if (player.isShiftKeyDown()) {
			if (!this.level().isClientSide()) {
				if (!this.mountOnShoulder(player)) {
					player.displayClientMessage(Component.translatable("message.white_monkey.shoulder_full"), true);
				}
			}
			return InteractionResult.SUCCESS;
		}

		// Botão direito com comida -> o macaco come (anima "eating" com o item no bone item_eat)
		ItemStack stack = player.getItemInHand(hand);
		if (!this.isEating() && !this.isOnShoulder() && stack.has(DataComponents.FOOD)) {
			if (!this.level().isClientSide()) {
				this.startEating(stack);
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		return super.mobInteract(player, hand);
	}

	@Override
	public void tick() {
		super.tick();

		if (this.isEating()) {
			this.eatTicks++;
			if (!this.level().isClientSide()) {
				if (this.isOnShoulder() || this.getEatStack().isEmpty()) {
					this.finishEating();
				} else if (this.eatTicks % 8 == 4) {
					this.playSound(unwrap(SoundEvents.GENERIC_EAT), 0.6F, 0.9F + this.random.nextFloat() * 0.3F);
				}
				if (this.eatTicks >= EAT_DURATION) {
					this.finishEating();
				}
			}
		} else {
			this.eatTicks = 0;
		}

		if (this.isOnShoulder()) {
			Player owner = this.getShoulderOwner();
			if (owner != null && owner.isAlive() && !owner.isSpectator()) {
				this.getNavigation().stop();
				this.followOwner(owner);
			} else if (!this.level().isClientSide()) {
				this.dismountFromShoulder(); // dono saiu, morreu ou mudou de dimensão
			}
		} else if (!this.level().isClientSide()) {
			this.releasePerchState();
		}
	}

	/** Trava o macaco no lugar enquanto ele come. */
	private static class EatingFreezeGoal extends Goal {
		private final MonkeyEntity monkey;

		EatingFreezeGoal(MonkeyEntity monkey) {
			this.monkey = monkey;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return this.monkey.isEating();
		}

		@Override
		public void start() {
			this.monkey.getNavigation().stop();
		}
	}
}
