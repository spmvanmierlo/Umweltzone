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

package de.avpptr.umweltzone.details

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import de.avpptr.umweltzone.BuildConfig
import de.avpptr.umweltzone.R
import de.avpptr.umweltzone.analytics.TrackingPoint
import de.avpptr.umweltzone.base.BaseFragment
import de.avpptr.umweltzone.details.dataconverters.toDetailsViewModel
import de.avpptr.umweltzone.details.dataconverters.toOtherDetailsViewModel
import de.avpptr.umweltzone.details.viewmodels.LezDetailsViewModel
import de.avpptr.umweltzone.details.viewmodels.OtherDetailsViewModel
import de.avpptr.umweltzone.extensions.isVisible
import de.avpptr.umweltzone.extensions.setBackgroundResourceOrHide
import de.avpptr.umweltzone.extensions.textOrHide
import de.avpptr.umweltzone.models.AdministrativeZone
import de.avpptr.umweltzone.models.ChildZone
import de.avpptr.umweltzone.models.LowEmissionZone
import de.avpptr.umweltzone.utils.ViewHelper
import org.parceler.Parcels

class DetailsFragment : BaseFragment() {

    private val zoneDetailsView by lazy { view?.findViewById(R.id.zoneDetailsView) as LinearLayout }

    private var administrativeZone: AdministrativeZone? = null

    public override fun getLayoutResource() = R.layout.fragment_zone_details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = arguments
        if (extras != null) {
            val parcelable = extras.getParcelable<Parcelable>(BUNDLE_KEY_ADMINISTRATIVE_ZONE)
            administrativeZone = Parcels.unwrap<AdministrativeZone>(parcelable)
        }
    }

    override fun onResume() {
        super.onResume()
        if (activity != null && administrativeZone != null) {
            updateDetails(activity!!, administrativeZone!!)
            updateSubTitle(administrativeZone!!.displayName)
        }
    }

    private fun updateDetails(activity: Activity, zone: AdministrativeZone) {
        zoneDetailsView.removeAllViews()
        zone.childZones.forEach { updateChildZoneDetails(activity, it) }
        addOtherDetails(zone.toOtherDetailsViewModel())
        addVerticalSpace()
    }

    private fun updateChildZoneDetails(activity: Activity, childZone: ChildZone) {
        when (childZone) {
            is LowEmissionZone -> addLowEmissionZoneDetails(childZone.toDetailsViewModel(activity))
        }
    }

    private fun addLowEmissionZoneDetails(viewModel: LezDetailsViewModel) {
        val detailsView = layoutInflater.inflate(R.layout.details_low_emission_zone)
        updateLezDetails(detailsView, viewModel)
        zoneDetailsView.addChildView(detailsView)
    }

    private fun addOtherDetails(viewModel: OtherDetailsViewModel) {
        val detailsView = layoutInflater.inflate(R.layout.details_other)
        updateOtherDetails(detailsView, viewModel)
        zoneDetailsView.addChildView(detailsView)
    }

    private fun addVerticalSpace() {
        val detailsView = layoutInflater.inflate(R.layout.vertical_space)
        zoneDetailsView.addChildView(detailsView)
    }

    private fun LayoutInflater.inflate(@LayoutRes resource: Int) =
            inflate(resource, zoneDetailsView, false)

    private fun LinearLayout.addChildView(view: View) =
            addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

    private fun updateLezDetails(rootView: View, model: LezDetailsViewModel) {
        val roadSignView = rootView.findViewById(R.id.detailsLezRoadSignView) as TextView
        val listOfCitiesView = rootView.findViewById(R.id.detailsLezListOfCitiesView) as TextView
        val zoneNumberSinceView = rootView.findViewById(R.id.detailsLezZoneNumberSinceView) as TextView
        val nextZoneNumberAsOfView = rootView.findViewById(R.id.detailsLezNextZoneNumberAsOfView) as TextView
        val abroadLicensedVehicleZoneInfoView = rootView.findViewById(R.id.detailsLezAbroadLicensedVehicleZoneInfoView) as TextView
        val geometryUpdatedAtView = rootView.findViewById(R.id.detailsLezGeometryUpdatedAtView) as TextView
        val geometrySourceView = rootView.findViewById(R.id.detailsLezGeometrySourceView) as TextView
        return with(model) {
            roadSignView.setBackgroundResourceOrHide = zoneStatusId
            listOfCitiesView.textOrHide = listOfCitiesText
            zoneNumberSinceView.textOrHide = zoneNumberSinceText
            nextZoneNumberAsOfView.textOrHide = nextZoneNumberAsOfText
            abroadLicensedVehicleZoneInfoView.textOrHide = abroadLicensedVehicleZoneNumberText
            geometryUpdatedAtView.textOrHide = geometryUpdatedAtText
            geometrySourceView.textOrHide = geometrySourceText
        }
    }

    private fun updateOtherDetails(rootView: View, model: OtherDetailsViewModel) {
        val furtherInformationView = rootView.findViewById(R.id.detailsOtherFurtherInformationView) as TextView
        val badgeOnlineView = rootView.findViewById(R.id.detailsOtherBadgeOnlineView) as TextView
        val activity = requireActivity()
        return with(model) {
            ViewHelper.setupTextViewExtended(activity,
                    furtherInformationView,
                    R.string.city_info_further_information,
                    furtherInformation,
                    TrackingPoint.CityInfoFurtherInfoClick,
                    zoneName)

            if (urlBadgeOnline.isEmpty()) {
                badgeOnlineView.isVisible = false
            } else {
                badgeOnlineView.isVisible = true
                ViewHelper.setupTextViewExtended(activity,
                        badgeOnlineView,
                        R.string.city_info_badge_online_title,
                        urlBadgeOnline,
                        TrackingPoint.CityInfoBadgeOnlineClick,
                        zoneName)
            }
        }
    }

    companion object {

        const val FRAGMENT_TAG = BuildConfig.APPLICATION_ID + ".DETAILS_FRAGMENT_TAG"

        const val BUNDLE_KEY_ADMINISTRATIVE_ZONE = BuildConfig.APPLICATION_ID + ".ADMINISTRATIVE_ZONE_BUNDLE_KEY"

        @JvmStatic
        fun newInstance(administrativeZone: AdministrativeZone): DetailsFragment {
            val fragment = DetailsFragment()
            val extras = Bundle()
            val parcelable = Parcels.wrap(administrativeZone)
            extras.putParcelable(BUNDLE_KEY_ADMINISTRATIVE_ZONE, parcelable)
            fragment.arguments = extras
            return fragment
        }
    }

}