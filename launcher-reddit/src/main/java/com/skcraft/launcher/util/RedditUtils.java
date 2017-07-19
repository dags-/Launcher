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
public class RedditUtils {

    public static List<String> getBackgrounds(String address) {
        HttpRequest request = null;

        try {
            request = HttpRequest.get(new URL(address));
            JsonNode root = new ObjectMapper().readTree(request.execute().getInputStream());
            if (root.has("data")) {
                JsonNode data = root.get("data");
                List<String> results = new LinkedList<String>();
                visit(data, results, 25);
                return results;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closer.close(request);
        }

        return Collections.emptyList();
    }

    private static void visit(JsonNode data, List<String> collector, int limit) {
        if (data.has("children")) {
            JsonNode children = data.get("children");
            for (JsonNode child : children) {
                if (child.has("data")) {
                    visitChild(child.get("data"), collector);
                }
                if (collector.size() >= limit) {
                    return;
                }
            }
        }
    }

    private static void visitChild(JsonNode data, List<String> collector) {
        // ignore videos/albums
        if (data.has("media_embed") && data.get("media_embed").size() > 0) {
            return;
        }

        if (data.has("preview")) {
            JsonNode preview = data.get("preview");
            if (preview.has("images")) {
                JsonNode images = preview.get("images");
                for (JsonNode image : images) {
                    if (image.has("source")) {
                        JsonNode source = image.get("source");
                        if (source.has("url")) {
                            String url = source.get("url").asText();
                            collector.add(url);
                            return;
                        }
                    }
                }
            }
        }
    }
}
