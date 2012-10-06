/**
 * Copyright (C) 2012 Inria, University Lille 1.
 *
 * This file is part of PowerAPI.
 *
 * PowerAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PowerAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerAPI.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: powerapi-user-list@googlegroups.com.
 */
package fr.inria.powerapi.listener.cpu.jfreechart

import fr.inria.powerapi.core.Process
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import java.awt.{ Toolkit, Dimension }
import org.jfree.chart.{ ChartPanel, ChartFactory }
import org.jfree.data.time.{ TimeSeriesDataItem, TimeSeriesCollection, TimeSeries, FixedMillisecond }
import org.jfree.ui.{ RefineryUtilities, ApplicationFrame }
import java.awt.BasicStroke

/**
 * Display received CpuFormulaValues to the wrapped JFreeChart chart.
 *
 * @author abourdon
 */
class Chart(title: String) {
  val dataset = new TimeSeriesCollection
  val chart = ChartFactory.createTimeSeriesChart(title,
    Chart.xValues, Chart.yValues, dataset, true, true, false)
  val timeSeries = collection.mutable.HashMap[Process, TimeSeries]()

  def process(implicit cpuFormulaValues: CpuFormulaValues) {
    val pid = cpuFormulaValues.tick.subscription.process
    if (!timeSeries.contains(pid)) {
      val serie = new TimeSeries(pid.toString)
      dataset.addSeries(serie)
      timeSeries += (pid -> serie)
      chart.getXYPlot().getRenderer().setSeriesStroke(dataset.getSeriesCount() - 1, new BasicStroke(3))
    }
    timeSeries(pid).add(new TimeSeriesDataItem(new FixedMillisecond(cpuFormulaValues.tick.timestamp), cpuFormulaValues.energy.power))
  }
}

/**
 * Chart companion object containing the JFreeChart's ApplicationFrame and some configurations.
 *
 * @author abourdon
 */
object Chart {
  val xValues = "Time (s)"
  val yValues = "Power (W)"
  val title = "PowerAPI"
  lazy val chart = {
    val ch = new Chart(title)
    val plot = ch.chart.getXYPlot()
    plot.setBackgroundPaint(java.awt.Color.WHITE)
    plot.setDomainGridlinesVisible(true)
    plot.setDomainGridlinePaint(java.awt.Color.GRAY)
    plot.setRangeGridlinesVisible(true)
    plot.setRangeGridlinePaint(java.awt.Color.GRAY)
    ch
  }

  val chartPanel = {
    val panel = new ChartPanel(chart.chart)
    panel.setMouseWheelEnabled(true)
    panel.setDomainZoomable(true)
    panel.setFillZoomRectangle(true)
    panel.setRangeZoomable(true)
    panel
  }

  val applicationFrame = {
    val app = new ApplicationFrame(title)
    app
  }

  def run() {
    applicationFrame.setContentPane(chartPanel)
    applicationFrame.setSize(new Dimension(800, 600))
    applicationFrame.setVisible(true)
  }

  def process(implicit cpuFormulaValues: CpuFormulaValues) {
    chart.process
  }
}