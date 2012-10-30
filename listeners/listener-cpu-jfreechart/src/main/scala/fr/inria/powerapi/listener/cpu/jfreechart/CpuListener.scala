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
package fr.inria.powerapi.listener.cpu.jfreechart

import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import javax.swing.SwingUtilities


/**
 * CPU listener which display received CpuFormulaValues into a JFreeChart chart.
 *
 * @see http://www.jfree.org/jfreechart
 *
 * @author abourdon
 */
class CpuListener extends Listener {

  override def preStart() {
    SwingUtilities.invokeLater(new Runnable {
      def run() {
        Chart.run()
      }
    })
  }

  def messagesToListen = Array(classOf[CpuFormulaValues])

  def process(cpuFormulaValues: CpuFormulaValues) {
    Chart.process(cpuFormulaValues)
  }

  def acquire = {
    case cpuFormulaValues: CpuFormulaValues => process(cpuFormulaValues)
  }
}