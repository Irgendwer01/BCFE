package ru.dbotthepony.mc.bcfe.mj;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjPassiveProvider;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;
import ru.dbotthepony.mc.bcfe.BCFE;

class EnergyContainerMJ implements IEnergyStorage {
	protected final TileEntity upvalue;
	EnumFacing face = EnumFacing.SOUTH;

	private EnumFacing lastFace;
	protected IMjConnector connector;
	protected IMjReadable read;
	protected IMjReceiver receiver;
	protected IMjPassiveProvider passive;

	public EnergyContainerMJ(TileEntity upvalue) {
		this.upvalue = upvalue;
	}

	EnergyContainerMJ face(EnumFacing face) {
		this.face = face;
		return this;
	}

	boolean isValid() {
		return this.upvalue.hasCapability(MjAPI.CAP_RECEIVER, this.face) ||
			this.upvalue.hasCapability(MjAPI.CAP_CONNECTOR, this.face);
	}

	EnergyContainerMJ updateValues() {
		if (lastFace == face) {
			return this;
		}

		lastFace = face;
		connector = this.upvalue.getCapability(MjAPI.CAP_CONNECTOR, this.face);
		read = this.upvalue.getCapability(MjAPI.CAP_READABLE, this.face);
		receiver = this.upvalue.getCapability(MjAPI.CAP_RECEIVER, this.face);
		passive = this.upvalue.getCapability(MjAPI.CAP_PASSIVE_PROVIDER, this.face);

		return this;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if (!canReceive()) {
			return 0;
		}

		maxReceive = Math.min(calcMaxReceive(), maxReceive);

		if (maxReceive == 0) {
			return 0;
		}

		long value = EnergyProviderMJ.fromRF(maxReceive);
		long simulated = receiver.receivePower(value, true);

		if (simulated == 0L) {
			if (!simulate) {
				receiver.receivePower(value, false);
			}

			return maxReceive;
		}

		value -= simulated;

		long ratio = BCFE.conversionRatio();

		if (value % ratio != 0) {
			value -= value % ratio;
		}

		simulated = receiver.receivePower(value, true);

		if (simulated % ratio != 0) {
			return 0;
		}

		if (!simulate) {
			return EnergyProviderMJ.toRF(value - receiver.receivePower(value, false));
		}

		return EnergyProviderMJ.toRF(value - simulated);
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		if (!canExtract()) {
			return 0;
		}

		long value = EnergyProviderMJ.fromRF(maxExtract);
		long simulated = passive.extractPower(BCFE.conversionRatio(), value, true);

		simulated -= simulated % BCFE.conversionRatio();

		if (simulated == 0L) {
			return 0;
		}

		if (!simulate) {
			return EnergyProviderMJ.toRF(passive.extractPower(BCFE.conversionRatio(), simulated, true));
		}

		return EnergyProviderMJ.toRF(simulated);
	}

	int calcMaxReceive() {
		if (read == null) {
			return Integer.MAX_VALUE;
		}

		return getMaxEnergyStored() - getEnergyStored();
	}

	@Override
	public int getEnergyStored() {
		return read != null ? EnergyProviderMJ.toRF(read.getStored()) : 0;
	}

	@Override
	public int getMaxEnergyStored() {
		return read != null ? EnergyProviderMJ.toRF(read.getCapacity()) : 0;
	}

	@Override
	public boolean canExtract() {
		return passive != null;
	}

	@Override
	public boolean canReceive() {
		return receiver != null && receiver.canReceive();
	}
}
