package ru.dbotthepony.mc.bcfe.mj;

import buildcraft.api.mj.MjAPI;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import ru.dbotthepony.mc.bcfe.BCFE;

public class EnergyProviderMJ implements ICapabilityProvider {
	private final TileEntity upvalue;
	private EnergyContainerMJ container;
	private EnergyWrapperMJ containerMJ;
	private boolean ignore = false;

	public EnergyProviderMJ(TileEntity upvalue) {
		this.upvalue = upvalue;
	}

	private static int antiOverflow(long value) {
		if (value > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}

		return (int) value;
	}

	public static int toRF(long microJoules) {
		long ratio = BCFE.conversionRatio();

		if (microJoules < ratio) {
			return 0;
		}

		microJoules -= microJoules % ratio;
		microJoules /= ratio;

		return antiOverflow(microJoules);
	}

	public static long fromRF(int rf) {
		return rf * BCFE.conversionRatio();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (ignore) {
			return false;
		}

		if (capability == CapabilityEnergy.ENERGY) {
			if (this.container == null) {
				this.container = new EnergyContainerMJ(this.upvalue);
			}

			ignore = true;
			boolean result = container.face(facing).isValid();
			ignore = false;

			return result;
		}

		if (capability == MjAPI.CAP_PASSIVE_PROVIDER || capability == MjAPI.CAP_RECEIVER || capability == MjAPI.CAP_READABLE || capability == MjAPI.CAP_CONNECTOR) {
			if (this.containerMJ == null) {
				this.containerMJ = new EnergyWrapperMJ(upvalue);
			}

			ignore = true;
			boolean result = this.containerMJ.face(facing).isValid();
			ignore = false;

			return result;
		}

		return false;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (ignore) {
			return null;
		}

		if (capability == CapabilityEnergy.ENERGY) {
			if (this.container == null) {
				this.container = new EnergyContainerMJ(this.upvalue);
			}

			ignore = true;
			EnergyContainerMJ container = this.container.face(facing).isValid() ? this.container.updateValues() : null;
			ignore = false;

			return (T) container;
		}

		if (capability == MjAPI.CAP_PASSIVE_PROVIDER || capability == MjAPI.CAP_RECEIVER || capability == MjAPI.CAP_READABLE || capability == MjAPI.CAP_CONNECTOR) {
			if (this.containerMJ == null) {
				this.containerMJ = new EnergyWrapperMJ(this.upvalue);
			}

			ignore = true;
			EnergyWrapperMJ container = this.containerMJ.face(facing).isValid() ? this.containerMJ : null;
			ignore = false;

			return (T) container;
		}

		return null;
	}
}
