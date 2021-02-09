package ru.dbotthepony.mc.bcfe.mj;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjPassiveProvider;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.IMjReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyWrapperMJ implements IMjConnector, IMjPassiveProvider, IMjReadable, IMjReceiver {
	protected final TileEntity upvalue;
	protected IEnergyStorage container;
	EnumFacing face = EnumFacing.SOUTH;

	public EnergyWrapperMJ(TileEntity upvalue) {
		this.upvalue = upvalue;
	}

	EnergyWrapperMJ face(EnumFacing face) {
		this.face = face;
		return this;
	}

	private boolean updateCapability(IEnergyStorage storage) {
		this.container = storage;

		if (storage == null) {
			return false;
		}

		return true;
	}

	boolean isValid() {
		return this.upvalue.hasCapability(CapabilityEnergy.ENERGY, this.face)
			? this.updateCapability(this.upvalue.getCapability(CapabilityEnergy.ENERGY, this.face)) : false;
	}

	@Override
	public long getPowerRequested() {
		if (this.container == null) {
			return 0;
		}

		return EnergyProviderMJ.fromRF(this.container.receiveEnergy(Integer.MAX_VALUE, true));
	}

	@Override
	public long receivePower(long microJoules, boolean simulate) {
		if (this.container == null) {
			return 0;
		}

		int rf = EnergyProviderMJ.toRF(microJoules);

		if (rf == 0) {
			return 0;
		}

		int simulated = this.container.receiveEnergy(rf, true);

		if (simulated == 0) {
			return microJoules;
		}

		if (!simulate) {
			this.container.receiveEnergy(simulated, false);
		}

		return microJoules - EnergyProviderMJ.fromRF(simulated);
	}

	@Override
	public long getStored() {
		if (this.container == null) {
			return 0;
		}

		return EnergyProviderMJ.fromRF(this.container.getEnergyStored());
	}

	@Override
	public long getCapacity() {
		if (this.container == null) {
			return 0;
		}

		return EnergyProviderMJ.fromRF(this.container.getMaxEnergyStored());
	}

	@Override
	public long extractPower(long min, long max, boolean simulate) {
		if (this.container == null) {
			return 0;
		}

		if (!this.container.canExtract()) {
			return 0;
		}

		int rfMax = EnergyProviderMJ.toRF(max);
		int simulated = this.container.extractEnergy(rfMax, true);

		if (rfMax == simulated) {
			if (!simulate) {
				this.container.extractEnergy(rfMax, false);
			}

			return max;
		}

		int rfMin = EnergyProviderMJ.toRF(min);

		if (rfMin > simulated) {
			return 0;
		}

		if (!simulate) {
			this.container.extractEnergy(simulated, false);
		}

		return EnergyProviderMJ.fromRF(simulated);
	}

	@Override
	public boolean canConnect(IMjConnector other) {
		if (this.container == null) {
			return false;
		}

		return true;
	}
}
