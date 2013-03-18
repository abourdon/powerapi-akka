/**
 * Copyright (C) 2012 Inria, University Lille 1.
 *
 * This file is part of PowerAPI.
 *
 * PowerAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * PowerAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with PowerAPI. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: powerapi-user-list@googlegroups.com.
 */
package fr.inria.powerapi.sensor.powerspy;

public final class PowerSpyEvent {

	private final Double currentRMS;
	private final Float uScale;
	private final Float iScale;

	public PowerSpyEvent(Double currentRMS, Float uScale, Float iScale) {
		this.currentRMS = currentRMS;
		this.uScale = uScale;
		this.iScale = iScale;
	}

	public Double getCurrentRMS() {
		return currentRMS;
	}

	public Float getUScale() {
		return uScale;
	}

	public Float getIScale() {
		return iScale;
	}

}
