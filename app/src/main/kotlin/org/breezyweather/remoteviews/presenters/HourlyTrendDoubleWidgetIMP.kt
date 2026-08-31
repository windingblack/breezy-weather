/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.remoteviews.presenters

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Canvas
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.WorkerThread
import androidx.core.graphics.createBitmap
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetTrendHourlyDoubleProvider
import org.breezyweather.common.extensions.getHour
import org.breezyweather.common.extensions.getTabletListAdaptiveWidth
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.remoteviews.trend.TrendLinearLayout
import org.breezyweather.remoteviews.trend.WidgetItemView
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import kotlin.math.min

object HourlyTrendDoubleWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(
        context: Context,
        location: Location?,
    ) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            innerUpdateWidget(context, location)
            return
        }
        AsyncHelper.runOnIO { innerUpdateWidget(context, location) }
    }

    @WorkerThread
    private fun innerUpdateWidget(
        context: Context,
        location: Location?,
    ) {
        val config = getWidgetConfig(
            context,
            context.getString(R.string.sp_widget_hourly_trend_double_setting)
        )
        if (config.cardStyle == "none") {
            config.cardStyle = "auto"
        }
        AppWidgetManager.getInstance(context).updateAppWidget(
            ComponentName(context, WidgetTrendHourlyDoubleProvider::class.java),
            getRemoteViews(
                context,
                location,
                context.getTabletListAdaptiveWidth(context.resources.displayMetrics.widthPixels),
                config.cardStyle,
                config.cardAlpha,
                config.textSize
            )
        )
    }

    @WorkerThread
    @SuppressLint("InflateParams", "WrongThread")
    private fun getDrawableView(
        context: Context,
        location: Location?,
        color: WidgetColor,
        textSize: Int
    ): View? {
        val weather = location?.weather ?: return null
        val provider = ResourcesProviderFactory.newInstance
        val itemCount = min(10, weather.nextHourlyForecast.size)
        val settings = SettingsManager.getInstance(context)
        val minimalIcon = settings.isWidgetUsingMonochromeIcons
        val lightTheme = color.isLightThemed

        val drawableView = LayoutInflater.from(context)
            .inflate(R.layout.widget_trend_hourly_double, null, false)
        val trendParent = drawableView.findViewById<TrendLinearLayout>(R.id.widget_trend_hourly_double)
        
        val widgetItemViews: Array<WidgetItemView> = arrayOf(
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_1),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_2),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_3),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_4),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_5),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_6),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_7),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_8),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_9),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_10)
        )
        widgetItemViews.forEachIndexed { i, widgetItemView ->
            if (i < itemCount) {
                weather.nextHourlyForecast.getOrNull(i)?.let { hourly ->
                    widgetItemView.setTitleText(hourly.date.getHour(location, context))
                    widgetItemView.setSubtitleText(null)
                    hourly.weatherCode?.let {
                        widgetItemView.setTopIconDrawable(
                            ResourceHelper.getWidgetNotificationIcon(
                                provider,
                                it,
                                hourly.isDaylight,
                                minimalIcon,
                                lightTheme
                            )
                        )
                    }
                    widgetItemView.trendItemView.visibility = View.GONE
                    widgetItemView.setBottomIconDrawable(null)
                    widgetItemView.setColor(lightTheme)
                    widgetItemView.setTextSize(textSize.toFloat())
                }
            } else {
                widgetItemView.visibility = View.INVISIBLE
            }
        }
        return drawableView
    }

    @WorkerThread
    fun getRemoteViews(
        context: Context,
        location: Location?,
        width: Int,
        cardStyle: String?,
        cardAlpha: Int,
        textSize: Int
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_remote)
        val color = WidgetColor(
            context,
            cardStyle!!,
            "auto",
            location?.isDaylight ?: true
        )
        val drawableView = getDrawableView(context, location, color, textSize) ?: return views
        if (location == null) return views

        val items: Array<WidgetItemView> = arrayOf(
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_1),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_2),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_3),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_4),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_5),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_6),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_7),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_8),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_9),
            drawableView.findViewById(R.id.widget_trend_hourly_double_item_10)
        )
        for (i in items) {
            i.setSize(width / 5f)
        }
        drawableView.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        drawableView.layout(
            0,
            0,
            drawableView.measuredWidth,
            drawableView.measuredHeight
        )
        val cache = createBitmap(drawableView.measuredWidth, drawableView.measuredHeight)
        val canvas = Canvas(cache)
        drawableView.draw(canvas)
        views.setImageViewBitmap(R.id.widget_remote_drawable, cache)
        views.setViewVisibility(R.id.widget_remote_progress, View.GONE)
        views.setImageViewResource(R.id.widget_remote_card, getCardBackgroundId(color))
        views.setInt(R.id.widget_remote_card, "setImageAlpha", (cardAlpha / 100.0 * 255).toInt())
        setOnClickPendingIntent(context, views, location)
        return views
    }

    fun isInUse(context: Context): Boolean {
        val widgetIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetTrendHourlyDoubleProvider::class.java))
        return widgetIds != null && widgetIds.isNotEmpty()
    }

    private fun setOnClickPendingIntent(
        context: Context,
        views: RemoteViews,
        location: Location,
    ) {
        views.setOnClickPendingIntent(
            R.id.widget_remote_drawable,
            getWeatherPendingIntent(context, location, Widgets.TREND_HOURLY_DOUBLE_PENDING_INTENT_CODE_WEATHER)
        )
    }
}
