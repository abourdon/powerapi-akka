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
package fr.inria.powerapi.listener.disk.console
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.formula.disk.api.DiskFormulaValues

/**
 * Disk listener, displaying DiskFormulaValues to the console.
 *
 * @author abourdon
 */
class DiskListener extends Listener {
  def messagesToListen = Array(classOf[DiskFormulaValues])

  def process(diskFormulaValues: DiskFormulaValues) {
    println(diskFormulaValues)
  }

  def acquire = {
    case diskFormulaValues: DiskFormulaValues => process(diskFormulaValues)
  }
}