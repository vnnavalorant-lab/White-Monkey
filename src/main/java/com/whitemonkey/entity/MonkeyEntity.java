package com.whitemonkey.entity;

import java.lang.reflect.Constructor;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

	/** Dono (domado ao ser alimentado ou ao subir no ombro). Salvo no mundo. */
	private UUID ownerUuid;
	private int eatTicks;
	/** começa true para "limpar" estados de ombro que ficaram salvos (gravidade/invulnerável) no 1º tick */
	private boolean perchedGodMode = true;

	public MonkeyEntity(EntityType<? extends MonkeyEntity> type, Level level) {
		super(type, level);
		this.setDropChance(EquipmentSlot.HEAD, 1.0F); // o capacete sempre cai quando o macaco morre
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
		this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.2, 5.0F, 2.0F));
		this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.9));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
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
		ItemStack one = food.copyWithCount(1);
		this.entityData.set(EAT_STACK, one);
		this.entityData.set(EATING, true);
		// O macaco "usa" o item na mão, igual ao jogador comendo: o próprio jogo gera as partículas
		// de pedaços do item saindo da boca e o som de mastigar (cliente e servidor).
		this.setItemInHand(InteractionHand.MAIN_HAND, one.copy());
		this.startUsingItem(InteractionHand.MAIN_HAND);
		this.eatTicks = 0;
		this.getNavigation().stop();
	}

	private void finishEating() {
		FoodProperties food = this.getEatStack().get(DataComponents.FOOD);
		if (food != null) {
			this.heal(Math.max(1, food.nutrition()));
		}
		this.stopUsingItem();
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		this.entityData.set(EATING, false);
		this.entityData.set(EAT_STACK, ItemStack.EMPTY);
		this.eatTicks = 0;
	}

	/** Partículas de pedaços do item saindo da "mão" (bone item_eat) do macaco. */
	private void spawnEatParticles() {
		if (!(this.level() instanceof ServerLevel server) || this.getEatStack().isEmpty()) return;
		double yaw = Math.toRadians(this.yBodyRot);
		double x = this.getX() - Math.sin(yaw) * 0.38;
		double y = this.getY() + 0.5;
		double z = this.getZ() + Math.cos(yaw) * 0.38;
		ParticleOptions options = itemParticle(this.getEatStack());
		if (options != null) {
			server.sendParticles(options, x, y, z, 4, 0.06, 0.06, 0.06, 0.03);
		}
	}

	/**
	 * Cria a partícula "item" (pedaços do item). Desde o 26.1 algumas APIs usam ItemStackTemplate no lugar de
	 * ItemStack; a reflexão aceita qualquer um dos dois construtores e evita erro de compilação entre versões.
	 */
	private static ParticleOptions itemParticle(ItemStack stack) {
		try {
			for (Constructor<?> c : ItemParticleOption.class.getConstructors()) {
				Class<?>[] types = c.getParameterTypes();
				if (types.length == 2 && ParticleType.class.isAssignableFrom(types[0])) {
					Object arg = stack;
					if (!types[1].isInstance(stack)) {
						arg = types[1].getMethod("fromNonEmptyStack", ItemStack.class).invoke(null, stack);
					}
					return (ParticleOptions) c.newInstance(ParticleTypes.ITEM, arg);
				}
			}
		} catch (ReflectiveOperationException | RuntimeException ignored) {
			// sem partículas, mas sem quebrar o jogo
		}
		return null;
	}

	/** SoundEvents.GENERIC_EAT pode ser SoundEvent ou Holder dependendo da versão; isto aceita os dois. */
	private static SoundEvent unwrap(Object sound) {
		return sound instanceof Holder<?> holder ? (SoundEvent) holder.value() : (SoundEvent) sound;
	}

	// ------------------------------------------------------------------ domesticação
	public boolean isTamed() {
		return this.ownerUuid != null;
	}

	public UUID getOwnerUuid() {
		return this.ownerUuid;
	}

	public boolean isOwnedBy(Player player) {
		return this.ownerUuid != null && this.ownerUuid.equals(player.getUUID());
	}

	/** Dono atual se estiver no mesmo mundo/online, senão null. */
	public Player getOwner() {
		return this.ownerUuid == null ? null : this.level().getPlayerByUUID(this.ownerUuid);
	}

	private void tame(Player player) {
		if (this.isTamed()) return;
		this.ownerUuid = player.getUUID();
		player.sendSystemMessage(Component.translatable("message.white_monkey.tamed"));
		if (this.level() instanceof ServerLevel server) {
			server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.9, this.getZ(),
					7, 0.4, 0.4, 0.4, 0.02);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (this.ownerUuid != null) {
			output.putString("OwnerUUID", this.ownerUuid.toString());
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.ownerUuid = null;
		String id = input.getString("OwnerUUID").orElse("");
		if (!id.isEmpty()) {
			try {
				this.ownerUuid = UUID.fromString(id);
			} catch (IllegalArgumentException ignored) {
				// UUID inválido: monkey volta a ser selvagem
			}
		}
	}

	// ------------------------------------------------------------------ capacete
	private static boolean isHelmet(ItemStack stack) {
		var equippable = stack.get(DataComponents.EQUIPPABLE);
		return equippable != null && equippable.slot() == EquipmentSlot.HEAD;
	}

	public ItemStack getHelmet() {
		return this.getItemBySlot(EquipmentSlot.HEAD);
	}

	private void giveBack(Player player, ItemStack stack) {
		if (!stack.isEmpty() && !player.getInventory().add(stack)) {
			ItemEntity drop = new ItemEntity(player.level(), player.getX(), player.getY() + 0.5, player.getZ(), stack);
			drop.setDefaultPickUpDelay();
			player.level().addFreshEntity(drop);
		}
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
		this.setNoGravity(true);
		this.noPhysics = true; // sem colisão: não empurra o jogador nem é empurrado
		return true;
	}

	public void dismountFromShoulder() {
		if (!this.isOnShoulder()) return;
		this.entityData.set(OWNER_ID, -1);
		this.releasePerchState();
	}

	/** Tira do ombro e coloca o macaco em um ponto (ex.: no chão). */
	public void dismountAt(double x, double y, double z) {
		this.dismountFromShoulder();
		this.setPos(x, y, z);
		this.setDeltaMovement(0, 0, 0);
		this.fallDistance = 0;
	}

	private void releasePerchState() {
		if (this.perchedGodMode) {
			this.perchedGodMode = false;
			this.setNoGravity(false);
		}
		this.noPhysics = false;
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
		this.noPhysics = true;
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
		// Macaco domado só obedece ao dono
		if (this.isTamed() && !this.isOwnedBy(player)) {
			if (!this.level().isClientSide()) {
				player.sendSystemMessage(Component.translatable("message.white_monkey.not_yours"));
			}
			return InteractionResult.SUCCESS;
		}

		// Shift + botão direito -> equipar no ombro
		if (player.isShiftKeyDown()) {
			if (!this.level().isClientSide()) {
				this.tame(player);
				if (!this.mountOnShoulder(player)) {
					player.sendSystemMessage(Component.translatable("message.white_monkey.shoulder_full"));
				}
			}
			return InteractionResult.SUCCESS;
		}

		// Botão direito com comida -> o macaco come (anima "eating" com o item no bone item_eat)
		ItemStack stack = player.getItemInHand(hand);
		if (!this.isEating() && !this.isOnShoulder() && stack.has(DataComponents.FOOD)) {
			if (!this.level().isClientSide()) {
				this.tame(player); // alimentar doma o macaco
				this.startEating(stack);
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}

		// Botão direito com um capacete -> o macaco veste (troca se já tiver um)
		if (isHelmet(stack)) {
			if (!this.level().isClientSide()) {
				ItemStack old = this.getHelmet().copy();
				this.setItemSlot(EquipmentSlot.HEAD, stack.copyWithCount(1));
				stack.consume(1, player);
				this.giveBack(player, old);
			}
			return InteractionResult.SUCCESS;
		}

		// Botão direito com a mão vazia -> tira o capacete
		if (stack.isEmpty() && !this.getHelmet().isEmpty()) {
			if (!this.level().isClientSide()) {
				this.giveBack(player, this.getHelmet().copy());
				this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
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
				} else {
					if (!this.isUsingItem() && this.eatTicks % 8 == 4) {
						this.playSound(unwrap(SoundEvents.GENERIC_EAT), 0.6F, 0.9F + this.random.nextFloat() * 0.3F);
					}
					// o item só está visível na animação entre ~0,5 s e ~1,6 s
					if (!this.isUsingItem() && this.eatTicks % 3 == 0 && this.eatTicks >= 12 && this.eatTicks <= 34) {
						this.spawnEatParticles();
					}
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

	/** Segue o dono (se domado), a menos que esteja comendo ou no ombro. */
	private static class FollowOwnerGoal extends Goal {
		private final MonkeyEntity monkey;
		private final double speed;
		private final float startDistance;
		private final float stopDistance;
		private Player owner;
		private int recalcTicks;

		FollowOwnerGoal(MonkeyEntity monkey, double speed, float startDistance, float stopDistance) {
			this.monkey = monkey;
			this.speed = speed;
			this.startDistance = startDistance;
			this.stopDistance = stopDistance;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (!this.monkey.isTamed() || this.monkey.isEating() || this.monkey.isOnShoulder()) return false;
			Player player = this.monkey.getOwner();
			if (player == null || player.isSpectator() || !player.isAlive()) return false;
			if (this.monkey.distanceToSqr(player) < (double) (this.startDistance * this.startDistance)) return false;
			this.owner = player;
			return true;
		}

		@Override
		public boolean canContinueToUse() {
			if (this.owner == null || !this.owner.isAlive() || this.owner.isSpectator()) return false;
			if (this.monkey.isEating() || this.monkey.isOnShoulder()) return false;
			return this.monkey.distanceToSqr(this.owner) > (double) (this.stopDistance * this.stopDistance);
		}

		@Override
		public void start() {
			this.recalcTicks = 0;
		}

		@Override
		public void stop() {
			this.owner = null;
			this.monkey.getNavigation().stop();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			this.monkey.getLookControl().setLookAt(this.owner, 10.0F, (float) this.monkey.getMaxHeadXRot());
			if (--this.recalcTicks > 0) return;
			this.recalcTicks = 10;
			// muito longe (ex.: o dono correu/teleportou): aparece ao lado dele
			if (this.monkey.distanceToSqr(this.owner) >= 24.0 * 24.0) {
				this.monkey.setPos(this.owner.getX() + 0.8, this.owner.getY(), this.owner.getZ() + 0.8);
				this.monkey.getNavigation().stop();
				return;
			}
			this.monkey.getNavigation().moveTo(this.owner, this.speed);
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
