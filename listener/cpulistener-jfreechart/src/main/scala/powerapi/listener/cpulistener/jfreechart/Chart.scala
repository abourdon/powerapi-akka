/**
 * Copyright (C) 2012 Inria, University Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */
package powerapi.listener.cpulistener.jfreechart
import java.awt.Dimension
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.data.time.Millisecond
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.time.TimeSeriesDataItem
import org.jfree.ui.ApplicationFrame
import org.jfree.ui.RefineryUtilities
import akka.util.duration.intToDurationInt
import javax.swing.JFrame
import powerapi.core.Process
import powerapi.formula.cpuformula.CpuFormulaValues
import java.util.Date
import org.jfree.data.time.FixedMillisecond
import java.awt.Toolkit

class Chart(title: String) {
  val dataset = new TimeSeriesCollection
  val chart = ChartFactory.createTimeSeriesChart(title,
    Chart.xValues, Chart.yValues, dataset, true, true, false);
  val timeSeries = collection.mutable.HashMap[Process, TimeSeries]()

  def process(implicit cpuFormulaValues: CpuFormulaValues) {
    val pid = cpuFormulaValues.tick.subscription.process
    if (!timeSeries.contains(pid)) {
      val serie = new TimeSeries(pid.toString)
      dataset.addSeries(serie)
      timeSeries += (pid -> serie)
    }
    timeSeries(pid).add(new TimeSeriesDataItem(new FixedMillisecond(cpuFormulaValues.tick.timestamp), cpuFormulaValues.energy.power))
  }
}

object Chart {
  val xValues = "Time (s)"
  val yValues = "Power (W)"
  val title = "PowerAPI"
  lazy val chart = new Chart(title)

  val chartPanel = {
    val panel = new ChartPanel(chart.chart)
    panel.setMouseWheelEnabled(true);
    panel.setDomainZoomable(true);
    panel.setFillZoomRectangle(true);
    panel.setRangeZoomable(true);
    panel
  }

  val applicationFrame = {
    val app = new ApplicationFrame(title)
    app
  }

  def run() {
    applicationFrame.setContentPane(chartPanel)
    val dimension = Toolkit.getDefaultToolkit().getScreenSize()
    applicationFrame.setSize(new Dimension(5 * dimension.width / 6, 5 * dimension.height / 6))
    applicationFrame.setVisible(true)
    RefineryUtilities.centerFrameOnScreen(applicationFrame)
  }

  def process(implicit cpuFormulaValues: CpuFormulaValues) {
    chart.process
  }
}