package org.odk.collect.android.preferences.utielities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class KnownUrlListUtils {
    private static final String KNOWN_URL_LIST = "knownUrlList";

    private static List<String> urlList;

    private KnownUrlListUtils() {
    }

    private static void setUpUrlList() {
        String urlListString = (String) GeneralSharedPreferences.getInstance().get(KNOWN_URL_LIST);

        urlList = urlListString == null || urlListString.isEmpty()
                ? new ArrayList<>()
                : new Gson().fromJson(urlListString, new TypeToken<List<String>>() {}.getType());

        if (urlList.isEmpty()) {
            addUrlToList(Collect.getInstance().getString(R.string.default_server_url));
        }
    }

    public static void addUrlToList(String url) {
        if (urlList == null) {
            setUpUrlList();
        }

        if (!urlList.contains(url)) {
            if (urlList.size() == 5) {
                urlList.remove(3);
            }

            urlList.add(0, url);
            String urlListString = new Gson().toJson(urlList);
            GeneralSharedPreferences.getInstance().save(KNOWN_URL_LIST, urlListString);
        }
    }

    public static List<String> getUrlList() {
        if (urlList == null) {
            setUpUrlList();
        }

        return urlList;
    }
}
