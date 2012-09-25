/**
 * Copyright (C) 2012 Inria, University Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 *
 * Contact: powerapi-user-list@googlegroups.com
 */
package fr.inria.powerapi.example.adamdemo.full

import java.awt.Dimension
import java.awt.Toolkit

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.data.time.FixedMillisecond
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.time.TimeSeriesDataItem
import org.jfree.ui.ApplicationFrame
import org.jfree.ui.RefineryUtilities

/**
 * Display received CpuFormulaValues to the wrapped JFreeChart chart.
 *
 * @author abourdon
 */
class Chart(title: String) {
  val dataset = new TimeSeriesCollection
  val chart = ChartFactory.createTimeSeriesChart(title,
    Chart.xValues, Chart.yValues, dataset, true, true, false)
  val timeSeries = collection.mutable.HashMap[String, TimeSeries]()
  private var offset = 0.0

  def add(values: Map[String, Double], timestamp: Long) {
    values.foreach({ value =>
      if (!timeSeries.contains(value._1)) {
        val serie = new TimeSeries(value._1)
        dataset.addSeries(serie)
        timeSeries += (value._1 -> serie)
      }
      timeSeries(value._1).addOrUpdate(new FixedMillisecond(timestamp), value._2 + getOffset)
    })
  }

  def remove(serie: String) {
    timeSeries.remove(serie)
    dataset.removeSeries(new TimeSeries(serie))
  }

  def setOffset(offset: Double) = synchronized {
    this.offset = offset
    println("Setting offset to " + offset)
  }

  def getOffset = synchronized {
    offset
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
  lazy val chart = new Chart(title)

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

  def setTitle(title: String) {
    chartPanel.getChart().setTitle(title)
    applicationFrame.setTitle(title)
  }

  def run() {
    applicationFrame.setContentPane(chartPanel)
    applicationFrame.setSize(new Dimension(800, 600))
    applicationFrame.setVisible(true)
  }

  def add(values: Map[String, Double], timestamp: Long) {
    chart.add(values, timestamp)
  }

  def remove(serie: String) {
    chart.remove(serie)
  }

  def setOffset(offset: Double) {
    chart.setOffset(offset)
  }
}