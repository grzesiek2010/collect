/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.google_API;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentList;

import org.odk.collect.android.logic.DriveListItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class GoogleDriveDao {

    private com.google.api.services.drive.Drive mService;

    public GoogleDriveDao(Context context, String googleUsername) {
        Collection<String> collection = new ArrayList<>();
        collection.add(com.google.api.services.drive.DriveScopes.DRIVE);

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, collection);

        mService = new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(), new GsonFactory(),
                        credential).build();

        credential.setSelectedAccountName(googleUsername);
    }

    public InputStream downloadFile(Drive service, File file) {
        if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
            try {
                HttpResponse resp = service.getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl())).execute();
                return resp.getContent();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            // The file doesn't have any content stored on Drive.
            return null;
        }
    }

    public File getFile(DriveListItem fileItem) throws IOException {
        return mService.files().get(fileItem.getDriveId()).execute();
    }

    public File getFile(String fileItem) throws IOException {
        return mService.files().get(fileItem).execute();
    }

    public Drive.Files.List getList(String qryClause) throws IOException {
        return mService.files().list().setQ(qryClause);
    }

    public ParentList getParentList(String qryClause) throws IOException {
        return mService.parents().list(qryClause).execute();
    }

}
