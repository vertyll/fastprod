package com.vertyll.fastprod.shared.i18n;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.vertyll.fastprod.shared.config.BackendApiProperties;
import com.vertyll.fastprod.shared.security.AuthTokenProvider;
import com.vertyll.fastprod.shared.service.BaseHttpService;

import tools.jackson.databind.ObjectMapper;

@Service
public class TranslationClient extends BaseHttpService {

    private static final String PUBLIC_ENDPOINT = "/translations/";
    private static final String ADMIN_ENDPOINT = "/admin/translations";

    public TranslationClient(
        BackendApiProperties backendApi,
        ObjectMapper objectMapper,
        AuthTokenProvider authTokenProvider
    ) {
        super(backendApi.url(), objectMapper, authTokenProvider);
    }

    public Map<String, String> messages(String language) {
        @SuppressWarnings("unchecked") Map<String, String> messages =
                get(PUBLIC_ENDPOINT + encode(language), Map.class);
        return messages == null ? Map.of() : messages;
    }

    public List<TranslationRow> list() {
        TranslationRow[] rows = get(ADMIN_ENDPOINT, TranslationRow[].class);
        return rows == null ? List.of() : Arrays.asList(rows);
    }

    public void update(String key, LocalizedText messages) {
        put(ADMIN_ENDPOINT + "/" + encode(key), Map.of("messages", messages), TranslationRow.class);
    }

    public void reset(String key) {
        delete(ADMIN_ENDPOINT + "/" + encode(key) + "/customization", Void.class);
    }
}
