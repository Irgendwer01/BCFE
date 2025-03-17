// Copyright (C) 2018-2025 DBot, Irgendwer01

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
// of the Software, and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies
// or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
// INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
// PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
// FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.

package ru.dbotthepony.mc.bcfe.mj;

import buildcraft.api.mj.MjAPI;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import org.jetbrains.annotations.NotNull;
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
	public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
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
	public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
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
