/*
 *  Copyright (C) 2019  Tobias Preuss
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.avpptr.umweltzone.zones.dataconverters

import android.content.Context
import android.support.annotation.ColorInt
import de.avpptr.umweltzone.extensions.getColorCompat
import de.avpptr.umweltzone.models.ChildZone
import de.avpptr.umweltzone.utils.LowEmissionZoneNumberConverter
import de.avpptr.umweltzone.zones.viewmodels.BadgeViewModel

fun ChildZone.toBadgeViewModel(context: Context) =
        BadgeViewModel("$zoneNumber", getBadgeColor(context))

@ColorInt
private fun ChildZone.getBadgeColor(context: Context): Int {
    val color = LowEmissionZoneNumberConverter.getColor(zoneNumber)
    return context.getColorCompat(color)
}
