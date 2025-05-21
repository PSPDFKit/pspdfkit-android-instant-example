/*
 *   Copyright © 2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.aia;

import static io.nutrient.domain.ai.AiAssistantKt.standaloneAiAssistant;

import com.pspdfkit.instant.example.utils.JwtGenerator;
import com.pspdfkit.instant.ui.InstantPdfActivity;
import io.nutrient.data.models.AiAssistantConfiguration;
import io.nutrient.domain.ai.AiAssistant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiAssistantInstantHelper {
    public AiAssistant createAiAssistant(
            InstantPdfActivity activity, String ipAddress, String sessionId, String instantId) {
        return standaloneAiAssistant(activity, aiAssistantConfiguration(activity, ipAddress, sessionId, instantId));
    }

    private static AiAssistantConfiguration aiAssistantConfiguration(
            InstantPdfActivity activity, String ipAddress, String sessionId, String documentId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("document_ids", List.of(documentId));
        claims.put("session_ids", List.of(sessionId));
        Map<String, Integer> requestLimit = new HashMap<>();
        requestLimit.put("requests", 160);
        requestLimit.put("time_period_s", 1000 * 60);
        claims.put("request_limit", requestLimit);

        return new AiAssistantConfiguration(
                "http://" + ipAddress + ":4000",
                new JwtGenerator(activity).generateJwtToken(claims, "keys/jwt.pem"),
                sessionId,
                null);
    }
}
