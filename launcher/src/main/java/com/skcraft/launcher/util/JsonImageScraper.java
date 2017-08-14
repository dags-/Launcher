package com.skcraft.launcher.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class JsonImageScraper implements BackgroundProvider {

    @Override
    public List<String> getBackgrounds(String address) {
        try {
            URL url = new URL(address);
            JsonNode urls = new ObjectMapper().readTree(url);
            List<String> results = new LinkedList<String>();
            for (JsonNode u : urls) {
                results.add(u.asText());
            }
            return results;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
