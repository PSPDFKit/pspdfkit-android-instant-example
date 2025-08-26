/*
 *   Copyright © 2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.aia;

import static io.nutrient.domain.ai.AiAssistantKt.standaloneAiAssistant;

import android.content.Context;
import com.pspdfkit.document.providers.DataProvider;
import com.pspdfkit.document.providers.DataProvidersHelperKt;
import com.pspdfkit.instant.client.InstantClient;
import com.pspdfkit.instant.client.InstantDocumentDescriptor;
import com.pspdfkit.instant.document.InstantPdfDocument;
import com.pspdfkit.instant.example.api.WebExampleDocumentDescriptor;
import io.nutrient.data.models.AiAssistantConfiguration;
import io.nutrient.data.models.DocumentIdentifiers;
import io.nutrient.domain.ai.AiAssistant;
import java.util.ArrayList;
import java.util.List;

public class AiAssistantInstantHelper {

    /**
     * Factory method to create an instance of the AiAssistant.
     * @param context [Context] The context of the application.
     * @param serverUrl Instant Server url.
     * @param allDocumentsDescriptors List of descriptors for each document on the server.
     * @param ipAddress IP address of a server where your instance of the Nutrient AI Assistant can be reached.
     * @param sessionId A unique identifier for the chat session, which can be used to recall a session in the future.
     * @param jwtToken JSON Web Token used for authentication with your instance of the Nutrient AI Assistant server.
     * It returns list of Instant Document identifiers that can be used for token creation.
     * @return An instance of the [AiAssistant] class.
     */
    public static AiAssistant createAiAssistantForInstant(
            Context context,
            String serverUrl,
            List<WebExampleDocumentDescriptor> allDocumentsDescriptors,
            String ipAddress,
            String sessionId,
            JwtTokenFunction jwtToken) {
        InstantClient instantClient = InstantClient.create(context, serverUrl);

        List<String> instantIds = new ArrayList<>();

        List<DocumentIdentifiers> listOfDocumentsIdentifiers = new ArrayList<>();
        List<String> documentLayersJwtTokens = new ArrayList<>();

        if (allDocumentsDescriptors != null) {
            for (WebExampleDocumentDescriptor documentDescriptor : allDocumentsDescriptors) {
                documentLayersJwtTokens.add(documentDescriptor.getDefaultLayer().jwt);
            }
        }

        for (String jwt : documentLayersJwtTokens) {
            InstantDocumentDescriptor instantDocumentDescriptor = instantClient.getInstantDocumentDescriptorForJwt(jwt);
            instantIds.add(instantDocumentDescriptor.getDocumentId());
            InstantPdfDocument instantPdfDocument = instantClient.openDocument(jwt);
            DataProvider dataProvider =
                    DataProvidersHelperKt.getDataProviderFromDocumentSource(instantPdfDocument.getDocumentSource());

            DocumentIdentifiers documentIdentifiers = new DocumentIdentifiers(
                    dataProvider,
                    instantDocumentDescriptor.getDocumentId(),
                    instantDocumentDescriptor.getSourcePdfSha(),
                    null,
                    instantDocumentDescriptor.getLayerName());
            listOfDocumentsIdentifiers.add(documentIdentifiers);
        }

        return standaloneAiAssistant(
                context,
                new AiAssistantConfiguration(
                        "http://" + ipAddress + ":4000", jwtToken.apply(instantIds), sessionId, null),
                listOfDocumentsIdentifiers);
    }

    public interface JwtTokenFunction {
        String apply(List<String> instantIds);
    }
}
