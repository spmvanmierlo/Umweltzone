/*
 *  Copyright (C) 2014  Tobias Preuss, Peter Vasil
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

package de.avpptr.umweltzone.utils;

import android.content.Context;
import android.support.v4.util.LruCache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.ligi.tracedroid.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.avpptr.umweltzone.R;
import de.avpptr.umweltzone.Umweltzone;
import de.avpptr.umweltzone.analytics.TrackingPoint;
import de.avpptr.umweltzone.caching.GenericCache;
import de.avpptr.umweltzone.caching.ResourceIdCache;
import de.avpptr.umweltzone.models.Circuit;
import de.avpptr.umweltzone.models.CircuitDeserializer;
import de.avpptr.umweltzone.models.Faq;
import de.avpptr.umweltzone.models.LowEmissionZone;

public abstract class ContentProvider {
    private static final LruCache<String, List<Circuit>> CIRCUITS_CACHE = new LruCache<String, List<Circuit>>(6);
    private static final GenericCache mResourceIdCache = new ResourceIdCache(6);

    public static List<Faq> getFaqs(final Context context) {
        // Do not accidentally compare with Locale.GERMAN
        if (Locale.getDefault().equals(Locale.GERMANY)) {
            return getContent(context, "faqs_de", Faq.class);
        }
        return getContent(context, "faqs_en", Faq.class);
    }

    public static List<LowEmissionZone> getLowEmissionZones(final Context context) {
        return getContent(context, "zones_de", LowEmissionZone.class);
    }

    public static List<Circuit> getCircuits(final Context context, final String zoneName) {
        String keyForZone = generateKeyForZoneWith(zoneName);
        List<Circuit> circuits = CIRCUITS_CACHE.get(keyForZone);
        if(circuits ==null) {
            CIRCUITS_CACHE.put(keyForZone, circuits = getContent(context, keyForZone, Circuit.class));
        }
            return circuits;
    }

    private static String generateKeyForZoneWith(String zoneName) {
        return "zone_" + zoneName;
    }
    private static <T> List<T> getContent(final Context context, final String fileName, Class<T> contentType) {
        return getContent(context, fileName, "raw", contentType);
    }

    private static <T> List<T> getContent(final Context context, final String fileName, final String folderName, Class<T> contentType) {
        // Invoke cache
        int rawResourceId = (Integer) mResourceIdCache.readObject(context, fileName, folderName);
        if (rawResourceId == de.avpptr.umweltzone.contract.Resources.INVALID_RESOURCE_ID) {
            final String filePath = folderName + "/" + fileName;
            Umweltzone.getTracker().trackError(TrackingPoint.ResourceNotFoundError, filePath);
            throw new IllegalStateException("Resource for file path '" + filePath + "' not found.");
        }
        InputStream inputStream = context.getResources().openRawResource(rawResourceId);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Circuit.class, new CircuitDeserializer());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        String datePattern = context.getString(R.string.config_zone_number_since_date_format);
        objectMapper.setDateFormat(new SimpleDateFormat(datePattern, Locale.getDefault()));
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            CollectionType collectionType = typeFactory.constructCollectionType(List.class, contentType);
            return objectMapper.readValue(inputStream, collectionType);
        } catch (IOException e) {
            // TODO Aware that app will crash when JSON is mis-structured.
            e.printStackTrace();
        }
        Log.e(ContentProvider.class.getName(), "Failure parsing zone data for: " + fileName);
        return null;
    }

}
